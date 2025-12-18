package vn.web.fashionshop.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.web.fashionshop.dto.RegisterDTO;
import vn.web.fashionshop.entity.Role;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.enums.ERoleName;
import vn.web.fashionshop.repository.RoleRepository;
import vn.web.fashionshop.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> getAllUsers(int pageNo) {
        int pageSize = 6;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id).orElse(null);
    }

    public Role getRoleByName(ERoleName roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    public boolean checkEmailExist(@NonNull String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkPhoneExist(@NonNull String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User create(User user) {
        if (user == null) {
            return null;
        }

        final String email = user.getEmail();
        final String phone = user.getPhone();
        final String rawPassword = user.getPassword();

        if (email == null || email.isBlank() || phone == null || phone.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return null;
        }
        // Check duplicate email and phone
        if (checkEmailExist(email)) {
            return null;
        }
        if (checkPhoneExist(phone)) {
            return null;
        }

        // Resolve role if only id is present (from form binding)
        Role inputRole = user.getRole();
        if (inputRole != null) {
            Long roleId = inputRole.getId();
            if (roleId != null) {
                Role role = roleRepository.findById(roleId).orElse(null);
                if (role != null) {
                    user.setRole(role);
                }
            }
        }

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(rawPassword));

        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setIsActive(true);
        return userRepository.save(user);
    }

    public User update(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }

        final String newEmail = user.getEmail();
        final String newPhone = user.getPhone();
        if (newEmail == null || newEmail.isBlank() || newPhone == null || newPhone.isBlank()) {
            return null;
        }

        User existingUser = getUserById(user.getId());
        if (existingUser != null) {
            // Check Duplicate Email
            if (!Objects.equals(existingUser.getEmail(), newEmail)) {
                if (checkEmailExist(newEmail)) {
                    return null; // Email taken by another user
                }
                existingUser.setEmail(newEmail);
            }

            // Check Duplicate Phone
            if (!Objects.equals(existingUser.getPhone(), newPhone)) {
                if (checkPhoneExist(newPhone)) {
                    return null; // Phone taken by another user
                }
                existingUser.setPhone(newPhone);
            }

            existingUser.setFullName(user.getFullName());
            existingUser.setAddress(user.getAddress());
            existingUser.setGender(user.getGender());

            // Resolve role if only id is present (from form binding)
            Role inputRole = user.getRole();
            if (inputRole != null) {
                Long roleId = inputRole.getId();
                if (roleId != null) {
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if (role != null) {
                        existingUser.setRole(role);
                    }
                } else {
                    existingUser.setRole(inputRole);
                }
            } else {
                existingUser.setRole(null);
            }

            existingUser.setIsActive(user.getIsActive());
            existingUser.setUpdatedAt(java.time.LocalDateTime.now());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            return userRepository.save(existingUser);
        }
        return null;
    }

    public void delete(Long id) {
        if (id == null) {
            return;
        }
        userRepository.deleteById(id);
    }

    public User registerDTOtoUser(RegisterDTO registerDTO) {
        Objects.requireNonNull(registerDTO, "registerDTO must not be null");
        User user = new User();
        user.setFullName(registerDTO.getFullName());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setGender(registerDTO.getGender());
        user.setAddress(registerDTO.getAddress());
        user.setPassword(registerDTO.getPassword());
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setIsActive(true);
        return user;
    }

    public Long countTotalUser() {
        return userRepository.count();
    }

    public Long countActiveUser() {
        return userRepository.countByIsActiveTrue();
    }

    public Long countUserThisMonth() {
        return userRepository.countUserThisMonth();
    }

    public List<Object[]> countUserByDateRange(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return userRepository.countUserByDateRange(startDate);
    }

    public List<Object[]> countUsersByRole() {
        return userRepository.countUsersByRole();
    }

    public Page<User> searchUsers(String keyword, Long roleId, String statusStr, int pageNo) {
        int pageSize = 6;
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

        Boolean status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            if ("active".equalsIgnoreCase(statusStr)) {
                status = true;
            } else if ("inactive".equalsIgnoreCase(statusStr)) {
                status = false;
            }
        }

        return userRepository.searchUsers(keyword, roleId, status, pageable);
    }
}
