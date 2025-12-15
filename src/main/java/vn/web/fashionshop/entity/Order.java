package vn.web.fashionshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.web.fashionshop.enums.EOrderStatus;
import vn.web.fashionshop.enums.EPaymentMethod;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // COD / Bank_Transfer
    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private EPaymentMethod paymentMethod;

    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EOrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval =
    true, fetch = FetchType.LAZY)
    private OrderAddress orderAddress;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval =
    true, fetch = FetchType.LAZY)
    private Payment payment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}