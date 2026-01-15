package vn.web.fashionshop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.web.fashionshop.dto.ProductCreateDTO;
import vn.web.fashionshop.dto.ProductResponseDTO;
import vn.web.fashionshop.dto.ProductUpdateDTO;
import vn.web.fashionshop.dto.ProductVariantUpdateDTO;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.entity.ProductVariant;
import vn.web.fashionshop.enums.ESize;
import vn.web.fashionshop.repository.CategoryRepository;
import vn.web.fashionshop.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Get product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    // Create new product with variants
    @Transactional
    public Product createProduct(ProductCreateDTO dto) {
        // 1. Create product entity
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setSku(dto.getSku());
        product.setPrice(BigDecimal.valueOf(dto.getPrice()));
        product.setDiscount(dto.getDiscount() != null ? BigDecimal.valueOf(dto.getDiscount()) : BigDecimal.ZERO);
        product.setDescription(dto.getDescription());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        product.setCreatedAt(LocalDateTime.now());

        // 2. Set category
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        // 3. Initialize empty variants list
        product.setVariants(new ArrayList<>());

        // 4. Add variants if provided
        int totalStock = 0;
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            for (ProductVariantUpdateDTO variantDTO : dto.getVariants()) {
                // Skip if marked for deletion (shouldn't happen in create, but just in case)
                if (variantDTO.get_delete() != null && variantDTO.get_delete()) {
                    continue;
                }

                ProductVariant variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSkuVariant(variantDTO.getSkuVariant());
                variant.setColor(variantDTO.getColor());

                // Parse size enum
                if (variantDTO.getSize() != null && !variantDTO.getSize().isEmpty()) {
                    try {
                        variant.setSize(ESize.valueOf(variantDTO.getSize()));
                    } catch (IllegalArgumentException e) {
                        variant.setSize(null);
                    }
                } else {
                    variant.setSize(null);
                }

                variant.setStock(variantDTO.getStock() != null ? variantDTO.getStock() : 0);
                variant.setCreatedAt(LocalDateTime.now());

                product.getVariants().add(variant);
                totalStock += variant.getStock();
            }
        }

        // 5. Set product total stock
        product.setStock(totalStock);

        // 6. Save product (cascade will save variants)
        return productRepository.save(product);
    }

    // STATS
    // Tổng đơn hàng
    public Long countAllProduct() {
        return productRepository.countAll();
    }

    // Tổng đơn còn tồn kho
    public Long countInStockProduct() {
        return productRepository.countInStock();
    }

    // Tổng đơn hêt tồn kho
    public Long countOutOfStockProduct() {
        return productRepository.countOutOfStock();
    }

    // Tổng giá trị hàng tồn kho
    public Long totalValueInStock() {
        return productRepository.totalValueInStock();
    }

    // Thống kê sản phẩm theo danh mục
    public List<Object[]> getProductCountByCategory() {
        return productRepository.getProductCountByCategory();
    }

    // Lấy sản phẩm mới nhất theo category slug (cho trang chủ)
    public List<Product> getNewArrivalsByCategory(String categorySlug, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findNewArrivalsByCategorySlug(categorySlug, pageable);
    }

    public Page<Product> searchProductAdvanced(String keyword, Long category, String stock, String sku, String isActive,
            int pageNo) {
        int pageSize = 6;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        return productRepository.searchProductAdvanced(keyword, category, stock, sku, isActive, pageable);
    }

    /**
     * Search products and convert to DTO for AJAX response
     */
    public Map<String, Object> searchProductsForAjax(String keyword, Long categoryId, String stock, String sku,
            String isActive,
            int page) {
        Page<Product> productPage = searchProductAdvanced(keyword, categoryId, stock, sku, isActive, page);

        // Convert to DTO to avoid circular reference
        List<ProductResponseDTO> productDTOs = new ArrayList<>();
        for (Product product : productPage.getContent()) {
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setId(product.getId());
            dto.setSku(product.getSku());
            dto.setProductName(product.getProductName());
            dto.setCategoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : "N/A");
            dto.setPrice(product.getPrice());
            dto.setStock(product.getStock());
            dto.setMainImageUrl(product.getMainImageUrl());
            dto.setIsActive(product.getIsActive());
            productDTOs.add(dto);
        }

        // Build response similar to Spring Page structure
        Map<String, Object> response = new HashMap<>();
        response.put("content", productDTOs);
        response.put("number", productPage.getNumber());
        response.put("numberOfElements", productPage.getNumberOfElements());
        response.put("totalPages", productPage.getTotalPages());
        response.put("totalElements", productPage.getTotalElements());
        response.put("first", productPage.isFirst());
        response.put("last", productPage.isLast());

        return response;
    }

    @Transactional
    public Product updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = getProductById(id);

        product.setProductName(dto.getProductName());
        product.setSku(dto.getSku());
        product.setPrice(BigDecimal.valueOf(dto.getPrice()));
        product.setDiscount(dto.getDiscount() != null ? BigDecimal.valueOf(dto.getDiscount()) : BigDecimal.ZERO);
        product.setStock(dto.getStock() != null ? dto.getStock() : 0);
        product.setDescription(dto.getDescription());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : false);
        product.setUpdatedAt(LocalDateTime.now());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        // 4. Update variant stocks if provided
        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            updateVariantStocks(product, dto.getVariants());
        }

        // 5. Save product
        Product savedProduct = productRepository.save(product);
        return savedProduct;
    }

    /**
     * Full CRUD for product variants - Add, Update, Delete
     */
    private void updateVariantStocks(Product product, List<ProductVariantUpdateDTO> variantUpdates) {

        List<ProductVariant> variantsToKeep = new ArrayList<>();
        int totalStock = 0;

        for (ProductVariantUpdateDTO dto : variantUpdates) {
            // Skip if marked for deletion
            if (dto.get_delete() != null && dto.get_delete()) {
                continue;
            }

            ProductVariant variant;

            if (dto.getId() == null || dto.getId() == 0) {
                // CREATE NEW VARIANT
                variant = new ProductVariant();
                variant.setProduct(product);
                variant.setCreatedAt(LocalDateTime.now());
            } else {
                // UPDATE EXISTING VARIANT
                variant = product.getVariants().stream()
                        .filter(v -> v.getId().equals(dto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Variant not found with id: " + dto.getId()));
            }

            // Update variant fields
            variant.setSkuVariant(dto.getSkuVariant());
            variant.setColor(dto.getColor());

            // Parse size enum
            if (dto.getSize() != null && !dto.getSize().isEmpty()) {
                try {
                    variant.setSize(ESize.valueOf(dto.getSize()));
                } catch (IllegalArgumentException e) {
                    variant.setSize(null);
                }
            } else {
                variant.setSize(null);
            }

            variant.setStock(dto.getStock() != null ? dto.getStock() : 0);
            variant.setUpdatedAt(LocalDateTime.now());

            variantsToKeep.add(variant);
            totalStock += variant.getStock();
        }

        // Replace product variants list
        product.getVariants().clear();
        product.getVariants().addAll(variantsToKeep);

        // Update product total stock
        product.setStock(totalStock);

    }

    /**
     * Lấy danh sách sản phẩm theo list IDs (cho Wishlist)
     */
    public List<Product> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return productRepository.findByIdIn(ids);
    }

}
