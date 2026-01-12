package vn.web.fashionshop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.web.fashionshop.dto.CategoryDTO;
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

    public List<Category> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull();
    }

    public Page<Category> searchCategoryAdvanced(String keyword, String status, String parentSlug, int pageNo) {
        int pageSize = 6;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        return categoryRepository.searchCategoryAdvanced(keyword, status, parentSlug, pageable);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public void delete(Long id) {
        // Kiểm tra category có tồn tại không
        Category category = findById(id);

        // Kiểm tra có children không
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục có danh mục con");
        }

        // Kiểm tra có products không
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục đang chứa sản phẩm");
        }

        categoryRepository.deleteById(id);
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

    /**
     * Get category performance data (product count per category)
     * Returns top categories ordered by product count
     */
    public List<Object[]> getCategoryPerformance(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return categoryRepository.findCategoryPerformance(pageable);
    }

    public Category create(CategoryDTO categoryDTO) {
        // Validation: Kiểm tra tên category đã tồn tại
        if (categoryRepository.existsByCategoryName(categoryDTO.getCategoryName())) {
            return null;
        }

        // Validation: Kiểm tra slug đã tồn tại
        if (categoryRepository.existsBySlug(categoryDTO.getSlug())) {
            return null;
        }

        // Validation: Parent category nếu có
        Long parentId = categoryDTO.getParentCategory() != null ? categoryDTO.getParentCategory().getId() : null;
        if (parentId != null) {
            validateParentCategory(null, parentId);
        }

        Category category = categoryDTOToCategory(categoryDTO);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    public Category update(CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(categoryDTO.getId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryDTO.getId()));

        // Validate parent category if changing
        Long newParentId = categoryDTO.getParentCategory() != null ? categoryDTO.getParentCategory().getId() : null;
        validateParentCategory(categoryDTO.getId(), newParentId);

        category.setCategoryName(categoryDTO.getCategoryName());
        category.setSlug(categoryDTO.getSlug());
        category.setImageUrl(categoryDTO.getImageUrl());
        category.setIsActive(categoryDTO.getIsActive() != null ? categoryDTO.getIsActive() : true);

        // Fetch parent from database instead of using transient object
        if (newParentId != null) {
            Category parentCategory = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + newParentId));
            category.setParentCategory(parentCategory);
        } else {
            // Set to null to make it a root category
            category.setParentCategory(null);
        }

        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    /**
     * Convert CategoryDTO sang Category entity
     */
    public Category categoryDTOToCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setCategoryName(categoryDTO.getCategoryName());
        category.setSlug(categoryDTO.getSlug());
        category.setImageUrl(categoryDTO.getImageUrl());
        category.setIsActive(categoryDTO.getIsActive() != null ? categoryDTO.getIsActive() : true);

        if (categoryDTO.getParentCategory() != null && categoryDTO.getParentCategory().getId() != null) {
            Long parentId = categoryDTO.getParentCategory().getId();
            Category parentCategory = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent category not found with id: " + parentId));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }

        return category;
    }

    /**
     * Validate parent category để tránh:
     * - Self-parent (category trỏ vào chính nó)
     * - Circular reference (vòng lặp A→B→C→A)
     * - Parent không tồn tại
     */
    private void validateParentCategory(Long categoryId, Long parentId) {
        // 1. Root category
        if (parentId == null) {
            return;
        }

        // 2. Không thể set chính nó làm parent
        if (categoryId != null && categoryId.equals(parentId)) {
            throw new RuntimeException("Category không thể là parent của chính nó");
        }

        // 3. Parent phải tồn tại trong database
        if (!categoryRepository.existsById(parentId)) {
            throw new RuntimeException("Parent category không tồn tại");
        }

        // 4. Kiểm tra circular reference (vòng lặp)
        if (categoryId != null && hasCircularReference(categoryId, parentId)) {
            throw new RuntimeException("Không thể tạo vòng lặp parent-child");
        }
    }

    /**
     * Kiểm tra circular reference bằng cách duyệt cây parent
     * VD: A → B → C, không cho C.parent = A (tạo vòng lặp)
     */
    private boolean hasCircularReference(Long categoryId, Long parentId) {
        Category parent = categoryRepository.findById(parentId).orElse(null);

        // Duyệt từ parent lên đến root
        while (parent != null) {
            if (parent.getId().equals(categoryId)) {
                return true; // Tìm thấy vòng lặp!
            }
            parent = parent.getParentCategory(); // Lên 1 cấp
        }

        return false; // Không có vòng lặp
    }

}
