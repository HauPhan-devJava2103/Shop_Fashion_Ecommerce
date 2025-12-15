package vn.web.fashionshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // tim user theo email
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByPhone(String phone);

    Optional<User> findByPhone(String phone);

}
