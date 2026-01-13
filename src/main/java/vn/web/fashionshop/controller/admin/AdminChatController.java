package vn.web.fashionshop.controller.admin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import vn.web.fashionshop.dto.ChatMessageDTO;
import vn.web.fashionshop.dto.ChatRoomDTO;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.entity.chat.ChatRoom;
import vn.web.fashionshop.service.ChatService;

@Controller
@RequestMapping("/admin/chat")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class AdminChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // Trang quản lý chat
    @GetMapping
    public String chatDashboard(Model model) {
        List<ChatRoomDTO> roomDTOs = chatService.getActiveRooms().stream()
                .map(chatService::toRoomDTO)
                .collect(Collectors.toList());

        model.addAttribute("rooms", roomDTOs);
        model.addAttribute("waitingCount", chatService.countWaitingRooms());

        return "admin/chat/index";
    }

    // API: Lấy danh sách rooms
    @GetMapping("/api/rooms")
    @ResponseBody
    public List<ChatRoomDTO> getRooms() {
        return chatService.getActiveRooms().stream()
                .map(chatService::toRoomDTO)
                .collect(Collectors.toList());
    }

    // API: Lấy tin nhắn của room
    @GetMapping("/api/rooms/{roomId}/messages")
    @ResponseBody
    public List<ChatMessageDTO> getMessages(@PathVariable Long roomId) {
        return chatService.getMessages(roomId).stream()
                .map(chatService::toMessageDTO)
                .collect(Collectors.toList());
    }

    // API: Đóng room
    @PutMapping("/api/rooms/{roomId}/close")
    @ResponseBody
    public ResponseEntity<ChatRoomDTO> closeRoom(@PathVariable Long roomId) {
        ChatRoom room = chatService.closeRoom(roomId);

        // Gửi thông báo WebSocket cho khách hàng
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/closed", "closed");

        return ResponseEntity.ok(chatService.toRoomDTO(room));
    }

    // API: Nhận chat (assign staff)
    @PutMapping("/api/rooms/{roomId}/accept")
    @ResponseBody
    public ResponseEntity<ChatRoomDTO> acceptChat(@PathVariable Long roomId,
            Authentication authentication) {
        // Lấy user hiện tại từ authentication
        String email = authentication.getName();
        User staff = chatService.getUserByEmail(email);

        ChatRoom room = chatService.assignStaff(roomId, staff);

        // Gửi tin nhắn hệ thống
        chatService.sendSystemMessage(roomId, "Nhân viên hỗ trợ đã tham gia cuộc trò chuyện.");

        return ResponseEntity.ok(chatService.toRoomDTO(room));
    }

    // Trang lịch sử chat
    @GetMapping("/history")
    public String chatHistory(Model model) {
        List<ChatRoomDTO> closedRooms = chatService.getClosedRooms().stream()
                .map(chatService::toRoomDTO)
                .collect(Collectors.toList());

        model.addAttribute("rooms", closedRooms);
        return "admin/chat/history";
    }

    // API: Lấy danh sách lịch sử chat
    @GetMapping("/api/history")
    @ResponseBody
    public List<ChatRoomDTO> getHistoryRooms() {
        return chatService.getClosedRooms().stream()
                .map(chatService::toRoomDTO)
                .collect(Collectors.toList());
    }
}
