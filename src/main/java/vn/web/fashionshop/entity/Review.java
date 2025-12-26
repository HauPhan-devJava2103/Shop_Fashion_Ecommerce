package vn.web.fashionshop.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews", uniqueConstraints = {
                @UniqueConstraint(name = "uk_review_order_item", columnNames = { "order_item_id" })
}, indexes = {
                @Index(name = "idx_review_product", columnList = "product_id"),
                @Index(name = "idx_review_user", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Review {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // Liên kết với OrderItem cụ thể (1 item chỉ được review 1 lần)
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "order_item_id", nullable = false, unique = true)
        @JsonIgnore
        private OrderItem orderItem;

        // Giữ lại product_id để query nhanh (denormalize)
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_id", nullable = false)
        @JsonIgnore
        private Product product;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        @JsonIgnore
        private User user;

        @Column(nullable = false)
        private Integer rating; // 1-5

        @Column(columnDefinition = "TEXT")
        private String comment;

        @Column(name = "image_url", length = 500)
        private String imageUrl;

        // Admin duyệt review hay không
        @Column(name = "is_approved", nullable = false)
        private Boolean isApproved = false;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        public String getProductName() {
                return product != null ? product.getProductName() : null;
        }

        public String getProductSku() {
                return product != null ? product.getSku() : null;
        }

        public String getUserFullName() {
                return user != null ? user.getFullName() : null;
        }

        public String getUserEmail() {
                return user != null ? user.getEmail() : null;
        }
}
