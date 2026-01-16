package vn.web.fashionshop.controller.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.web.fashionshop.dto.wishlist.WishlistStatusResponse;
import vn.web.fashionshop.dto.wishlist.WishlistToggleRequest;
import vn.web.fashionshop.dto.wishlist.WishlistToggleResponse;
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.GuestWishlistService;
import vn.web.fashionshop.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistApiController {

    private final WishlistService wishlistService;
    private final GuestWishlistService guestWishlistService;

    public WishlistApiController(WishlistService wishlistService, GuestWishlistService guestWishlistService) {
        this.wishlistService = wishlistService;
        this.guestWishlistService = guestWishlistService;
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count(HttpServletRequest request) {
        if (isAuthenticated()) {
            return ResponseEntity.ok(wishlistService.getMyWishlistCount());
        }
        // Guest: count from cookie
        return ResponseEntity.ok((long) guestWishlistService.getGuestWishlistCount(request));
    }

    @GetMapping("/status")
    public ResponseEntity<WishlistStatusResponse> status(
            @RequestParam(name = "ids", required = false) String ids,
            HttpServletRequest request) {

        List<Long> productIds = parseIds(ids);

        if (isAuthenticated()) {
            List<Long> liked = wishlistService.getMyLikedProductIds(productIds);
            long count = wishlistService.getMyWishlistCount();
            return ResponseEntity.ok(new WishlistStatusResponse(liked, count));
        }

        // Guest: check from cookie
        Set<Long> guestWishlist = guestWishlistService.getGuestWishlistIds(request);
        List<Long> likedProductIds = productIds.stream()
                .filter(guestWishlist::contains)
                .toList();
        return ResponseEntity.ok(new WishlistStatusResponse(likedProductIds, guestWishlist.size()));
    }

    @PostMapping("/toggle")
    public ResponseEntity<WishlistToggleResponse> toggle(
            @RequestBody WishlistToggleRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {

        long productId = body != null && body.productId() != null ? body.productId() : 0L;
        if (productId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        if (isAuthenticated()) {
            boolean liked = wishlistService.toggle(productId);
            long count = wishlistService.getMyWishlistCount();
            return ResponseEntity.ok(new WishlistToggleResponse(liked, count));
        }

        // Guest: toggle in cookie
        try {
            boolean liked = guestWishlistService.toggle(request, response, productId);
            int count = guestWishlistService.getGuestWishlistCount(request);
            // Count after toggle
            count = liked ? count + 1 : count - 1;
            if (count < 0)
                count = 0;
            return ResponseEntity.ok(new WishlistToggleResponse(liked, count));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    private static boolean isAuthenticated() {
        String email = CartService.currentUserEmailOrNull();
        return email != null && !email.isBlank();
    }

    private static List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }

        String cleaned = ids.trim();
        if (cleaned.isEmpty()) {
            return List.of();
        }

        List<String> parts = Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        List<Long> out = new ArrayList<>();
        for (String p : parts) {
            try {
                long v = Long.parseLong(p);
                if (v > 0) {
                    out.add(v);
                }
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
        return out;
    }
}
