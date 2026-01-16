package vn.web.fashionshop.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

        // Count queries for stats widgets
        long countByIsApproved(Boolean isApproved);

        long countByRating(Integer rating);

        long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        long countByImageUrlIsNotNull();

        // Average rating calculation
        @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.isApproved = true")
        Double getAverageRating();

        // Average rating from all reviews
        @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r")
        Double getAverageRatingAll();

        // Rating distribution counts
        @Query("SELECT COUNT(r) FROM Review r WHERE r.rating = :rating")
        long countByRatingValue(@Param("rating") Integer rating);

        // Search and filter queries
        @Query("SELECT r FROM Review r " +
                        "WHERE (:keyword IS NULL OR :keyword = '' OR " +
                        "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(r.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(r.product.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:status IS NULL OR :status = '' OR " +
                        "(CASE WHEN :status = 'approved' THEN r.isApproved = true " +
                        "WHEN :status = 'pending' THEN r.isApproved = false END)) " +
                        "AND (:rating IS NULL OR r.rating = :rating)")
        Page<Review> searchReviews(
                        @Param("keyword") String keyword,
                        @Param("status") String status,
                        @Param("rating") Integer rating,
                        Pageable pageable);

        // Get reviews by approval status with pagination
        Page<Review> findByIsApproved(Boolean isApproved, Pageable pageable);

        // Get reviews by product
        Page<Review> findByProductId(Long productId, Pageable pageable);

        @Query("SELECT r FROM Review r " +
                        "JOIN FETCH r.user u " +
                        "WHERE r.product.id = :productId AND r.isApproved = true " +
                        "ORDER BY r.createdAt DESC")
        List<Review> findApprovedByProductIdWithUser(@Param("productId") Long productId);

        @Query("SELECT r.product.id, COALESCE(AVG(r.rating), 0.0) " +
                        "FROM Review r " +
                        "WHERE r.isApproved = true AND r.product.id IN :productIds " +
                        "GROUP BY r.product.id")
        List<Object[]> findAverageRatingForProductIds(@Param("productIds") List<Long> productIds);

        // Get reviews by user
        Page<Review> findByUserId(Long userId, Pageable pageable);

        // Check if review exists for orderItem
        boolean existsByOrderItemId(Long orderItemId);

        // Find reviews by user email
        @Query("SELECT r FROM Review r JOIN FETCH r.product p JOIN FETCH r.orderItem oi " +
                        "WHERE r.user.email = :email ORDER BY r.createdAt DESC")
        List<Review> findByUserEmailWithDetails(@Param("email") String email);

        // Count approved reviews for a product
        @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
        long countApprovedByProductId(@Param("productId") Long productId);

        // Get approved average rating for a product
        @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
        Double getApprovedAverageRatingByProductId(@Param("productId") Long productId);
}
