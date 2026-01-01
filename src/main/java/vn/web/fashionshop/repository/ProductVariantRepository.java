package vn.web.fashionshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.web.fashionshop.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    boolean existsBySkuVariant(String skuVariant);

    boolean existsByProduct_Id(Long productId);
}
