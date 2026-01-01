package vn.web.fashionshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import vn.web.fashionshop.entity.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Page<Voucher> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByCodeIgnoreCase(String code);
}
