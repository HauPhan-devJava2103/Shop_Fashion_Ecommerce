package vn.web.fashionshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.WishlistItem;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @Query("SELECT wi FROM WishlistItem wi JOIN FETCH wi.product p WHERE wi.user.email = :email ORDER BY wi.createdAt DESC")
    List<WishlistItem> findByUserEmailWithProduct(@Param("email") String email);

    Optional<WishlistItem> findByUser_EmailAndProduct_Id(String email, Long productId);

    long countByUser_Email(String email);

    @Query("SELECT wi.product.id FROM WishlistItem wi WHERE wi.user.email = :email AND wi.product.id IN :productIds")
    List<Long> findLikedProductIds(@Param("email") String email, @Param("productIds") List<Long> productIds);
}
