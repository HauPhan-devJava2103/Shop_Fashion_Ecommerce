package vn.web.fashionshop.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.web.fashionshop.dto.ProductForm;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.service.CategoryService;
import vn.web.fashionshop.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping({ "", "/", "/index" })
    public String index(Model model,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "status", required = false) String status) {

        Page<Product> productsPage = productService.searchProductsAdmin(keyword, categoryId, status, page);

        model.addAttribute("products", productsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "admin/product/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product/create";
    }

    @PostMapping("/create")
    public String save(@ModelAttribute("productForm") ProductForm form, RedirectAttributes redirectAttributes, Model model) {
        Product created = productService.create(form);
        if (created == null) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("errorMessage", "Tạo sản phẩm thất bại! Kiểm tra SKU trùng, giá/stock/discount hoặc thiếu ảnh.");
            return "admin/product/create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Tạo sản phẩm thành công!");
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        if (product == null) {
            return "redirect:/admin/products";
        }

        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setSku(product.getSku());
        form.setProductName(product.getProductName());
        form.setDescription(product.getDescription());
        form.setStock(product.getStock());
        form.setPrice(product.getPrice());
        form.setDiscount(product.getDiscount());
        form.setIsActive(product.getIsActive());
        form.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            form.setMainImageUrl(product.getImages().get(0).getUrlImage());
            if (product.getImages().size() > 1)
                form.setExtraImageUrl1(product.getImages().get(1).getUrlImage());
            if (product.getImages().size() > 2)
                form.setExtraImageUrl2(product.getImages().get(2).getUrlImage());
            if (product.getImages().size() > 3)
                form.setExtraImageUrl3(product.getImages().get(3).getUrlImage());
            if (product.getImages().size() > 4)
                form.setExtraImageUrl4(product.getImages().get(4).getUrlImage());
        }

        model.addAttribute("productForm", form);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product/edit";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("productForm") ProductForm form, RedirectAttributes redirectAttributes, Model model) {
        Product updated = productService.update(form);
        if (updated == null) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("errorMessage", "Cập nhật thất bại! Kiểm tra SKU trùng, giá/stock/discount hoặc thiếu ảnh.");
            return "admin/product/edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Soft delete to satisfy BR14 safely (ẩn sản phẩm thay vì xóa vật lý)
        productService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã ẩn sản phẩm!");
        return "redirect:/admin/products";
    }
}
