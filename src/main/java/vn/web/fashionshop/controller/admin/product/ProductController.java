package vn.web.fashionshop.controller.admin.product;

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
import vn.web.fashionshop.dto.ChartResponse;
import vn.web.fashionshop.dto.ProductCreateDTO;
import vn.web.fashionshop.dto.ProductUpdateDTO;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.service.CategoryService;
import vn.web.fashionshop.service.ProductImageService;
import vn.web.fashionshop.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductImageService productImageService;

    public ProductController(ProductService productService,
            CategoryService categoryService,
            ProductImageService productImageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productImageService = productImageService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String index(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String stock,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String isActive,
            Model model) {

        // Add statistics to model
        model.addAttribute("totalProducts", productService.countAllProduct());
        model.addAttribute("productsInStock", productService.countInStockProduct());
        model.addAttribute("productsOutOfStock", productService.countOutOfStockProduct());
        model.addAttribute("totalInventoryValue", productService.totalValueInStock());

        // Get product list with pagination
        Page<Product> productPage = productService.searchProductAdvanced(keyword, categoryId, stock, sku, isActive,
                page);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());

        // Filter values
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("stock", stock);
        model.addAttribute("sku", sku);
        model.addAttribute("isActive", isActive);

        // Categories for filter dropdown
        model.addAttribute("categories", categoryService.getAll());

        return "admin/product/index";
    }

    // Search product using AJAX
    @GetMapping("/api/search")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public java.util.Map<String, Object> searchProductsAjax(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String stock,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String isActive) {

        return productService.searchProductsForAjax(keyword, categoryId, stock, sku, isActive, page);
    }

    // API endpoint for pie chart data
    @GetMapping("/api/stats/category-distribution")
    @ResponseBody
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ChartResponse getProductCategoryDistribution() {
        List<Object[]> data = productService.getProductCountByCategory();

        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : data) {
            labels.add((String) row[0]); // categoryName
            values.add((Long) row[1]); // productCount
        }

        return new ChartResponse(labels, values);
    }

    // View Product Details
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/product/view";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String createProductForm(Model model) {
        List<Category> categories = categoryService.getAll();
        model.addAttribute("categories", categories);
        model.addAttribute("product", new ProductCreateDTO());
        return "admin/product/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String createProduct(
            @Valid @ModelAttribute ProductCreateDTO productDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (productDTO.getVariants() != null && !productDTO.getVariants().isEmpty()) {
            for (int i = 0; i < productDTO.getVariants().size(); i++) {
                var v = productDTO.getVariants().get(i);
                System.out.println("  [" + i + "] Variant SKU: " + v.getSkuVariant() + ", Stock: " + v.getStock());
            }
        }

        // Validate form
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("  - " + error.getDefaultMessage());
            });
            model.addAttribute("categories", categoryService.getAll());
            return "admin/product/create";
        }

        try {
            Product product = productService.createProduct(productDTO);
            redirectAttributes.addFlashAttribute("success", "Tạo sản phẩm mới thành công!");
            return "redirect:/admin/products/" + product.getId();
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/products/create";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String editProductForm(@PathVariable Long id, Model model) {
        // Get product with all relationships
        Product product = productService.getProductById(id);

        // Get all categories for dropdown
        List<Category> categories = categoryService.getAll();

        // Add to model
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);

        return "admin/product/edit";
    }

    // Update Product POST
    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductUpdateDTO productDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (productDTO.getVariants() != null && !productDTO.getVariants().isEmpty()) {
            System.out.println("=== VARIANT DETAILS ===");
            for (int i = 0; i < productDTO.getVariants().size(); i++) {
                var v = productDTO.getVariants().get(i);
            }
        }

        // Validate form
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> {
                System.out.println("  - " + error.getDefaultMessage());
            });
            model.addAttribute("product", productService.getProductById(id));
            model.addAttribute("categories", categoryService.getAll());
            return "admin/product/edit";
        }

        try {
            productService.updateProduct(id, productDTO);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công!");
            return "redirect:/admin/products/" + id;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    @PostMapping("/edit/{id}/upload-main-image")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String uploadMainImageFromEdit(
            @PathVariable Long id,
            @RequestParam("imageFile") org.springframework.web.multipart.MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        try {
            productImageService.uploadMainImage(id, imageFile);
            redirectAttributes.addFlashAttribute("success", "Upload ảnh thành công và đã đặt làm ảnh chính!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/products/edit/" + id;
    }

    // ==================== IMAGE MANAGEMENT ====================

    @GetMapping("/{id}/images")
    public String manageImages(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        List<vn.web.fashionshop.entity.Image> images = productImageService.getProductImages(id);

        model.addAttribute("product", product);
        model.addAttribute("images", images);

        return "admin/product/images";
    }

    @PostMapping("/{id}/images/upload")
    public String uploadImages(
            @PathVariable Long id,
            @RequestParam("files") org.springframework.web.multipart.MultipartFile[] files,
            RedirectAttributes redirectAttributes) {

        try {
            productImageService.uploadImages(id, files);
            redirectAttributes.addFlashAttribute("success", "Upload hình ảnh thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/products/" + id + "/images";
    }

    @PostMapping("/{productId}/images/{imageId}/set-main")
    public String setMainImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            RedirectAttributes redirectAttributes) {

        try {
            productImageService.setMainImage(productId, imageId);
            redirectAttributes.addFlashAttribute("success", "Đã đặt làm ảnh chính!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/products/" + productId + "/images";
    }

    @PostMapping("/{productId}/images/{imageId}/delete")
    public String deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            RedirectAttributes redirectAttributes) {

        try {
            productImageService.deleteImage(productId, imageId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa hình ảnh!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/products/" + productId + "/images";
    }

}
