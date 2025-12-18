package vn.web.fashionshop.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import vn.web.fashionshop.config.UploadProperties;

@Service
public class LocalFileUploadService implements FileUploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path imagesDir;
    private final String imagesUrlPrefix;

    public LocalFileUploadService(UploadProperties uploadProperties) {
        // Use explicit guards so IDE null analysis can prove non-null.
        String dir = uploadProperties.getImagesDir();
        if (dir == null || dir.isBlank()) {
            dir = "uploads/images";
        }

        String prefix = uploadProperties.getImagesUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "/images/";
        }

        this.imagesDir = Paths.get(dir);
        this.imagesUrlPrefix = normalizeUrlPrefix(prefix);
    }

    @Override
    public UploadResult storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image uploads are allowed");
        }

        // Prefer content-type to determine extension to avoid trusting client-supplied filename.
        String ext = extensionFromContentType(contentType);

        // Fallback: if content-type is generic/unknown, attempt to extract extension from filename (sanitized).
        if (ext.isEmpty()) {
            String originalFilename = file.getOriginalFilename();
            String safeName = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
            ext = getExtension(safeName);
        }

        if (ext.isEmpty() || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid image type. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        try {
            Files.createDirectories(imagesDir);

            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = imagesDir.resolve(filename).normalize();

            // Prevent path traversal
            if (!target.startsWith(imagesDir.normalize())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }

            // Save first, then validate it is a real image to mitigate malicious uploads.
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            // Validate that the uploaded file is actually an image
            if (!isReadableImage(target)) {
                Files.deleteIfExists(target);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image content");
            }

            return new UploadResult(filename, imagesUrlPrefix + filename);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image", ex);
        }
    }

    @Override
    public boolean deleteImage(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        String clean = StringUtils.cleanPath(filename);
        Path filePath = imagesDir.resolve(clean).normalize();
        if (!filePath.startsWith(imagesDir.normalize())) {
            return false;
        }
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image", ex);
        }
    }

    private static String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private static String extensionFromContentType(String contentType) {
        String ct = contentType.toLowerCase(Locale.ROOT);
        return switch (ct) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            // Some browsers may send these variants
            case "image/jpg" -> "jpg";
            default -> "";
        };
    }

    private static boolean isReadableImage(Path file) {
        try {
            // ImageIO.read returns null if it cannot decode the input.
            return ImageIO.read(file.toFile()) != null;
        } catch (IOException ex) {
            return false;
        }
    }

    private static String normalizeUrlPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/images/";
        }
        String p = prefix.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        if (!p.endsWith("/")) {
            p = p + "/";
        }
        return p;
    }
}
