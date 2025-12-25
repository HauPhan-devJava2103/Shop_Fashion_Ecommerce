package vn.web.fashionshop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        // Tìm user theo email
        Optional<User> findByEmail(String email);

        Boolean existsByEmail(String email);

        Boolean existsByPhone(String phone);

        Optional<User> findByPhone(String phone);

        // Đếm user đang hoạt động
        Long countByIsActiveTrue();

        // Đếm user có trong tháng hiện tại
        @Query("SELECT COUNT(u) FROM User u WHERE MONTH(u.createdAt) = MONTH(CURRENT_DATE()) AND YEAR(u.createdAt) = YEAR(CURRENT_DATE())")
        Long countUserThisMonth();

        // Đếm số lượng User thêm vào theo từng ngày (Native SQL for MySQL)
        @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as count " +
                        "FROM users " +
                        "WHERE created_at >= :startDate " +
                        "GROUP BY DATE(created_at) " +
                        "ORDER BY DATE(created_at)", nativeQuery = true)
        List<Object[]> countUserByDateRange(@Param("startDate") LocalDateTime startDate);

        @Query("SELECT r.roleName, COUNT(u) FROM User u JOIN u.role r GROUP BY r.roleName")
        List<Object[]> countUsersByRole();

        // Tìm kiếm user phân trang
        @Query("SELECT u FROM User u WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR u.fullName LIKE %:keyword% OR u.email LIKE %:keyword% OR u.phone LIKE %:keyword%) AND "
                        +
                        "(:roleId IS NULL OR u.role.id = :roleId) AND " +
                        "(:status IS NULL OR u.isActive = :status)")
        Page<User> searchUsers(@Param("keyword") String keyword,
                        @Param("roleId") Long roleId,
                        @Param("status") Boolean status,
                        Pageable pageable);
}
