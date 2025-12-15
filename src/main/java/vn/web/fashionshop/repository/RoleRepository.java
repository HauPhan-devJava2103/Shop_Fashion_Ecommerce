package vn.web.fashionshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.Role;
import vn.web.fashionshop.enums.ERoleName;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(ERoleName roleName);
}
