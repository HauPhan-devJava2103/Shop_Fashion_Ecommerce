package vn.web.fashionshop.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.service.CategoryService;
import vn.web.fashionshop.service.ProductService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping({ "/", "/home" })
    public String getHomePage(Model model) {
        // Lấy danh mục gốc để hiển thị "Discover Collection"
        List<Category> rootCategories = categoryService.getRootCategories();
        model.addAttribute("featuredCategories", rootCategories);

        // Đếm số danh mục con của mỗi danh mục gốc
        Map<Long, Long> categoryChildCounts = categoryService.countChildCategoriesByRootCategory();
        model.addAttribute("categoryChildCounts", categoryChildCounts);

        // Lấy sản phẩm mới nhất theo danh mục Men và Women (giới hạn 8 sản phẩm)
        model.addAttribute("menProducts", productService.getNewArrivalsByCategory("men", 8));
        model.addAttribute("womenProducts", productService.getNewArrivalsByCategory("women", 8));

        return "home";
    }

}
