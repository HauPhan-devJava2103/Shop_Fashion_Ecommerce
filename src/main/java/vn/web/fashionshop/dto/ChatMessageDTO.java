package vn.web.fashionshop.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.web.fashionshop.enums.EChatMessageType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private EChatMessageType type;
    private Boolean isFromStaff;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
