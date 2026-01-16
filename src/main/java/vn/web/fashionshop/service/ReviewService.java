package vn.web.fashionshop.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.web.fashionshop.entity.OrderItem;
import vn.web.fashionshop.entity.Review;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.enums.EOrderStatus;
import vn.web.fashionshop.repository.OrderItemRepository;
import vn.web.fashionshop.repository.ReviewRepository;
import vn.web.fashionshop.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    // Stats Widget Methods

    // Total Reviews
    public long getTotalReviews() {
        return reviewRepository.count();
    }

    // Pending Reviews
    public long getPendingReviews() {
        return reviewRepository.countByIsApproved(false);
    }

    // Approved Reviews
    public long getApprovedReviews() {
        return reviewRepository.countByIsApproved(true);
    }

    // Average Rating
    public double getAverageRating() {
        Double avg = reviewRepository.getAverageRatingAll();
        return avg != null ? avg : 0.0;
    }

    // Get average rating from approved reviews only
    public double getApprovedAverageRating() {
        Double avg = reviewRepository.getAverageRating();
        return avg != null ? avg : 0.0;
    }

    // Get number of reviews created this month
    public long getReviewsThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(
                LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        return reviewRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
    }

    // Get number of reviews created today
    public long getReviewsToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return reviewRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    // Get number of reviews with images
    public long getReviewsWithImage() {
        return reviewRepository.countByImageUrlIsNotNull();
    }

    // Get approval rate percentage
    public double getApprovalRate() {
        long total = getTotalReviews();
        if (total == 0) {
            return 0.0;
        }
        long approved = getApprovedReviews();
        return (approved * 100.0) / total;
    }

    // Get rating distribution (number of reviews per rating)
    public Map<Integer, Long> getRatingDistribution() {
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, reviewRepository.countByRatingValue(i));
        }
        return distribution;
    }

    // Get percentage for each rating level
    public Map<Integer, Double> getRatingPercentages() {
        Map<Integer, Double> percentages = new HashMap<>();
        long total = getTotalReviews();

        if (total == 0) {
            for (int i = 1; i <= 5; i++) {
                percentages.put(i, 0.0);
            }
            return percentages;
        }

        Map<Integer, Long> distribution = getRatingDistribution();
        for (int i = 1; i <= 5; i++) {
            double percentage = (distribution.get(i) * 100.0) / total;
            percentages.put(i, percentage);
        }

        return percentages;
    }

    // CRUD

    // Get all reviews with pagination
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    // Search reviews with filters
    public Page<Review> searchReviews(String keyword, String status, Integer rating, Pageable pageable) {
        return reviewRepository.searchReviews(keyword, status, rating, pageable);
    }

    // Get review by ID
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }

    // Get reviews by product
    public Page<Review> getReviewsByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable);
    }

    // Get reviews by user
    public Page<Review> getReviewsByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable);
    }

    // Approve a review
    @Transactional
    public Review approveReview(Long id) {
        Review review = getReviewById(id);
        review.setIsApproved(true);
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // Reject/Unapprove a review
    @Transactional
    public Review rejectReview(Long id) {
        Review review = getReviewById(id);
        review.setIsApproved(false);
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    // Delete a review
    @Transactional
    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        reviewRepository.delete(review);
    }

    // Save or update a review
    @Transactional
    public Review saveReview(Review review) {
        if (review.getId() == null) {
            review.setCreatedAt(LocalDateTime.now());
        } else {
            review.setUpdatedAt(LocalDateTime.now());
        }
        return reviewRepository.save(review);
    }

    // CUSTOMER REVIEW METHODS

    // Create review from customer
    @Transactional
    public Review createCustomerReview(String userEmail, Long orderItemId, Integer rating, String comment,
            String imageUrl) {
        // Validate rating
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5 sao");
        }

        // Find user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("USER_NOT_FOUND"));

        // Find orderItem and validate
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("ORDER_ITEM_NOT_FOUND"));

        // Check order belongs to user
        if (orderItem.getOrder() == null ||
                orderItem.getOrder().getUser() == null ||
                !userEmail.equals(orderItem.getOrder().getUser().getEmail())) {
            throw new IllegalArgumentException("Bạn không có quyền đánh giá sản phẩm này");
        }

        // Check order is COMPLETED
        if (orderItem.getOrder().getOrderStatus() != EOrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Chỉ có thể đánh giá khi đơn hàng đã hoàn thành");
        }

        // Check not already reviewed
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new IllegalArgumentException("Sản phẩm này đã được đánh giá");
        }

        // Get product from variant
        if (orderItem.getVariant() == null || orderItem.getVariant().getProduct() == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm");
        }

        // Create review
        Review review = new Review();
        review.setOrderItem(orderItem);
        review.setProduct(orderItem.getVariant().getProduct());
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment);
        review.setImageUrl(imageUrl);
        review.setIsApproved(false); // Cần admin duyệt
        review.setCreatedAt(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    // Get my reviews
    public List<Review> getMyReviews(String userEmail) {
        return reviewRepository.findByUserEmailWithDetails(userEmail);
    }

    // Get approved reviews for a product (for product detail page)
    public List<Review> getApprovedProductReviews(Long productId) {
        return reviewRepository.findApprovedByProductIdWithUser(productId);
    }

    // Get product review stats
    public ProductReviewStats getProductReviewStats(Long productId) {
        long count = reviewRepository.countApprovedByProductId(productId);
        Double avgRating = reviewRepository.getApprovedAverageRatingByProductId(productId);
        return new ProductReviewStats(count, avgRating != null ? avgRating : 0.0);
    }

    // Check if orderItem can be reviewed
    public boolean canReview(String userEmail, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElse(null);
        if (orderItem == null)
            return false;
        if (orderItem.getOrder() == null)
            return false;
        if (!userEmail.equals(orderItem.getOrder().getUser().getEmail()))
            return false;
        if (orderItem.getOrder().getOrderStatus() != EOrderStatus.COMPLETED)
            return false;
        if (reviewRepository.existsByOrderItemId(orderItemId))
            return false;
        return true;
    }

    // Record for product review stats
    public record ProductReviewStats(long reviewCount, double averageRating) {
    }
}
