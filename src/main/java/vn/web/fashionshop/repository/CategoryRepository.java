package vn.web.fashionshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.web.fashionshop.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
