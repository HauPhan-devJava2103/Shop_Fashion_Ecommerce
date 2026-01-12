package vn.web.fashionshop.entity.chat;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.enums.EChatRoomStatus;

@Entity
@Table(name = "chat_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User đã đăng nhập (null nếu là guest)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Guest session ID (UUID) - dùng cho khách chưa đăng ký
    @Column(name = "guest_session_id", length = 100)
    private String guestSessionId;

    @Column(name = "guest_name", length = 100)
    private String guestName;

    @Column(name = "guest_email", length = 100)
    private String guestEmail;

    @Column(name = "is_guest", nullable = false)
    private Boolean isGuest = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EChatRoomStatus status = EChatRoomStatus.WAITING;

    // Nhân viên/Admin được giao xử lý
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    // Helper method để lấy tên hiển thị
    public String getDisplayName() {
        if (isGuest) {
            return guestName != null ? guestName : "Khách #" + guestSessionId.substring(0, 8);
        }
        return user != null ? user.getFullName() : "Unknown";
    }
}
