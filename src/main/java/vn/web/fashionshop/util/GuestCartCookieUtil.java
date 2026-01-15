package vn.web.fashionshop.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.Cookie;

public final class GuestCartCookieUtil {

    private GuestCartCookieUtil() {
    }

    public static final String COOKIE_NAME = "guest_cart";
    // Keep small to avoid cookie size issues
    private static final int MAX_COOKIE_AGE_SECONDS = 60 * 60 * 24 * 7; // 7 days

    public static Map<Long, Integer> readGuestCart(Cookie[] cookies) {
        if (cookies == null) {
            return new LinkedHashMap<>();
        }
        for (Cookie c : cookies) {
            if (COOKIE_NAME.equals(c.getName())) {
                return decode(c.getValue());
            }
        }
        return new LinkedHashMap<>();
    }

    public static Cookie buildCookie(Map<Long, Integer> cart) {
        String encoded = encode(cart);
        Cookie cookie = new Cookie(COOKIE_NAME, encoded);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(MAX_COOKIE_AGE_SECONDS);
        return cookie;
    }

    public static Cookie clearCookie() {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    // Format: variantId:qty,variantId:qty
    private static String encode(Map<Long, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, Integer> e : cart.entrySet()) {
            Long variantId = e.getKey();
            Integer qty = e.getValue();
            if (variantId == null || qty == null || qty <= 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(variantId).append(':').append(qty);
        }
        return URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8);
    }

    private static Map<Long, Integer> decode(String raw) {
        Map<Long, Integer> map = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return map;
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return map;
        }
        if (decoded.isBlank()) {
            return map;
        }
        String[] parts = decoded.split(",");
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            String[] kv = part.split(":");
            if (kv.length != 2) {
                continue;
            }
            try {
                long id = Long.parseLong(kv[0].trim());
                int qty = Integer.parseInt(kv[1].trim());
                if (id > 0 && qty > 0) {
                    map.put(id, qty);
                }
            } catch (NumberFormatException ex) {
                // ignore bad token
            }
        }
        return map;
    }
}
