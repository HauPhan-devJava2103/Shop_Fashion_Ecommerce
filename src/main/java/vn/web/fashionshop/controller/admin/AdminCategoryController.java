package vn.web.fashionshop.controller.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.web.fashionshop.dto.CategoryForm;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String index(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/category/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "admin/category/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("categoryForm") CategoryForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("allCategories", categoryService.getAllCategories());
            return "admin/category/create";
        }

        categoryService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Đã tạo danh mục");
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Category category = categoryService.getById(id);
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục");
            return "redirect:/admin/categories";
        }

        model.addAttribute("categoryForm", categoryService.toForm(category));
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "admin/category/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id,
            @Valid @ModelAttribute("categoryForm") CategoryForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("allCategories", categoryService.getAllCategories());
            return "admin/category/edit";
        }

        Category updated = categoryService.update(id, form);
        if (updated == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục");
            return "redirect:/admin/categories";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật danh mục");
        return "redirect:/admin/categories";
    }

    @GetMapping("/disable/{id}")
    public String disable(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean ok = categoryService.disable(id);
        redirectAttributes.addFlashAttribute(ok ? "successMessage" : "errorMessage", ok ? "Đã ẩn danh mục" : "Không tìm thấy danh mục");
        return "redirect:/admin/categories";
    }

    @GetMapping("/enable/{id}")
    public String enable(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean ok = categoryService.enable(id);
        redirectAttributes.addFlashAttribute(ok ? "successMessage" : "errorMessage", ok ? "Đã bật danh mục" : "Không tìm thấy danh mục");
        return "redirect:/admin/categories";
    }
}
