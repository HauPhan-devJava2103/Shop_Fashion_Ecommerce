package vn.web.fashionshop.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.repository.ReviewRepository;
import vn.web.fashionshop.service.CategoryService;
import vn.web.fashionshop.service.ProductService;

@Controller
@RequiredArgsConstructor
public class CollectionController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final ReviewRepository reviewRepository;

    @GetMapping("/collections/{rootSlug}")
    public String collectionByRootSlug(
            @PathVariable String rootSlug,
            @RequestParam(name = "category", required = false) String categorySlug,
            @RequestParam(name = "color", required = false) List<String> colors,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sort", required = false) String sort,
            Model model) {
        Category rootCategory = null;
        try {
            rootCategory = categoryService.findBySlug(rootSlug);
        } catch (RuntimeException ex) {
            // If category doesn't exist yet, still render page (empty products) instead of forwarding to /error.
        }

        Category selectedCategory = null;
        if (categorySlug != null && !categorySlug.isBlank()) {
            try {
                selectedCategory = categoryService.findBySlug(categorySlug);
            } catch (RuntimeException ex) {
                // Ignore invalid category slug; fallback to root listing
            }
        }

        List<String> normalizedColors = (colors == null || colors.isEmpty())
            ? null
            : colors.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.trim().toLowerCase())
                .toList();

        List<Product> products = (selectedCategory != null)
            ? productService.getActiveProductsByCategorySlugFiltered(categorySlug, normalizedColors, minPrice, maxPrice)
            : productService.getActiveProductsByRootCategorySlugFiltered(rootSlug, normalizedColors, minPrice, maxPrice);

        String normalizedSort = (sort == null || sort.isBlank()) ? "rating_desc" : sort;
        products = sortProducts(products, normalizedSort);
        List<Category> childCategories = rootCategory != null
                ? categoryService.getActiveChildrenByParentSlug(rootSlug)
                : List.of();

        model.addAttribute("rootCategory", rootCategory);
        model.addAttribute("rootSlug", rootSlug);
        model.addAttribute("childCategories", childCategories);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("products", products);
        model.addAttribute("productCount", products != null ? products.size() : 0);
        model.addAttribute("selectedSort", normalizedSort);

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

        String fallbackTitle = mapTitle(rootSlug, rootCategory != null ? rootCategory.getCategoryName() : "");
        model.addAttribute("pageTitle", selectedCategory != null ? selectedCategory.getCategoryName() : fallbackTitle);

        return "collection";
    }

    private List<Product> sortProducts(List<Product> products, String sort) {
        if (products == null || products.isEmpty()) {
            return products;
        }
        String s = sort != null ? sort.trim().toLowerCase() : "";

        Comparator<Product> byDiscountedPriceAsc = Comparator
                .comparing(CollectionController::computeDiscountedPrice, Comparator.nullsLast(BigDecimal::compareTo));
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
            default -> null;
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
            default -> (fallback == null || fallback.isBlank()) ? "Bộ sưu tập" : fallback;
        };
    }
}
