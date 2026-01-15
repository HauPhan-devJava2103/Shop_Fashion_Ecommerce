package vn.web.fashionshop.controller.admin.category;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import vn.web.fashionshop.dto.CategoryDTO;
import vn.web.fashionshop.dto.ChartResponse;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String index(Model model,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String parentSlug) {

        // Get paginated categories
        Page<Category> categoriesPage = categoryService.searchCategoryAdvanced(keyword, status, parentSlug, page);

        // Stats
        model.addAttribute("totalCategories", categoryService.countTotal());
        model.addAttribute("activeCategories", categoryService.countActive());
        model.addAttribute("hiddenCategories", categoryService.countHidden());
        model.addAttribute("rootCategories", categoryService.countRoots());

        // Pagination data
        model.addAttribute("categories", categoriesPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoriesPage.getTotalPages());

        // Root categories for filter dropdown
        model.addAttribute("rootCategoriesList", categoryService.getRootCategories());

        // Keep filter params for form
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("parentSlug", parentSlug);

        return "admin/category/index";
    }

    // Search category using AJAX
    @GetMapping("/api/search")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public Page<Category> searchCategoriesAjax(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "parentSlug", required = false) String parentSlug) {

        return categoryService.searchCategoryAdvanced(keyword, status, parentSlug, page);
    }

    // Category Performance Chart
    @GetMapping("/api/stats/performance")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ChartResponse getCategoryPerformance(
            @RequestParam(name = "limit", defaultValue = "5") Integer limit) {

        List<Object[]> data = categoryService.getCategoryPerformance(limit);

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : data) {
            labels.add((String) row[0]); // categoryName
            values.add((Long) row[1]); // productCount
        }
        return new ChartResponse(labels, values);
    }

    @GetMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String create(Model model) {
        model.addAttribute("category", new CategoryDTO());
        model.addAttribute("categories", categoryService.getAll()); // For parent category selection
        return "admin/category/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String create(@Valid @ModelAttribute("category") CategoryDTO categoryDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Kiểm tra lỗi validation từ DTO
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", categoryDTO);
            model.addAttribute("categories", categoryService.getAll());
            return "admin/category/create";
        }

        // Tạo Category
        try {
            Category createdCategory = categoryService.create(categoryDTO);

            if (createdCategory == null) {
                model.addAttribute("errorMessage", "Ten danh muc hoac slug da ton tai!");
                model.addAttribute("category", categoryDTO);
                model.addAttribute("categories", categoryService.getAll());
                return "admin/category/create";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Tao danh muc thanh cong!");
            return "redirect:/admin/categories";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("category", categoryDTO);
            model.addAttribute("categories", categoryService.getAll());
            return "admin/category/create";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(id);

            // Convert Category to CategoryDTO for form binding
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(category.getId());
            categoryDTO.setCategoryName(category.getCategoryName());
            categoryDTO.setSlug(category.getSlug());
            categoryDTO.setImageUrl(category.getImageUrl());
            categoryDTO.setIsActive(category.getIsActive());
            categoryDTO.setParentCategory(category.getParentCategory());

            model.addAttribute("category", categoryDTO);
            model.addAttribute("categories", categoryService.getAll());
            return "admin/category/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay danh muc!");
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("category") CategoryDTO categoryDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        categoryDTO.setId(id);

        // Validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("category", categoryDTO);
            model.addAttribute("categories", categoryService.getAll());
            return "admin/category/edit";
        }

        try {
            Category updatedCategory = categoryService.update(categoryDTO);

            if (updatedCategory == null) {
                model.addAttribute("errorMessage", "Cap nhat that bai!");
                model.addAttribute("category", categoryDTO);
                model.addAttribute("categories", categoryService.getAll());
                return "admin/category/edit";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat danh muc thanh cong!");
            return "redirect:/admin/categories";

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("category", categoryDTO);
            model.addAttribute("categories", categoryService.getAll());
            return "admin/category/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa danh mục");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        model.addAttribute("category", category);
        return "admin/category/view";
    }

}
