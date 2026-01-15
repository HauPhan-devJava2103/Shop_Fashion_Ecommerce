package vn.web.fashionshop.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.repository.ReviewRepository;
import vn.web.fashionshop.service.CategoryService;
import vn.web.fashionshop.service.ProductService;

@Controller
public class ShopController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final ReviewRepository reviewRepository;

    public ShopController(CategoryService categoryService, ProductService productService, ReviewRepository reviewRepository) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/shop")
    public String shop(
            @RequestParam(name = "root", required = false) String rootSlug,
            @RequestParam(name = "category", required = false) String categorySlug,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "color", required = false) List<String> colors,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sort", required = false) String sort,
            Model model) {

        List<String> allowedRoots = List.of("men", "women", "accessories");
        String normalizedRoot = (rootSlug != null && allowedRoots.contains(rootSlug)) ? rootSlug : null;

        Category selectedCategory = null;
        if (categorySlug != null && !categorySlug.isBlank()) {
            try {
                selectedCategory = categoryService.findBySlug(categorySlug);
            } catch (RuntimeException ex) {
                // ignore invalid category
            }
        }

        // If category is selected but root isn't, infer root from category tree
        if (normalizedRoot == null && selectedCategory != null) {
            Category cursor = selectedCategory;
            while (cursor.getParentCategory() != null) {
                cursor = cursor.getParentCategory();
            }
            if (cursor.getSlug() != null && allowedRoots.contains(cursor.getSlug())) {
                normalizedRoot = cursor.getSlug();
            }
        }

        List<String> normalizedColors = (colors == null || colors.isEmpty())
                ? null
                : colors.stream()
                        .filter(c -> c != null && !c.isBlank())
                        .map(c -> c.trim().toLowerCase())
                        .toList();

        List<Product> products;
        if (selectedCategory != null) {
            products = productService.getActiveProductsByCategorySlugFiltered(categorySlug, normalizedColors, minPrice, maxPrice);
        } else if (normalizedRoot != null) {
            products = productService.getActiveProductsByRootCategorySlugFiltered(normalizedRoot, normalizedColors, minPrice, maxPrice);
        } else {
            products = productService.getActiveProductsByRootCategorySlugsFiltered(allowedRoots, normalizedColors, minPrice, maxPrice);
        }

        // Keyword filter (simple contains on product name)
        if (keyword != null && !keyword.isBlank() && products != null && !products.isEmpty()) {
            String k = keyword.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> p != null && p.getProductName() != null && p.getProductName().toLowerCase().contains(k))
                    .toList();
        }

        // Sorting (in-memory)
        String normalizedSort = (sort == null || sort.isBlank()) ? "rating_desc" : sort;
        products = sortProducts(products, normalizedSort);

        // Sidebar categories
        List<Category> rootCategories = allowedRoots.stream()
                .map(slug -> {
                    try {
                        return categoryService.findBySlug(slug);
                    } catch (RuntimeException ex) {
                        return null;
                    }
                })
                .filter(c -> c != null && (c.getIsActive() == null || Boolean.TRUE.equals(c.getIsActive())))
                .toList();

        List<Category> childCategories = normalizedRoot != null
                ? categoryService.getActiveChildrenByParentSlug(normalizedRoot)
                : List.of();

        Map<String, List<Category>> rootChildrenMap = new HashMap<>();
        for (Category r : rootCategories) {
            rootChildrenMap.put(r.getSlug(), categoryService.getActiveChildrenByParentSlug(r.getSlug()));
        }

        model.addAttribute("rootCategories", rootCategories);
        model.addAttribute("selectedRootSlug", normalizedRoot);
        model.addAttribute("childCategories", childCategories);
        model.addAttribute("rootChildrenMap", rootChildrenMap);
        model.addAttribute("selectedCategory", selectedCategory);

        model.addAttribute("products", products);
        model.addAttribute("productCount", products != null ? products.size() : 0);
        model.addAttribute("selectedSort", normalizedSort);
        model.addAttribute("keyword", keyword);

        // Price slider bounds + selected values (use discounted price if available)
        long priceSliderMin = 0L;
        long priceSliderMax = 1_000_000L;
        if (products != null && !products.isEmpty()) {
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            for (Product p : products) {
                BigDecimal discounted = computeDiscountedPrice(p);
                if (discounted == null) {
                    continue;
                }
                long v = discounted.setScale(0, RoundingMode.HALF_UP).longValue();
                min = Math.min(min, v);
                max = Math.max(max, v);
            }
            if (min != Long.MAX_VALUE && max != Long.MIN_VALUE) {
                priceSliderMin = Math.max(0L, min);
                priceSliderMax = Math.max(priceSliderMin, max);
            }
        }

        long priceValueMin = minPrice != null ? minPrice.setScale(0, RoundingMode.HALF_UP).longValue() : priceSliderMin;
        long priceValueMax = maxPrice != null ? maxPrice.setScale(0, RoundingMode.HALF_UP).longValue() : priceSliderMax;
        if (priceValueMax < priceValueMin) {
            long tmp = priceValueMin;
            priceValueMin = priceValueMax;
            priceValueMax = tmp;
        }

        model.addAttribute("priceSliderMin", priceSliderMin);
        model.addAttribute("priceSliderMax", priceSliderMax);
        model.addAttribute("priceValueMin", priceValueMin);
        model.addAttribute("priceValueMax", priceValueMax);

        String title;
        if (selectedCategory != null) {
            title = selectedCategory.getCategoryName();
        } else if (normalizedRoot != null) {
            title = mapTitle(normalizedRoot, "Shop");
        } else {
            title = "Shop";
        }
        model.addAttribute("pageTitle", title);

        return "shop";
    }

    private List<Product> sortProducts(List<Product> products, String sort) {
        if (products == null || products.isEmpty()) {
            return products;
        }
        String s = sort != null ? sort.trim().toLowerCase() : "";

        Comparator<Product> byDiscountedPriceAsc = Comparator
                .comparing(ShopController::computeDiscountedPrice, Comparator.nullsLast(BigDecimal::compareTo));
        Comparator<Product> byDiscountedPriceDesc = byDiscountedPriceAsc.reversed();
        Comparator<Product> byCreatedAtDesc = Comparator
                .comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();

        final Map<Long, Double> ratingMap = ("rating_desc".equals(s) || "highestrated".equals(s) || "rating".equals(s))
            ? getAverageRatings(products)
            : Map.of();

        Comparator<Product> byRatingDesc = Comparator
            .comparing((Product p) -> ratingMap.getOrDefault(p.getId(), 0.0))
            .reversed()
            .thenComparing(byCreatedAtDesc);

        Comparator<Product> comparator = switch (s) {
            case "pricehigh" -> byDiscountedPriceDesc;
            case "pricelow" -> byDiscountedPriceAsc;
            case "newest" -> byCreatedAtDesc;
            case "rating_desc", "highestrated", "rating" -> byRatingDesc;
            default -> null; // keep existing order
        };

        if (comparator == null) {
            return products;
        }

        return products.stream().sorted(comparator).toList();
    }

    private Map<Long, Double> getAverageRatings(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Map.of();
        }

        List<Long> ids = products.stream()
                .filter(Objects::nonNull)
                .map(Product::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = reviewRepository.findAverageRatingForProductIds(ids);
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> map = new HashMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }
            Object idObj = row[0];
            Object avgObj = row[1];
            if (idObj instanceof Long id) {
                Double avg = (avgObj instanceof Number n) ? n.doubleValue() : 0.0;
                map.put(id, avg);
            }
        }
        return map;
    }

    private static BigDecimal computeDiscountedPrice(Product product) {
        if (product == null || product.getPrice() == null) {
            return null;
        }
        BigDecimal price = product.getPrice();
        BigDecimal discount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        return price.subtract(price.multiply(discount).divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP));
    }

    private static String mapTitle(String rootSlug, String fallback) {
        if (rootSlug == null) {
            return fallback;
        }
        return switch (rootSlug.toLowerCase()) {
            case "men" -> "Thời trang nam";
            case "women" -> "Thời trang nữ";
            case "accessories" -> "Phụ kiện";
            default -> (fallback == null || fallback.isBlank()) ? "Shop" : fallback;
        };
    }
}
