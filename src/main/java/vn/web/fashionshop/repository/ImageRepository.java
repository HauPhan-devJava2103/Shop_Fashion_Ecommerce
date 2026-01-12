package vn.web.fashionshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByProductId(Long productId);

    void deleteByProductIdAndId(Long productId, Long imageId);
}
