package vn.web.fashionshop.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.web.fashionshop.dto.product.ProductVariantOptionDto;
import vn.web.fashionshop.repository.ProductVariantRepository;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductVariantRepository productVariantRepository;

    public ProductApiController(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<ProductVariantOptionDto>> getVariants(@PathVariable("productId") Long productId) {
        if (productId == null || productId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        var variants = productVariantRepository.findByProductIdOrderByIdAsc(productId);
        var dto = variants.stream()
                .map(v -> new ProductVariantOptionDto(
                        v.getId(),
                        v.getColor(),
                        v.getSize() != null ? v.getSize().name() : null,
                        v.getStock()))
                .toList();

        return ResponseEntity.ok(dto);
    }
}
