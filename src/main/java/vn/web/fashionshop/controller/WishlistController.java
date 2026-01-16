package vn.web.fashionshop.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.WishlistItem;
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.GuestWishlistService;
import vn.web.fashionshop.service.WishlistService;

@Controller
public class WishlistController {

    private final WishlistService wishlistService;
    private final GuestWishlistService guestWishlistService;

    public WishlistController(WishlistService wishlistService, GuestWishlistService guestWishlistService) {
        this.wishlistService = wishlistService;
        this.guestWishlistService = guestWishlistService;
    }

    @GetMapping("/wishlist")
    public String wishlist(Model model, HttpServletRequest request) {
        String email = CartService.currentUserEmailOrNull();

        if (email == null || email.isBlank()) {
            // Guest: hiển thị wishlist từ cookie
            List<Product> products = guestWishlistService.getGuestWishlistProducts(request);
            model.addAttribute("guestMode", true);
            model.addAttribute("products", products);
            model.addAttribute("pageTitle", "Wishlist");
            return "wishlist";
        }

        // User đã đăng nhập: hiển thị wishlist từ DB
        List<WishlistItem> items = wishlistService.getMyWishlistItems();
        model.addAttribute("guestMode", false);
        model.addAttribute("items", items);
        model.addAttribute("pageTitle", "Wishlist");
        return "wishlist";
    }

    @PostMapping("/wishlist/toggle")
    public String toggleWishlistPost(
            @RequestParam("productId") long productId,
            HttpServletRequest request,
            HttpServletResponse response) {
        return doToggle(productId, request, response);
    }

    // Support existing anchor-style hearts (GET) without needing JS
    @GetMapping("/wishlist/toggle")
    public String toggleWishlistGet(
            @RequestParam("productId") long productId,
            HttpServletRequest request,
            HttpServletResponse response) {
        return doToggle(productId, request, response);
    }

    private String doToggle(long productId, HttpServletRequest request, HttpServletResponse response) {
        String email = CartService.currentUserEmailOrNull();

        try {
            if (email == null || email.isBlank()) {
                // Guest: toggle trong cookie
                guestWishlistService.toggle(request, response, productId);
            } else {
                // User đã đăng nhập: toggle trong DB
                wishlistService.toggle(productId);
            }
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
