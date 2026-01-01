package vn.web.fashionshop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.web.fashionshop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    @EntityGraph(attributePaths = { "category", "images" })
    Optional<Product> findById(Long id);

    @EntityGraph(attributePaths = { "category", "images" })
    Optional<Product> findByIdAndIsActiveTrue(Long id);

    @EntityGraph(attributePaths = { "category", "images" })
    @Query("SELECT p FROM Product p WHERE "
            + "(:keyword IS NULL OR :keyword = '' OR p.productName LIKE %:keyword% OR p.sku LIKE %:keyword%) AND "
            + "(:categoryId IS NULL OR p.category.id = :categoryId) AND "
            + "(:isActive IS NULL OR p.isActive = :isActive)")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
