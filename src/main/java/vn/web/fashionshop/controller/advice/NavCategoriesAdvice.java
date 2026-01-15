package vn.web.fashionshop.controller.advice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.service.CategoryService;

@ControllerAdvice
public class NavCategoriesAdvice {

    private final CategoryService categoryService;

    public NavCategoriesAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute("navRootCategories")
    public List<Category> navRootCategories(HttpServletRequest request) {
        if (isAdminRequest(request)) {
            return List.of();
        }
        List<Category> roots = categoryService.getRootCategories();
        if (roots == null || roots.isEmpty()) {
            return List.of();
        }
        return roots.stream()
                .filter(c -> c != null && Boolean.TRUE.equals(c.getIsActive()))
                .toList();
    }

    @ModelAttribute("navRootChildrenMap")
    public Map<String, List<Category>> navRootChildrenMap(@ModelAttribute("navRootCategories") List<Category> roots,
            HttpServletRequest request) {
        if (isAdminRequest(request) || roots == null || roots.isEmpty()) {
            return Map.of();
        }

        Map<String, List<Category>> map = new HashMap<>();
        for (Category r : roots) {
            if (r == null || r.getSlug() == null || r.getSlug().isBlank()) {
                continue;
            }
            map.put(r.getSlug(), categoryService.getActiveChildrenByParentSlug(r.getSlug()));
        }
        return map;
    }

    private static boolean isAdminRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/admin");
    }
}
