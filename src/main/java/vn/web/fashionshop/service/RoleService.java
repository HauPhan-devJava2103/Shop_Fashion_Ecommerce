package vn.web.fashionshop.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vn.web.fashionshop.entity.Role;
import vn.web.fashionshop.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
