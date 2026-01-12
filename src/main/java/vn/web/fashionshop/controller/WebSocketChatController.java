package vn.web.fashionshop.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import vn.web.fashionshop.dto.ChatMessageDTO;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.entity.chat.ChatMessage;
import vn.web.fashionshop.service.ChatService;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // Nhận tin nhắn từ client và broadcast cho tất cả subscribers của room
    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageDTO sendMessage(@DestinationVariable Long roomId,
            @Payload ChatMessageDTO messageDTO,
            java.security.Principal principal) {

        // Lấy user từ authentication nếu có
        User sender = null;
        String senderName = messageDTO.getSenderName();

        if (principal != null && principal.getName() != null) {
            sender = chatService.getUserByEmail(principal.getName());
            if (sender != null && !messageDTO.getIsFromStaff()) {
                // Nếu là customer đã đăng nhập, dùng tên của họ
                senderName = sender.getFullName();
            }
        }

        // Lưu tin nhắn vào database
        ChatMessage savedMessage = chatService.sendMessage(
                roomId,
                senderName,
                messageDTO.getContent(),
                messageDTO.getIsFromStaff(),
                sender);

        // Chuyển đổi thành DTO để trả về
        return ChatMessageDTO.builder()
                .id(savedMessage.getId())
                .roomId(roomId)
                .senderId(sender != null ? sender.getId() : null)
                .senderName(savedMessage.getSenderName())
                .content(savedMessage.getContent())
                .type(savedMessage.getType())
                .isFromStaff(savedMessage.getIsFromStaff())
                .createdAt(savedMessage.getCreatedAt())
                .isRead(savedMessage.getIsRead())
                .build();
    }

    /**
     * Thông báo người dùng đang gõ
     * Client gửi đến: /app/chat.typing/{roomId}
     * Broadcast đến: /topic/room/{roomId}/typing
     */
    @MessageMapping("/chat.typing/{roomId}")
    @SendTo("/topic/room/{roomId}/typing")
    public String notifyTyping(@DestinationVariable Long roomId,
            @Payload String senderName) {
        return senderName;
    }

    // Thông báo cho admin khi có tin nhắn mới từ customer
    public void notifyAdminNewMessage(Long roomId, ChatMessageDTO message) {
        messagingTemplate.convertAndSend("/topic/admin/new-message", message);
    }

    // Thông báo cho admin khi có room mới
    public void notifyAdminNewRoom(Long roomId) {
        messagingTemplate.convertAndSend("/topic/admin/new-room", roomId);
    }
}
