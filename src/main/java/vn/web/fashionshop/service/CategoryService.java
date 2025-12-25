package vn.web.fashionshop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        if (categoryRepository.findById(id).isPresent()) {
            categoryRepository.deleteById(id);
        } else {
            throw new RuntimeException("Category not found");
        }
    }

    public long countTotal() {
        return categoryRepository.count();
    }

    public long countActive() {
        return categoryRepository.countByIsActiveTrue();
    }

    public long countHidden() {
        return categoryRepository.countByIsActiveFalse();
    }

    public long countRoots() {
        return categoryRepository.countByParentCategoryIsNull();
    }
}
