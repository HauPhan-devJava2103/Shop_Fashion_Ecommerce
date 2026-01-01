package vn.web.fashionshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.web.fashionshop.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
