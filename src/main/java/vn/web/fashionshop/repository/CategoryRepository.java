package vn.web.fashionshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.web.fashionshop.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
        long countByIsActiveTrue();

        long countByIsActiveFalse();

        long countByParentCategoryIsNull();

        // Kiểm tra category có tồn tại không
        boolean existsByCategoryName(String categoryName);

        // Kiểm tra slug đã tồn tại
        boolean existsBySlug(String slug);

        // Tìm category theo slug
        Optional<Category> findBySlug(String slug);

        // Tìm tất cả root categories
        List<Category> findByParentCategoryIsNull();

        // Tìm kiếm với filter trạng thái, parent category và phân trang
        @Query("SELECT c FROM Category c LEFT JOIN c.parentCategory p WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR " +
                        "LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(c.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:status IS NULL OR :status = '' OR " +
                        "(:status = 'active' AND c.isActive = true) OR " +
                        "(:status = 'inactive' AND c.isActive = false)) " +
                        "AND (:parentSlug IS NULL OR :parentSlug = '' OR p.slug = :parentSlug)")
        Page<Category> searchCategoryAdvanced(
                        @Param("keyword") String keyword,
                        @Param("status") String status,
                        @Param("parentSlug") String parentSlug,
                        Pageable pageable);

        // Get category performance (product count per category)
        @Query("SELECT c.categoryName, COUNT(p.id) as productCount " +
                        "FROM Category c LEFT JOIN c.products p " +
                        "GROUP BY c.id, c.categoryName " +
                        "ORDER BY productCount DESC")
        List<Object[]> findCategoryPerformance(Pageable pageable);
}
