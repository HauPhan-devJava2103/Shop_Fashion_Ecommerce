package vn.web.fashionshop.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.entity.WishlistItem;
import vn.web.fashionshop.repository.ProductRepository;
import vn.web.fashionshop.repository.UserRepository;
import vn.web.fashionshop.repository.WishlistItemRepository;

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
}
