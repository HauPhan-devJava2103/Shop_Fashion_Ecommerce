package vn.web.fashionshop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import vn.web.fashionshop.entity.WishlistItem;
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.WishlistService;

@Controller
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        List<WishlistItem> items = wishlistService.getMyWishlistItems();
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Wishlist");
        return "wishlist";
    }

    @PostMapping("/wishlist/toggle")
    public String toggleWishlistPost(
            @RequestParam("productId") long productId,
            HttpServletRequest request) {
        return doToggle(productId, request);
    }

    // Support existing anchor-style hearts (GET) without needing JS
    @GetMapping("/wishlist/toggle")
    public String toggleWishlistGet(
            @RequestParam("productId") long productId,
            HttpServletRequest request) {
        return doToggle(productId, request);
    }

    private String doToggle(long productId, HttpServletRequest request) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        try {
            wishlistService.toggle(productId);
        } catch (IllegalArgumentException ex) {
            // ignore: product not found
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/wishlist";
    }
}
