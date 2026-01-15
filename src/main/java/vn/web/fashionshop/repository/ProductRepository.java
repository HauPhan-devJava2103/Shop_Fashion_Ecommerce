package vn.web.fashionshop.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

        // Tổng sản phẩm của shop
        @Query("SELECT COUNT(p) FROM Product p")
        Long countAll();

        // Tổng sản phẩm còn tồn kho
        @Query("SELECT COUNT(p) FROM Product p WHERE p.stock > 0")
        Long countInStock();

        // Tổng sản phẩm hêt tồn kho
        @Query("SELECT COUNT(p) FROM Product p WHERE p.stock = 0")
        Long countOutOfStock();

        // Tổng giá trị hàng tồn kho
        @Query("SELECT SUM(p.price * p.stock) FROM Product p")
        Long totalValueInStock();

        // Thống kê sản phẩm theo danh mục (cho pie chart)
        @Query("SELECT c.categoryName, COUNT(p) FROM Product p JOIN p.category c GROUP BY c.id, c.categoryName ORDER BY COUNT(p) DESC")
        List<Object[]> getProductCountByCategory();

        // Đếm số sản phẩm theo category ID (bao gồm cả danh mục con)
        @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId OR p.category.parentCategory.id = :categoryId")
        Long countByCategoryId(@Param("categoryId") Long categoryId);

        // Tìm kiếm product với filter keyword, category, stock, isActive và phân trang
        @Query("SELECT p FROM Product p LEFT JOIN p.category c WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR " +
                        "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:categoryId IS NULL OR c.id = :categoryId) " +
                        "AND (:stock IS NULL OR :stock = '' OR " +
                        "(:stock = 'in_stock' AND p.stock > 0) OR " +
                        "(:stock = 'out_of_stock' AND p.stock = 0)) " +
                        "AND (:sku IS NULL OR :sku = '' OR " +
                        "LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) " +
                        "AND (:isActive IS NULL OR :isActive = '' OR " +
                        "(:isActive = 'true' AND p.isActive = true) OR " +
                        "(:isActive = 'false' AND p.isActive = false))")
        Page<Product> searchProductAdvanced(
                        @Param("keyword") String keyword,
                        @Param("categoryId") Long categoryId,
                        @Param("stock") String stock,
                        @Param("sku") String sku,
                        @Param("isActive") String isActive,
                        Pageable pageable);

        // Lấy sản phẩm mới nhất theo category slug (cho trang chủ)
        @Query("SELECT p FROM Product p JOIN p.category c WHERE " +
                        "(c.slug = :categorySlug OR c.parentCategory.slug = :categorySlug) " +
                        "AND p.isActive = true " +
                        "ORDER BY p.createdAt DESC")
        List<Product> findNewArrivalsByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

        // Lấy tất cả sản phẩm active theo root category slug (bao gồm danh mục con)
        @Query("SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.category c " +
                        "LEFT JOIN c.parentCategory pc " +
                        "LEFT JOIN FETCH p.images imgs " +
                        "WHERE (c.slug = :rootSlug OR pc.slug = :rootSlug) " +
                        "AND p.isActive = true " +
                        "ORDER BY p.createdAt DESC")
        List<Product> findActiveByRootCategorySlug(@Param("rootSlug") String rootSlug);

        // Lọc theo màu (variant.color) và khoảng giá (giá sau giảm nếu có)
        @Query("SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.category c " +
                        "LEFT JOIN c.parentCategory pc " +
                        "LEFT JOIN FETCH p.images imgs " +
                        "LEFT JOIN p.variants v " +
                        "WHERE (c.slug = :rootSlug OR pc.slug = :rootSlug) " +
                        "AND p.isActive = true " +
                        "AND (:colors IS NULL OR LOWER(v.color) IN :colors) " +
                        "AND (:minPrice IS NULL OR (p.price - (p.price * COALESCE(p.discount, 0) / 100)) >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR (p.price - (p.price * COALESCE(p.discount, 0) / 100)) <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC")
        List<Product> findActiveByRootCategorySlugFiltered(
                        @Param("rootSlug") String rootSlug,
                        @Param("colors") List<String> colors,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice);

        // Lọc theo màu + giá cho nhiều root slug (men/women/accessories)
        @Query("SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.category c " +
                        "LEFT JOIN c.parentCategory pc " +
                        "LEFT JOIN FETCH p.images imgs " +
                        "LEFT JOIN p.variants v " +
                        "WHERE (c.slug IN :rootSlugs OR pc.slug IN :rootSlugs) " +
                        "AND p.isActive = true " +
                        "AND (:colors IS NULL OR LOWER(v.color) IN :colors) " +
                        "AND (:minPrice IS NULL OR (p.price - (p.price * COALESCE(p.discount, 0) / 100)) >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR (p.price - (p.price * COALESCE(p.discount, 0) / 100)) <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC")
        List<Product> findActiveByRootCategorySlugsFiltered(
                        @Param("rootSlugs") List<String> rootSlugs,
                        @Param("colors") List<String> colors,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice);

        // Lấy tất cả sản phẩm active theo 1 category slug (bao gồm danh mục con nếu có)
        @Query("SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.category c " +
                        "LEFT JOIN c.parentCategory pc " +
                        "LEFT JOIN FETCH p.images imgs " +
                        "WHERE (c.slug = :categorySlug OR pc.slug = :categorySlug) " +
                        "AND p.isActive = true " +
                        "ORDER BY p.createdAt DESC")
        List<Product> findActiveByCategorySlug(@Param("categorySlug") String categorySlug);

        // Lấy tất cả sản phẩm active theo nhiều root category slug (bao gồm danh mục con)
        @Query("SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.category c " +
                        "LEFT JOIN c.parentCategory pc " +
                        "LEFT JOIN FETCH p.images imgs " +
                        "WHERE (c.slug IN :rootSlugs OR pc.slug IN :rootSlugs) " +
                        "AND p.isActive = true " +
                        "ORDER BY p.createdAt DESC")
        List<Product> findActiveByRootCategorySlugs(@Param("rootSlugs") List<String> rootSlugs);

        // Lọc theo màu (variant.color) và khoảng giá (giá sau giảm nếu có)
        @Query("SELECT DISTINCT p FROM Product p " +
                        "JOIN FETCH p.category c " +
                        "LEFT JOIN c.parentCategory pc " +
                        "LEFT JOIN FETCH p.images imgs " +
                        "LEFT JOIN p.variants v " +
                        "WHERE (c.slug = :categorySlug OR pc.slug = :categorySlug) " +
                        "AND p.isActive = true " +
                        "AND (:colors IS NULL OR LOWER(v.color) IN :colors) " +
                        "AND (:minPrice IS NULL OR (p.price - (p.price * COALESCE(p.discount, 0) / 100)) >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR (p.price - (p.price * COALESCE(p.discount, 0) / 100)) <= :maxPrice) " +
                        "ORDER BY p.createdAt DESC")
        List<Product> findActiveByCategorySlugFiltered(
                        @Param("categorySlug") String categorySlug,
                        @Param("colors") List<String> colors,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice);

        @Query("SELECT p FROM Product p " +
                        "LEFT JOIN FETCH p.variants v " +
                        "WHERE p.id = :id")
        java.util.Optional<Product> findByIdWithVariants(@Param("id") Long id);

        @Query("SELECT p FROM Product p " +
                        "LEFT JOIN FETCH p.category c " +
                        "WHERE p.id = :id")
        java.util.Optional<Product> findByIdForDetail(@Param("id") Long id);

}
