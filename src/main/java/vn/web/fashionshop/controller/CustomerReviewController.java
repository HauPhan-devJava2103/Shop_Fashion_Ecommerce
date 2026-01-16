package vn.web.fashionshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.web.fashionshop.entity.Review;
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.FileUploadService;
import vn.web.fashionshop.service.ReviewService;

@Controller
public class CustomerReviewController {

    private final ReviewService reviewService;
    private final FileUploadService fileUploadService;

    public CustomerReviewController(ReviewService reviewService, FileUploadService fileUploadService) {
        this.reviewService = reviewService;
        this.fileUploadService = fileUploadService;
    }

    // View my reviews
    @GetMapping("/my-reviews")
    public String myReviews(Model model) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        var reviews = reviewService.getMyReviews(email);
        model.addAttribute("reviews", reviews);
        return "my-reviews";
    }

    // Submit review for an order item
    @PostMapping("/reviews/submit")
    public String submitReview(
            @RequestParam("orderItemId") Long orderItemId,
            @RequestParam("rating") Integer rating,
            @RequestParam(value = "comment", required = false) String comment,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {

        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        try {
            // Upload image if provided
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                var uploadResult = fileUploadService.storeImage(image);
                imageUrl = uploadResult.url();
            }

            Review review = reviewService.createCustomerReview(email, orderItemId, rating, comment, imageUrl);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cảm ơn bạn đã đánh giá! Đánh giá sẽ được hiển thị sau khi được duyệt.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi xảy ra khi gửi đánh giá.");
        }

        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/orders";
    }

    // Check if can review (AJAX)
    @GetMapping("/reviews/can-review/{orderItemId}")
    public String canReview(
            @PathVariable Long orderItemId,
            Model model) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            model.addAttribute("canReview", false);
            return "fragments/review-status :: canReview";
        }

        boolean canReview = reviewService.canReview(email, orderItemId);
        model.addAttribute("canReview", canReview);
        return "fragments/review-status :: canReview";
    }
}
