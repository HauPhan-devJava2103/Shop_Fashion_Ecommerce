package vn.web.fashionshop.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.service.ProductService;

@Controller
@RequiredArgsConstructor
public class WishlistController {

    private final ProductService productService;
    private static final String WISHLIST_COOKIE_NAME = "wishlist";
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 ngày

    // Toggle sản phẩm trong wishlist (thêm/xóa)
    @PostMapping("/api/wishlist/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<?> toggleWishlist(
            @PathVariable Long productId,
            @CookieValue(value = WISHLIST_COOKIE_NAME, defaultValue = "") String wishlistCookie,
            HttpServletResponse response) {

        List<Long> wishlist = parseWishlistCookie(wishlistCookie);
        boolean added;

        if (wishlist.contains(productId)) {
            wishlist.remove(productId);
            added = false;
        } else {
            wishlist.add(productId);
            added = true;
        }

        // Lưu cookie mới
        saveWishlistCookie(wishlist, response);

        return ResponseEntity.ok().body(new WishlistResponse(added, wishlist.size()));
    }

    // Kiểm tra sản phẩm có trong wishlist không
    @GetMapping("/api/wishlist/check/{productId}")
    @ResponseBody
    public ResponseEntity<?> checkWishlist(
            @PathVariable Long productId,
            @CookieValue(value = WISHLIST_COOKIE_NAME, defaultValue = "") String wishlistCookie) {

        List<Long> wishlist = parseWishlistCookie(wishlistCookie);
        boolean inWishlist = wishlist.contains(productId);

        return ResponseEntity.ok().body(new WishlistCheckResponse(inWishlist));
    }

    // Lấy số lượng sản phẩm trong wishlist
    @GetMapping("/api/wishlist/count")
    @ResponseBody
    public ResponseEntity<?> getWishlistCount(
            @CookieValue(value = WISHLIST_COOKIE_NAME, defaultValue = "") String wishlistCookie) {

        List<Long> wishlist = parseWishlistCookie(wishlistCookie);
        return ResponseEntity.ok().body(new WishlistCountResponse(wishlist.size()));
    }

    // Trang hiển thị wishlist
    @GetMapping("/wishlist")
    public String viewWishlist(
            @CookieValue(value = WISHLIST_COOKIE_NAME, defaultValue = "") String wishlistCookie,
            Model model) {

        List<Long> wishlistIds = parseWishlistCookie(wishlistCookie);
        List<Product> wishlistProducts = new ArrayList<>();

        if (!wishlistIds.isEmpty()) {
            wishlistProducts = productService.findByIds(wishlistIds);
        }

        model.addAttribute("wishlistProducts", wishlistProducts);
        model.addAttribute("wishlistCount", wishlistIds.size());

        return "wishlist";
    }

    // Xóa tất cả wishlist
    @PostMapping("/api/wishlist/clear")
    @ResponseBody
    public ResponseEntity<?> clearWishlist(HttpServletResponse response) {
        Cookie cookie = new Cookie(WISHLIST_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().body(new WishlistResponse(false, 0));
    }

    // ========== Helper Methods ==========

    private List<Long> parseWishlistCookie(String cookieValue) {
        if (cookieValue == null || cookieValue.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return Arrays.stream(cookieValue.split("-"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    private void saveWishlistCookie(List<Long> wishlist, HttpServletResponse response) {
        String cookieValue = wishlist.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("-"));

        Cookie cookie = new Cookie(WISHLIST_COOKIE_NAME, cookieValue);
        cookie.setMaxAge(COOKIE_MAX_AGE); // 7 ngày
        cookie.setPath("/");
        cookie.setHttpOnly(false); // Cho phép JS đọc để update UI
        response.addCookie(cookie);
    }

    // ========== Response DTOs ==========

    record WishlistResponse(boolean added, int count) {
    }

    record WishlistCheckResponse(boolean inWishlist) {
    }

    record WishlistCountResponse(int count) {
    }
}
