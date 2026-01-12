package vn.web.fashionshop.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.web.fashionshop.entity.Payment;
import vn.web.fashionshop.enums.EPaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Tổng doanh thu tất cả (SUCCESS)
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal calculateTotalRevenue(@Param("status") EPaymentStatus status);

    // Doanh thu hôm nay
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startOfDay")
    BigDecimal calculateTodayRevenue(@Param("status") EPaymentStatus status,
            @Param("startOfDay") LocalDateTime startOfDay);

    // Doanh thu theo khoảng thời gian
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate AND p.createdAt <= :endDate")
    BigDecimal calculateRevenueByDateRange(
            @Param("status") EPaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Đếm số đơn thanh toán thành công hôm nay
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startOfDay")
    Long countTodayPayments(@Param("status") EPaymentStatus status, @Param("startOfDay") LocalDateTime startOfDay);

    // Doanh thu theo ngày (cho biểu đồ) - Native SQL
    @Query(value = "SELECT DATE(created_at) as date, COALESCE(SUM(amount), 0) as revenue " +
            "FROM payments " +
            "WHERE status = 'SUCCESS' AND created_at >= :startDate " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date ASC", nativeQuery = true)
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate);

    // Doanh thu tháng (theo khoảng thời gian)
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate AND p.createdAt < :endDate")
    BigDecimal calculateMonthlyRevenue(@Param("status") EPaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Đếm số đơn thanh toán thành công trong khoảng thời gian
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate AND p.createdAt < :endDate")
    Long countPaymentsByDateRange(@Param("status") EPaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Lấy danh sách thanh toán gần đây
    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Payment> findRecentPayments(
            @Param("status") EPaymentStatus status,
            Pageable pageable);

}
