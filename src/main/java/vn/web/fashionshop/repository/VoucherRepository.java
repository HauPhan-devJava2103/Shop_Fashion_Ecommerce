package vn.web.fashionshop.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

        // Kiểm tra mã voucher tồn tại
        Boolean existsByCode(String code);

        // Đếm tổng Voucher còn hiệu lực
        @Query("SELECT COUNT(v) FROM Voucher v WHERE v.isActive = true")
        Long countByIsActiveTrue();

        // Đếm tổng số lượt đã sử dụng voucher (SUM của usedCount)
        @Query("SELECT COALESCE(SUM(v.usedCount), 0) FROM Voucher v")
        Long countTotalUsed();

        // Đếm tổng voucher hết hạn
        @Query("SELECT COUNT(v) FROM Voucher v WHERE v.endAt < :currentDate")
        Long countByExpired(@Param("currentDate") LocalDateTime currentDate);

        // Tìm kiếm với filter trạng thái
        @Query("SELECT v FROM Voucher v WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR v.code LIKE %:keyword% OR v.description LIKE %:keyword%) "
                        +
                        "AND (:status IS NULL OR :status = '' OR " +
                        "(:status = 'active' AND v.isActive = true) OR " +
                        "(:status = 'inactive' AND v.isActive = false) OR " +
                        "(:status = 'expired' AND v.endAt < :currentDate) OR " +
                        "(:status = 'valid' AND v.isActive = true AND (v.endAt IS NULL OR v.endAt > :currentDate)))")
        Page<Voucher> searchVoucherAdvanced(
                        @Param("keyword") String keyword,
                        @Param("status") String status,
                        @Param("currentDate") LocalDateTime currentDate,
                        Pageable pageable);

}
