package vn.web.fashionshop.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.Order;
import vn.web.fashionshop.enums.EOrderStatus;
import vn.web.fashionshop.enums.EPaymentMethod;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

        // Tổng đơn hàng
        @Query("SELECT COUNT(o) FROM Order o")
        Long countAll();

        // Tổng đơn chờ Pending
        @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'PENDING'")
        Long countPending();

        // Tổng đơn đang giao hàng
        @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'SHIPPED'")
        Long countProcessing();

        // Tổng doanh thu - Revenue
        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus = 'COMPLETED'")
        BigDecimal countRevenue();

        // Tổng các trạng thái xử lý đơn
        @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
        Long countByStatus(@Param("status") EOrderStatus status);

        // Tính doanh thu theo ngày
        @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderStatus = 'COMPLETED' AND DATE(o.createdAt) = :date")
        BigDecimal getRevenueByDate(@Param("date") LocalDate date);

        // Đếm số đơn hàng theo ngày (cho Order Trends chart)
        @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = :date")
        Long countByDate(@Param("date") LocalDate date);

        // Tìm kiếm đơn hàng với filter: keyword, status, payment method, date range
        @Query("SELECT o FROM Order o LEFT JOIN o.user u WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR " +
                        "CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%') OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:status IS NULL OR o.orderStatus = :status) " +
                        "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
                        "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
                        "AND (:endDate IS NULL OR o.createdAt <= :endDate) " +
                        "ORDER BY o.createdAt DESC")
        Page<Order> searchOrders(
                        @Param("keyword") String keyword,
                        @Param("status") EOrderStatus status,
                        @Param("paymentMethod") EPaymentMethod paymentMethod,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        // Lấy tất cả đơn hàng với phân trang
        Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
