package vn.web.fashionshop.controller;

import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.web.fashionshop.service.FileUploadService;

/**
 * Simple upload endpoint.
 *
 * Uploaded images are saved under: app.upload.images-dir (default: uploads/images)
 * and can be accessed via: /images/{filename} (mapped in WebMvcConfig).
 */
@RestController
public class UploadController {

    private final FileUploadService fileUploadService;

    public UploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

        @GetMapping(value = "/admin/upload/image", produces = MediaType.TEXT_HTML_VALUE)
        public String uploadImageForm(CsrfToken csrfToken) {
                String csrfField = "";
                if (csrfToken != null) {
                        csrfField = "<input type=\"hidden\" name=\"" + csrfToken.getParameterName() + "\" value=\"" + csrfToken.getToken()
                                        + "\" />";
                }
                return """
                                <!doctype html>
                                <html lang=\"en\">
                                    <head>
                                        <meta charset=\"utf-8\" />
                                        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
                                        <title>Upload image</title>
                                    </head>
                                    <body style=\"font-family: system-ui, -apple-system, Segoe UI, Roboto, Arial, sans-serif; padding: 24px;\">
                                        <h2>Upload image</h2>
                                        <form action=\"/admin/upload/image\" method=\"post\" enctype=\"multipart/form-data\">
                                            """ + csrfField + """
                                            <input type=\"file\" name=\"file\" accept=\"image/*\" required />
                                            <button type=\"submit\" style=\"margin-left: 8px;\">Upload</button>
                                        </form>
                                        <p style=\"margin-top: 12px; color: #555;\">
                                            Endpoint expects <code>POST</code> multipart/form-data with field <code>file</code>.
                                        </p>
                                    </body>
                                </html>
                                """;
        }

    @PostMapping(value = "/admin/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadService.UploadResult uploadImage(@RequestParam("file") MultipartFile file) {
        return fileUploadService.storeImage(file);
    }
}
