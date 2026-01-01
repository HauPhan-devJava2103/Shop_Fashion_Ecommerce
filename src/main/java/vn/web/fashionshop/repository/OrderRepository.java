package vn.web.fashionshop.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.web.fashionshop.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o")
    BigDecimal sumTotalRevenue();

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.createdAt >= :start and o.createdAt < :end")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
