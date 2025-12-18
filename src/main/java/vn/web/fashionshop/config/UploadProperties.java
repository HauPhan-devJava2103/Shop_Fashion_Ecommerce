package vn.web.fashionshop.config;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

/**
 * Application-specific upload settings.
 */
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /**
     * Directory on disk where images are stored.
     */
    @NotBlank
    @NonNull
    private String imagesDir = "uploads/images";

    /**
     * URL prefix used to access images.
     */
    @NotBlank
    @NonNull
    private String imagesUrlPrefix = "/images/";

    @NonNull
    public String getImagesDir() {
        return imagesDir;
    }

    public void setImagesDir(String imagesDir) {
        // Keep non-null to satisfy nullness analysis and avoid misconfiguration NPEs later.
        this.imagesDir = Objects.requireNonNull(imagesDir, "app.upload.images-dir must not be null");
    }

    @NonNull
    public String getImagesUrlPrefix() {
        return imagesUrlPrefix;
    }

    public void setImagesUrlPrefix(String imagesUrlPrefix) {
        this.imagesUrlPrefix = Objects.requireNonNull(imagesUrlPrefix, "app.upload.images-url-prefix must not be null");
    }
}
