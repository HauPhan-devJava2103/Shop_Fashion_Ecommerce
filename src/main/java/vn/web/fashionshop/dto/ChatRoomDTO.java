package vn.web.fashionshop.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.web.fashionshop.enums.EChatRoomStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {
    private Long id;
    private Long userId;
    private String guestSessionId;
    private String displayName;
    private String email;
    private Boolean isGuest;
    private EChatRoomStatus status;
    private Long assignedStaffId;
    private String assignedStaffName;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private Long unreadCount;
}
