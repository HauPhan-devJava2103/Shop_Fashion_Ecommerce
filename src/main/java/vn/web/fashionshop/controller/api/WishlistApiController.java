package vn.web.fashionshop.controller.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.web.fashionshop.dto.wishlist.WishlistStatusResponse;
import vn.web.fashionshop.dto.wishlist.WishlistToggleRequest;
import vn.web.fashionshop.dto.wishlist.WishlistToggleResponse;
import vn.web.fashionshop.service.CartService;
import vn.web.fashionshop.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistApiController {

    private final WishlistService wishlistService;

    public WishlistApiController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        if (!isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(wishlistService.getMyWishlistCount());
    }

    @GetMapping("/status")
    public ResponseEntity<WishlistStatusResponse> status(@RequestParam(name = "ids", required = false) String ids) {
        if (!isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Long> productIds = parseIds(ids);
        List<Long> liked = wishlistService.getMyLikedProductIds(productIds);
        long count = wishlistService.getMyWishlistCount();
        return ResponseEntity.ok(new WishlistStatusResponse(liked, count));
    }

    @PostMapping("/toggle")
    public ResponseEntity<WishlistToggleResponse> toggle(@RequestBody WishlistToggleRequest body) {
        if (!isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        long productId = body != null && body.productId() != null ? body.productId() : 0L;
        if (productId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        boolean liked = wishlistService.toggle(productId);
        long count = wishlistService.getMyWishlistCount();
        return ResponseEntity.ok(new WishlistToggleResponse(liked, count));
    }

    private static boolean isAuthenticated() {
        String email = CartService.currentUserEmailOrNull();
        return email != null && !email.isBlank();
    }

    private static List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return List.of();
        }

        // Accept: "1,2,3" or repeated commas/spaces
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
