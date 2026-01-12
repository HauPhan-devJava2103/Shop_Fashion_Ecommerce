package vn.web.fashionshop.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import vn.web.fashionshop.dto.ChatMessageDTO;
import vn.web.fashionshop.dto.ChatRoomDTO;
import vn.web.fashionshop.dto.GuestRoomRequest;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.entity.chat.ChatRoom;
import vn.web.fashionshop.service.ChatService;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    // tạo room mới cho guest
    @PostMapping("/rooms/guest")
    public ResponseEntity<ChatRoomDTO> createGuestRoom(@RequestBody GuestRoomRequest request) {
        ChatRoom room = chatService.getOrCreateRoomForGuest(
                request.getSessionId(),
                request.getName(),
                request.getEmail());
        return ResponseEntity.ok(chatService.toRoomDTO(room));
    }

    // tạo room cho member đã đăng nhập
    @PostMapping("/rooms/member")
    public ResponseEntity<ChatRoomDTO> createMemberRoom(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        User user = chatService.getUserByEmail(email);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        ChatRoom room = chatService.getOrCreateRoomForMember(user);
        return ResponseEntity.ok(chatService.toRoomDTO(room));
    }

    // Lấy thông tin room
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomDTO> getRoom(@PathVariable Long roomId) {
        return chatService.getRoomById(roomId)
                .map(room -> ResponseEntity.ok(chatService.toRoomDTO(room)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Lấy tin nhắn room
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable Long roomId) {
        List<ChatMessageDTO> dtos = chatService.getMessages(roomId).stream()
                .map(chatService::toMessageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Đóng room
    @PutMapping("/rooms/{roomId}/close")
    public ResponseEntity<ChatRoomDTO> closeRoom(@PathVariable Long roomId) {
        ChatRoom room = chatService.closeRoom(roomId);
        return ResponseEntity.ok(chatService.toRoomDTO(room));
    }

}
