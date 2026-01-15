package vn.web.fashionshop.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import vn.web.fashionshop.dto.profile.ProfileForm;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.enums.EGender;
import vn.web.fashionshop.repository.UserRepository;
import vn.web.fashionshop.service.CartService;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", new ProfileForm(user.getFullName(), user.getPhone(), user.getGender(), user.getAddress()));
        }
        model.addAttribute("user", user);
        model.addAttribute("genders", EGender.values());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
            BindingResult bindingResult,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        String email = CartService.currentUserEmailOrNull();
        if (email == null || email.isBlank()) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        // Validate duplicate phone (phone is unique)
        if (profileForm.getPhone() != null && !profileForm.getPhone().isBlank()) {
            Optional<User> phoneOwner = userRepository.findByPhone(profileForm.getPhone().trim());
            if (phoneOwner.isPresent() && !phoneOwner.get().getId().equals(user.getId())) {
                bindingResult.rejectValue("phone", "duplicate", "Số điện thoại đã được sử dụng!");
            }
        }

        // Validate avatar file (optional)
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                model.addAttribute("avatarError", "Vui lòng chọn đúng định dạng ảnh (JPG/PNG/WebP...)!");
                model.addAttribute("user", user);
                model.addAttribute("genders", EGender.values());
                return "profile";
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("genders", EGender.values());
            return "profile";
        }

        user.setFullName(profileForm.getFullName() != null ? profileForm.getFullName().trim() : user.getFullName());
        user.setPhone(profileForm.getPhone() != null ? profileForm.getPhone().trim() : user.getPhone());
        user.setGender(profileForm.getGender());
        user.setAddress(profileForm.getAddress());
        user.setUpdatedAt(LocalDateTime.now());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String originalName = avatarFile.getOriginalFilename();
                String ext = "";
                if (originalName != null) {
                    int dot = originalName.lastIndexOf('.');
                    if (dot >= 0 && dot < originalName.length() - 1) {
                        ext = originalName.substring(dot).toLowerCase(Locale.ROOT);
                        // very small sanitization
                        if (!ext.matches("\\.[a-z0-9]{1,8}")) {
                            ext = "";
                        }
                    }
                }

                String fileName = "avatar_" + user.getId() + "_" + UUID.randomUUID() + ext;
                Path avatarDir = Paths.get("uploads", "images", "avatars");
                Files.createDirectories(avatarDir);

                Path dest = avatarDir.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

                user.setAvatarUrl("/images/avatars/" + fileName);
            } catch (Exception e) {
                model.addAttribute("avatarError", "Tải ảnh lên thất bại. Vui lòng thử lại!");
                model.addAttribute("user", user);
                model.addAttribute("genders", EGender.values());
                return "profile";
            }
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        return "redirect:/profile";
    }
}
