package vn.web.fashionshop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.repository.ProductRepository;
import vn.web.fashionshop.util.GuestWishlistCookieUtil;

/**
 * Service xử lý wishlist cho khách vãng lai (guest)
 * Lưu trữ qua cookie, không cần đăng nhập
 */
@Service
public class GuestWishlistService {

    private final ProductRepository productRepository;

    public GuestWishlistService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Lấy danh sách sản phẩm yêu thích của guest
     */
    public List<Product> getGuestWishlistProducts(HttpServletRequest request) {
        Set<Long> productIds = GuestWishlistCookieUtil.readGuestWishlist(request.getCookies());
        if (productIds.isEmpty()) {
            return new ArrayList<>();
        }
        return productRepository.findAllById(productIds).stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .toList();
    }

    /**
     * Lấy danh sách productId từ cookie
     */
    public Set<Long> getGuestWishlistIds(HttpServletRequest request) {
        return GuestWishlistCookieUtil.readGuestWishlist(request.getCookies());
    }

    /**
     * Đếm số lượng sản phẩm trong wishlist
     */
    public int getGuestWishlistCount(HttpServletRequest request) {
        return GuestWishlistCookieUtil.readGuestWishlist(request.getCookies()).size();
    }

    /**
     * Toggle sản phẩm trong wishlist (thêm nếu chưa có, xóa nếu đã có)
     * 
     * @return true nếu sản phẩm được thêm, false nếu bị xóa
     */
    public boolean toggle(HttpServletRequest request, HttpServletResponse response, Long productId) {
        // Kiểm tra sản phẩm tồn tại
        if (productId == null || !productRepository.existsById(productId)) {
            throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
        }

        var result = GuestWishlistCookieUtil.toggleProduct(request.getCookies(), productId);
        response.addCookie(GuestWishlistCookieUtil.buildCookie(result.wishlist()));
        return result.added();
    }

    /**
     * Thêm sản phẩm vào wishlist
     */
    public void addProduct(HttpServletRequest request, HttpServletResponse response, Long productId) {
        if (productId == null || !productRepository.existsById(productId)) {
            throw new IllegalArgumentException("PRODUCT_NOT_FOUND");
        }
        Set<Long> wishlist = GuestWishlistCookieUtil.addProduct(request.getCookies(), productId);
        response.addCookie(GuestWishlistCookieUtil.buildCookie(wishlist));
    }

    /**
     * Xóa sản phẩm khỏi wishlist
     */
    public void removeProduct(HttpServletRequest request, HttpServletResponse response, Long productId) {
        Set<Long> wishlist = GuestWishlistCookieUtil.removeProduct(request.getCookies(), productId);
        response.addCookie(GuestWishlistCookieUtil.buildCookie(wishlist));
    }

    /**
     * Xóa toàn bộ wishlist cookie
     */
    public void clearGuestWishlist(HttpServletResponse response) {
        response.addCookie(GuestWishlistCookieUtil.clearCookie());
    }

    /**
     * Kiểm tra sản phẩm có trong wishlist không
     */
    public boolean isProductInWishlist(HttpServletRequest request, Long productId) {
        if (productId == null) {
            return false;
        }
        Set<Long> wishlist = GuestWishlistCookieUtil.readGuestWishlist(request.getCookies());
        return wishlist.contains(productId);
    }
}
