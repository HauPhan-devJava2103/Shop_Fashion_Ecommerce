package vn.web.fashionshop.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.ProductVariant;
import vn.web.fashionshop.entity.Review;
import vn.web.fashionshop.repository.ImageRepository;
import vn.web.fashionshop.repository.ProductRepository;
import vn.web.fashionshop.repository.ProductVariantRepository;
import vn.web.fashionshop.repository.ReviewRepository;

@Controller
public class ProductDetailController {

    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ReviewRepository reviewRepository;

    public ProductDetailController(
            ProductRepository productRepository,
            ImageRepository imageRepository,
            ProductVariantRepository productVariantRepository,
            ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
        this.productVariantRepository = productVariantRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Product product = productRepository.findByIdForDetail(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Load bags separately to avoid Hibernate MultipleBagFetchException
        var images = imageRepository.findByProductId(product.getId());
        var variants = productVariantRepository.findByProductIdOrderByIdAsc(product.getId());
        var approvedReviews = reviewRepository.findApprovedByProductIdWithUser(product.getId());

        product.setImages(images);
        product.setVariants(variants);
        product.setReviews(approvedReviews);

        List<ProductVariant> variantsSafe = variants != null ? variants : List.of();

        Set<String> colors = new LinkedHashSet<>();
        Set<String> sizes = new LinkedHashSet<>();
        for (ProductVariant v : variantsSafe) {
            if (v == null) {
                continue;
            }
            if (v.getColor() != null && !v.getColor().isBlank()) {
                colors.add(v.getColor());
            }
            if (v.getSize() != null) {
                sizes.add(v.getSize().name());
            }
        }
    List<Review> approvedReviewsSafe = approvedReviews != null ? approvedReviews : List.of();

        double avgRating = approvedReviewsSafe.isEmpty()
                ? 0.0
            : approvedReviewsSafe.stream()
                        .map(Review::getRating)
                        .filter(x -> x != null)
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);

        BigDecimal discountedPrice = computeDiscountedPrice(product);

        model.addAttribute("product", product);
        model.addAttribute("discountedPrice", discountedPrice);
        model.addAttribute("colors", colors);
        model.addAttribute("sizes", sizes);
        model.addAttribute("reviews", approvedReviewsSafe);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", approvedReviewsSafe.size());

        return "product-detail";
    }

    private static BigDecimal computeDiscountedPrice(Product product) {
        if (product == null || product.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal price = product.getPrice();
        BigDecimal discount = product.getDiscount() != null ? product.getDiscount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }
        return price.subtract(price.multiply(discount)
                .divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
