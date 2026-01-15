package vn.web.fashionshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

	List<ProductVariant> findByProductIdOrderByIdAsc(Long productId);

	@Query("SELECT DISTINCT v FROM ProductVariant v " +
			"JOIN FETCH v.product p " +
			"LEFT JOIN FETCH p.images img " +
			"WHERE v.id IN :ids")
	java.util.List<ProductVariant> findByIdInWithProductAndImages(@Param("ids") java.util.List<Long> ids);
}
