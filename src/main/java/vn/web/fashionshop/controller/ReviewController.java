package vn.web.fashionshop.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.web.fashionshop.entity.Review;
import vn.web.fashionshop.service.ReviewService;

@Controller
@RequestMapping("/admin/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping({ "", "/" })
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String index(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating,
            Model model) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Review> reviewsPage = reviewService.searchReviews(keyword, status, rating, pageable);

        model.addAttribute("totalReviews", reviewService.getTotalReviews());
        model.addAttribute("pendingReviews", reviewService.getPendingReviews());
        model.addAttribute("approvedReviews", reviewService.getApprovedReviews());
        model.addAttribute("averageRating", reviewService.getAverageRating());

        // Additional Stats
        model.addAttribute("reviewsThisMonth", reviewService.getReviewsThisMonth());
        model.addAttribute("reviewsToday", reviewService.getReviewsToday());
        model.addAttribute("reviewsWithImage", reviewService.getReviewsWithImage());
        model.addAttribute("approvalRate", reviewService.getApprovalRate());

        // Rating Distribution
        Map<Integer, Long> ratingDistribution = reviewService.getRatingDistribution();
        Map<Integer, Double> ratingPercentages = reviewService.getRatingPercentages();

        model.addAttribute("fiveStarCount", ratingDistribution.get(5));
        model.addAttribute("fourStarCount", ratingDistribution.get(4));
        model.addAttribute("threeStarCount", ratingDistribution.get(3));
        model.addAttribute("twoStarCount", ratingDistribution.get(2));
        model.addAttribute("oneStarCount", ratingDistribution.get(1));

        model.addAttribute("fiveStarPercentage", ratingPercentages.get(5));
        model.addAttribute("fourStarPercentage", ratingPercentages.get(4));
        model.addAttribute("threeStarPercentage", ratingPercentages.get(3));
        model.addAttribute("twoStarPercentage", ratingPercentages.get(2));
        model.addAttribute("oneStarPercentage", ratingPercentages.get(1));

        // Review List
        model.addAttribute("reviews", reviewsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewsPage.getTotalPages());

        // Filter parameters
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("rating", rating);

        return "admin/review/index";
    }

    // AJAX Search Reviews
    @GetMapping("/api/search")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @ResponseBody
    public Page<Review> searchReviewsAjax(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return reviewService.searchReviews(keyword, status, rating, pageable);
    }

    // View review details
    @GetMapping("/view/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String view(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Review review = reviewService.getReviewById(id);
            model.addAttribute("review", review);
            return "admin/review/view";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/reviews";
        }
    }

    // Approve a review
    @GetMapping("/approve/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.approveReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đánh giá đã được duyệt thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    // Reject/Unapprove a review
    @GetMapping("/reject/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.rejectReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đánh giá đã bị từ chối!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    // Delete a review
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đánh giá đã được xóa thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

}
