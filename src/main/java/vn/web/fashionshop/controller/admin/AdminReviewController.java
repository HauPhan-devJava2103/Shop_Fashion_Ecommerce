package vn.web.fashionshop.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.web.fashionshop.entity.Review;
import vn.web.fashionshop.repository.ReviewRepository;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewRepository reviewRepository;

    public AdminReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            Model model) {

        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", Math.max(1, reviews.getTotalPages()));
        model.addAttribute("size", size);
        return "admin/review/index";
    }
}
