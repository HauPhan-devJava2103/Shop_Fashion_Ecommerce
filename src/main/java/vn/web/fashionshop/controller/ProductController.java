package vn.web.fashionshop.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.service.CategoryService;
import vn.web.fashionshop.service.ProductService;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/shop")
    public String shop(Model model,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId) {

        Page<Product> productsPage = productService.searchProductsPublic(keyword, categoryId, page);
        model.addAttribute("products", productsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "shop";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getActiveById(id);
        if (product == null) {
            return "redirect:/shop";
        }
        model.addAttribute("product", product);
        return "single-product-details";
    }

    // Backward compatible route from header dropdown
    @GetMapping("/single-product-details")
    public String productDetailsCompat(@RequestParam(name = "id", required = false) Long id) {
        if (id == null) {
            return "redirect:/shop";
        }
        return "redirect:/products/" + id;
    }
}
