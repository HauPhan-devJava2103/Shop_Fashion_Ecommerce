package vn.web.fashionshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.web.fashionshop.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
