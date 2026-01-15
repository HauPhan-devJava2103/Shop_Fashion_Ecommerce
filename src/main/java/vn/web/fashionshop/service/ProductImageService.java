package vn.web.fashionshop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.web.fashionshop.entity.Image;
import vn.web.fashionshop.entity.Product;
import vn.web.fashionshop.repository.ImageRepository;
import vn.web.fashionshop.repository.ProductRepository;
import vn.web.fashionshop.service.FileUploadService.UploadResult;

@Service
public class ProductImageService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final FileUploadService fileUploadService;

    public ProductImageService(ImageRepository imageRepository,
            ProductRepository productRepository,
            FileUploadService fileUploadService) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
        this.fileUploadService = fileUploadService;
    }

    /**
     * Get all images for a product
     */
    public List<Image> getProductImages(Long productId) {
        return imageRepository.findByProductId(productId);
    }

    /**
     * Upload multiple images for a product
     */
    @Transactional
    public void uploadImages(Long productId, MultipartFile[] files) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            // Upload file
            UploadResult result = fileUploadService.storeImage(file);

            // Create image entity
            Image image = new Image();
            image.setProduct(product);
            image.setUrlImage(result.url());
            image.setAltText(product.getProductName());
            image.setIsMain(false); // Default not main
            image.setCreatedAt(LocalDateTime.now());

            imageRepository.save(image);
        }
    }

    /**
     * Upload a single image and set it as the main image for the product.
     */
    @Transactional
    public void uploadMainImage(Long productId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Upload file
        UploadResult result = fileUploadService.storeImage(file);

        // Unset existing main images
        List<Image> existingImages = imageRepository.findByProductId(productId);
        for (Image img : existingImages) {
            if (Boolean.TRUE.equals(img.getIsMain())) {
                img.setIsMain(false);
                img.setUpdatedAt(LocalDateTime.now());
                imageRepository.save(img);
            }
        }

        // Create image entity
        Image image = new Image();
        image.setProduct(product);
        image.setUrlImage(result.url());
        image.setAltText(product.getProductName());
        image.setIsMain(true);
        image.setCreatedAt(LocalDateTime.now());
        image.setUpdatedAt(LocalDateTime.now());

        imageRepository.save(image);
    }

    /**
     * Set an image as main (and unset others)
     */
    @Transactional
    public void setMainImage(Long productId, Long imageId) {
        List<Image> images = imageRepository.findByProductId(productId);

        for (Image img : images) {
            if (img.getId().equals(imageId)) {
                img.setIsMain(true);
            } else {
                img.setIsMain(false);
            }
            img.setUpdatedAt(LocalDateTime.now());
            imageRepository.save(img);
        }
    }

    /**
     * Delete an image
     */
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        // Delete file from storage
        String filename = extractFilename(image.getUrlImage());
        if (filename != null) {
            fileUploadService.deleteImage(filename);
        }

        // Delete from database
        imageRepository.delete(image);
    }

    /**
     * Extract filename from URL
     */
    private String extractFilename(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < url.length() - 1) {
            return url.substring(lastSlash + 1);
        }
        return url;
    }
}
