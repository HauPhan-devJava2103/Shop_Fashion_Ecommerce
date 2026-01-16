package vn.web.fashionshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    // Tìm transactions theo payment ID
    List<PaymentTransaction> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);
}
