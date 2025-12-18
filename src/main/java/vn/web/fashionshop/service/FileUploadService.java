package vn.web.fashionshop.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service for storing uploaded files (primarily product/user images).
 */
public interface FileUploadService {

    UploadResult storeImage(MultipartFile file);

    /**
     * Deletes a previously stored image by filename.
     *
     * @param filename the stored filename (e.g. "a1b2c3.jpg")
     * @return true if a file was deleted; false if it did not exist
     */
    boolean deleteImage(String filename);

    record UploadResult(String filename, String url) {
    }
}
