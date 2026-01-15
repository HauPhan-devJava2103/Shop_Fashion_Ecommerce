package vn.web.fashionshop.dto.wishlist;

import java.util.List;

public record WishlistStatusResponse(List<Long> likedProductIds, long count) {
}
