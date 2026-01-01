package vn.web.fashionshop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.web.fashionshop.dto.ProductForm;
import vn.web.fashionshop.entity.Category;
import vn.web.fashionshop.entity.Image;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public Page<Product> searchProductsAdmin(String keyword, Long categoryId, String status, int pageNo) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(Math.max(pageNo - 1, 0), pageSize);

        Boolean isActive = null;
        if (status != null && !status.isBlank()) {
            if ("active".equalsIgnoreCase(status)) {
                isActive = true;
            } else if ("inactive".equalsIgnoreCase(status)) {
                isActive = false;
            }
        }

        return productRepository.searchProducts(keyword, categoryId, isActive, pageable);
    }

    public Page<Product> searchProductsPublic(String keyword, Long categoryId, int pageNo) {
        int pageSize = 12;
        Pageable pageable = PageRequest.of(Math.max(pageNo - 1, 0), pageSize);
        return productRepository.searchProducts(keyword, categoryId, true, pageable);
    }

    public Product getById(Long id) {
        if (id == null) {
            return null;
        }
        return productRepository.findById(id).orElse(null);
    }

    public Product getActiveById(Long id) {
        if (id == null) {
            return null;
        }
        return productRepository.findByIdAndIsActiveTrue(id).orElse(null);
    }

    @Transactional
    public Product create(ProductForm form) {
        if (form == null) {
            return null;
        }

        String sku = safeTrim(form.getSku());
        String productName = safeTrim(form.getProductName());
        if (sku == null || sku.isBlank() || productName == null || productName.isBlank()) {
            return null;
        }
        if (productRepository.existsBySku(sku)) {
            return null;
        }
        if (!isValidPrice(form.getPrice())) {
            return null;
        }

        Integer stock = form.getStock();
        if (stock == null || stock < 0) {
            return null;
        }

        BigDecimal discount = form.getDiscount() == null ? BigDecimal.ZERO : form.getDiscount();
        if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(new BigDecimal("100")) > 0) {
            return null;
        }

        Product product = new Product();
        product.setSku(sku);
        product.setProductName(productName);
        product.setDescription(form.getDescription());
        product.setStock(stock);
        product.setPrice(form.getPrice());
        product.setDiscount(discount);
        product.setIsActive(form.getIsActive() == null ? true : form.getIsActive());

        Category category = categoryService.getById(form.getCategoryId());
        product.setCategory(category);

        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        List<Image> images = buildImagesFromForm(product, form, now);
        if (images.isEmpty()) {
            // BR15: ít nhất 1 ảnh
            return null;
        }
        product.setImages(images);

        return productRepository.save(product);
    }

    @Transactional
    public Product update(ProductForm form) {
        if (form == null || form.getId() == null) {
            return null;
        }

        Product existing = getById(form.getId());
        if (existing == null) {
            return null;
        }

        String newSku = safeTrim(form.getSku());
        String newName = safeTrim(form.getProductName());

        if (newSku == null || newSku.isBlank() || newName == null || newName.isBlank()) {
            return null;
        }

        if (!Objects.equals(existing.getSku(), newSku) && productRepository.existsBySku(newSku)) {
            return null;
        }
        if (!isValidPrice(form.getPrice())) {
            return null;
        }
        Integer stock = form.getStock();
        if (stock == null || stock < 0) {
            return null;
        }
        BigDecimal discount = form.getDiscount() == null ? BigDecimal.ZERO : form.getDiscount();
        if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(new BigDecimal("100")) > 0) {
            return null;
        }

        existing.setSku(newSku);
        existing.setProductName(newName);
        existing.setDescription(form.getDescription());
        existing.setStock(stock);
        existing.setPrice(form.getPrice());
        existing.setDiscount(discount);
        existing.setIsActive(form.getIsActive() == null ? existing.getIsActive() : form.getIsActive());

        Category category = categoryService.getById(form.getCategoryId());
        existing.setCategory(category);

        LocalDateTime now = LocalDateTime.now();
        existing.setUpdatedAt(now);

        List<Image> images = buildImagesFromForm(existing, form, now);
        if (images.isEmpty()) {
            return null;
        }

        if (existing.getImages() != null) {
            existing.getImages().clear();
            existing.getImages().addAll(images);
        } else {
            existing.setImages(images);
        }

        return productRepository.save(existing);
    }

    @Transactional
    public void deactivate(Long id) {
        Product existing = getById(id);
        if (existing == null) {
            return;
        }
        existing.setIsActive(false);
        existing.setUpdatedAt(LocalDateTime.now());
        productRepository.save(existing);
    }

    private static boolean isValidPrice(BigDecimal price) {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    private static String safeTrim(String input) {
        return input == null ? null : input.trim();
    }

    private static List<Image> buildImagesFromForm(Product product, ProductForm form, LocalDateTime now) {
        List<String> urls = new ArrayList<>();
        if (form.getMainImageUrl() != null && !form.getMainImageUrl().isBlank()) {
            urls.add(form.getMainImageUrl().trim());
        }
        if (form.getExtraImageUrl1() != null && !form.getExtraImageUrl1().isBlank()) {
            urls.add(form.getExtraImageUrl1().trim());
        }
        if (form.getExtraImageUrl2() != null && !form.getExtraImageUrl2().isBlank()) {
            urls.add(form.getExtraImageUrl2().trim());
        }
        if (form.getExtraImageUrl3() != null && !form.getExtraImageUrl3().isBlank()) {
            urls.add(form.getExtraImageUrl3().trim());
        }
        if (form.getExtraImageUrl4() != null && !form.getExtraImageUrl4().isBlank()) {
            urls.add(form.getExtraImageUrl4().trim());
        }

        // BR15: tối đa 5 ảnh (1 chính + 4 phụ)
        if (urls.size() > 5) {
            urls = urls.subList(0, 5);
        }

        List<Image> images = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            Image image = new Image();
            image.setProduct(product);
            image.setUrlImage(urls.get(i));
            image.setAltText(product.getProductName());
            image.setIsMain(i == 0);
            image.setCreatedAt(now);
            image.setUpdatedAt(now);
            images.add(image);
        }
        return images;
    }
}
