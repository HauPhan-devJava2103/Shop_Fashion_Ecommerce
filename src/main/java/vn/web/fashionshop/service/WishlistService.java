package vn.web.fashionshop.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.entity.WishlistItem;
import vn.web.fashionshop.repository.ProductRepository;
import vn.web.fashionshop.repository.UserRepository;
import vn.web.fashionshop.repository.WishlistItemRepository;
import vn.web.fashionshop.util.GuestWishlistCookieUtil;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(
            WishlistItemRepository wishlistItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<WishlistItem> getMyWishlistItems() {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }
        return wishlistItemRepository.findByUserEmailWithProduct(email);
    }

    @Transactional
    public boolean toggle(long productId) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }

        WishlistItem existing = wishlistItemRepository.findByUser_EmailAndProduct_Id(email, productId).orElse(null);
        if (existing != null) {
            wishlistItemRepository.delete(existing);
            return false;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("USER_NOT_FOUND"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("PRODUCT_NOT_FOUND"));

        WishlistItem wi = new WishlistItem();
        wi.setUser(user);
        wi.setProduct(product);
        wi.setCreatedAt(LocalDateTime.now());
        wishlistItemRepository.save(wi);
        return true;
    }

    @Transactional(readOnly = true)
    public long getMyWishlistCount() {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }
        return wishlistItemRepository.countByUser_Email(email);
    }

    @Transactional(readOnly = true)
    public List<Long> getMyLikedProductIds(List<Long> productIds) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("NOT_AUTHENTICATED");
        }
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyList();
        }
        return wishlistItemRepository.findLikedProductIds(email, productIds);
    }

    // Merge guest wishlist (from cookie)
    @Transactional
    public int mergeGuestWishlist(HttpServletRequest request, HttpServletResponse response) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return 0;
        }

        Set<Long> guestProductIds = GuestWishlistCookieUtil.readGuestWishlist(request.getCookies());
        if (guestProductIds.isEmpty()) {
            return 0;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return 0;
        }

        // Lấy danh sách productId đã có trong DB wishlist
        List<Long> existingIds = wishlistItemRepository.findProductIdsByUserEmail(email);

        int addedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Long productId : guestProductIds) {
            // Skip nếu đã có trong DB
            if (existingIds.contains(productId)) {
                continue;
            }

            // Kiểm tra product tồn tại và active
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !Boolean.TRUE.equals(product.getIsActive())) {
                continue;
            }

            // Thêm vào wishlist
            WishlistItem wi = new WishlistItem();
            wi.setUser(user);
            wi.setProduct(product);
            wi.setCreatedAt(now);
            wishlistItemRepository.save(wi);
            addedCount++;
        }

        // Xóa cookie sau khi merge
        response.addCookie(GuestWishlistCookieUtil.clearCookie());

        return addedCount;
    }
}
