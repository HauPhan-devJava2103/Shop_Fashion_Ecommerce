package vn.web.fashionshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import vn.web.fashionshop.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findAllByOrderByCategoryNameAsc();

	boolean existsByCategoryNameIgnoreCase(String categoryName);

	boolean existsBySlugIgnoreCase(String slug);

}
