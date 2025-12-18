package vn.web.fashionshop.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

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

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
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
        if (email == null || email.isBlank() || phone == null || phone.isBlank()) {
            return null;
        }
        // Check duplicate email and phone
        if (checkEmailExist(email)) {
            return null;
        }
        if (checkPhoneExist(phone)) {
            return null;
        }

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
            existingUser.setRole(user.getRole());
            existingUser.setIsActive(user.getIsActive());
            existingUser.setUpdatedAt(java.time.LocalDateTime.now());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // TODO: Encrypt password here
                existingUser.setPassword(user.getPassword());
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
}
