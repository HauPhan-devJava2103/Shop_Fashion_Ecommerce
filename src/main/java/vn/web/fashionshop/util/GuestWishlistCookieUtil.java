package vn.web.fashionshop.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.servlet.http.Cookie;

/**
 * Utility class để xử lý Guest Wishlist qua Cookie
 * Lưu danh sách productId yêu thích cho khách vãng lai
 */
public final class GuestWishlistCookieUtil {

    private GuestWishlistCookieUtil() {
    }

    public static final String COOKIE_NAME = "guest_wishlist";
    private static final int MAX_COOKIE_AGE_SECONDS = 60 * 60 * 24 * 30; // 30 days

    // Đọc danh sách productId từ cookie
    public static Set<Long> readGuestWishlist(Cookie[] cookies) {
        if (cookies == null) {
            return new LinkedHashSet<>();
        }
        for (Cookie c : cookies) {
            if (COOKIE_NAME.equals(c.getName())) {
                return decode(c.getValue());
            }
        }
        return new LinkedHashSet<>();
    }

    // Tạo cookie mới với danh sách productId
    public static Cookie buildCookie(Set<Long> productIds) {
        String encoded = encode(productIds);
        Cookie cookie = new Cookie(COOKIE_NAME, encoded);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(MAX_COOKIE_AGE_SECONDS);
        return cookie;
    }

    // Xóa cookie
    public static Cookie clearCookie() {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    // Thêm productId vào wishlist
    public static Set<Long> addProduct(Cookie[] cookies, Long productId) {
        Set<Long> wishlist = readGuestWishlist(cookies);
        if (productId != null && productId > 0) {
            wishlist.add(productId);
        }
        return wishlist;
    }

    // Xóa productId khỏi wishlist
    public static Set<Long> removeProduct(Cookie[] cookies, Long productId) {
        Set<Long> wishlist = readGuestWishlist(cookies);
        if (productId != null) {
            wishlist.remove(productId);
        }
        return wishlist;
    }

    // Toggle productId trong wishlist
    public static ToggleResult toggleProduct(Cookie[] cookies, Long productId) {
        Set<Long> wishlist = readGuestWishlist(cookies);
        boolean added;
        if (productId != null && productId > 0) {
            if (wishlist.contains(productId)) {
                wishlist.remove(productId);
                added = false;
            } else {
                wishlist.add(productId);
                added = true;
            }
        } else {
            added = false;
        }
        return new ToggleResult(wishlist, added);
    }

    public record ToggleResult(Set<Long> wishlist, boolean added) {
    }

    // Format: productId,productId,productId
    private static String encode(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Long id : productIds) {
            if (id == null || id <= 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id);
        }
        return URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8);
    }

    private static Set<Long> decode(String raw) {
        Set<Long> set = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) {
            return set;
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return set;
        }
        if (decoded.isBlank()) {
            return set;
        }
        String[] parts = decoded.split(",");
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            try {
                long id = Long.parseLong(part.trim());
                if (id > 0) {
                    set.add(id);
                }
            } catch (NumberFormatException ex) {
                // ignore bad token
            }
        }
        return set;
    }
}
