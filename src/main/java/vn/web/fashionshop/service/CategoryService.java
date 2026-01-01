package vn.web.fashionshop.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import vn.web.fashionshop.dto.CategoryForm;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.repository.CategoryRepository;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public List<Category> getAllCategories() {
		return categoryRepository.findAllByOrderByCategoryNameAsc();
	}

	public Category getById(Long id) {
		if (id == null) {
			return null;
		}
		return categoryRepository.findById(id).orElse(null);
	}

	public CategoryForm toForm(Category category) {
		if (category == null) {
			return null;
		}
		CategoryForm form = new CategoryForm();
		form.setId(category.getId());
		form.setCategoryName(category.getCategoryName());
		form.setSlug(category.getSlug());
		form.setImageUrl(category.getImageUrl());
		form.setIsActive(Boolean.TRUE.equals(category.getIsActive()));
		form.setParentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null);
		return form;
	}

	public Category create(CategoryForm form) {
		Objects.requireNonNull(form, "form");
		Category category = new Category();
		applyForm(category, form);
		category.setCreatedAt(LocalDateTime.now());
		category.setUpdatedAt(null);
		return categoryRepository.save(category);
	}

	public Category update(Long id, CategoryForm form) {
		Objects.requireNonNull(form, "form");
		Category existing = categoryRepository.findById(id).orElse(null);
		if (existing == null) {
			return null;
		}
		applyForm(existing, form);
		existing.setUpdatedAt(LocalDateTime.now());
		return categoryRepository.save(existing);
	}

	public boolean disable(Long id) {
		Category existing = categoryRepository.findById(id).orElse(null);
		if (existing == null) {
			return false;
		}
		existing.setIsActive(false);
		existing.setUpdatedAt(LocalDateTime.now());
		categoryRepository.save(existing);
		return true;
	}

	public boolean enable(Long id) {
		Category existing = categoryRepository.findById(id).orElse(null);
		if (existing == null) {
			return false;
		}
		existing.setIsActive(true);
		existing.setUpdatedAt(LocalDateTime.now());
		categoryRepository.save(existing);
		return true;
	}

	private void applyForm(Category category, CategoryForm form) {
		category.setCategoryName(form.getCategoryName() != null ? form.getCategoryName().trim() : null);
		String slug = (form.getSlug() == null || form.getSlug().isBlank()) ? slugify(form.getCategoryName()) : form.getSlug().trim();
		category.setSlug(slug);
		category.setImageUrl(form.getImageUrl() != null && !form.getImageUrl().isBlank() ? form.getImageUrl().trim() : null);
		category.setIsActive(form.getIsActive() == null ? Boolean.TRUE : form.getIsActive());

		if (form.getParentId() == null) {
			category.setParentCategory(null);
		} else {
			Category parent = categoryRepository.findById(form.getParentId()).orElse(null);
			// Prevent self-parenting in update scenarios
			if (parent != null && (category.getId() == null || !parent.getId().equals(category.getId()))) {
				category.setParentCategory(parent);
			} else {
				category.setParentCategory(null);
			}
		}
	}

	private String slugify(String input) {
		if (input == null) {
			return "";
		}
		String s = input.trim().toLowerCase(Locale.ROOT);
		s = s.replaceAll("[^a-z0-9\\s-]", "");
		s = s.replaceAll("\\s+", "-");
		s = s.replaceAll("-+", "-");
		return s;
	}
}
