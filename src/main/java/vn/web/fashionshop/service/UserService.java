package vn.web.fashionshop.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
        return userRepository.findById(id).orElse(null);
    }

    public Role getRoleByName(ERoleName roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    public boolean checkEmailExist(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkPhoneExist(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User create(User user) {
        // Check duplicate email and phone
        if (checkEmailExist(user.getEmail())) {
            return null;
        }
        if (checkPhoneExist(user.getPhone())) {
            return null;
        }

        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setIsActive(true);
        return userRepository.save(user);
    }

    public User update(User user) {
        if (user.getId() == null) {
            return null;
        }
        User existingUser = getUserById(user.getId());
        if (existingUser != null) {
            // Check Duplicate Email
            if (!existingUser.getEmail().equals(user.getEmail())) {
                if (checkEmailExist(user.getEmail())) {
                    return null; // Email taken by another user
                }
                existingUser.setEmail(user.getEmail());
            }

            // Check Duplicate Phone
            if (!existingUser.getPhone().equals(user.getPhone())) {
                if (checkPhoneExist(user.getPhone())) {
                    return null; // Phone taken by another user
                }
                existingUser.setPhone(user.getPhone());
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
        if (id != null) {
            userRepository.deleteById(id);
        }
    }

    public User registerDTOtoUser(RegisterDTO registerDTO) {
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
