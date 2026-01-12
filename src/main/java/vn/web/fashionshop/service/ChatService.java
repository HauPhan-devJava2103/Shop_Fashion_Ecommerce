package vn.web.fashionshop.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.web.fashionshop.dto.ChatMessageDTO;
import vn.web.fashionshop.dto.ChatRoomDTO;
import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.entity.chat.ChatMessage;
import vn.web.fashionshop.entity.chat.ChatRoom;
import vn.web.fashionshop.enums.EChatMessageType;
import vn.web.fashionshop.enums.EChatRoomStatus;
import vn.web.fashionshop.repository.ChatMessageRepository;
import vn.web.fashionshop.repository.ChatRoomRepository;
import vn.web.fashionshop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    // Lấy user theo email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // Tạo hoặc lấy room member
    @Transactional
    public ChatRoom getOrCreateRoomForMember(User user) {
        // Kiểm tra xem đã có room active chưa
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUserAndStatus(user, EChatRoomStatus.ACTIVE);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Kiểm tra room đang chờ
        existingRoom = chatRoomRepository.findByUserAndStatus(user, EChatRoomStatus.WAITING);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Tạo room mới
        ChatRoom room = new ChatRoom();
        room.setUser(user);
        room.setIsGuest(false);
        room.setStatus(EChatRoomStatus.WAITING);
        room.setCreatedAt(LocalDateTime.now());
        room.setLastMessageAt(LocalDateTime.now());

        return chatRoomRepository.save(room);
    }

    // Tạo hoặc lấy room cho guest (chưa đăng ký)
    @Transactional
    public ChatRoom getOrCreateRoomForGuest(String sessionId, String guestName, String guestEmail) {
        // Nếu không có sessionId thì tạo mới
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        // Kiểm tra xem đã có room active chưa
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByGuestSessionIdAndStatus(sessionId,
                EChatRoomStatus.ACTIVE);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Kiểm tra room đang chờ
        existingRoom = chatRoomRepository.findByGuestSessionIdAndStatus(sessionId, EChatRoomStatus.WAITING);
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Tạo room mới
        ChatRoom room = new ChatRoom();
        room.setGuestSessionId(sessionId);
        room.setGuestName(guestName);
        room.setGuestEmail(guestEmail);
        room.setIsGuest(true);
        room.setStatus(EChatRoomStatus.WAITING);
        room.setCreatedAt(LocalDateTime.now());
        room.setLastMessageAt(LocalDateTime.now());

        return chatRoomRepository.save(room);
    }

    // Lấy room theo ID
    public Optional<ChatRoom> getRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId);
    }

    // Đóng room
    @Transactional
    public ChatRoom closeRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setStatus(EChatRoomStatus.CLOSED);
        return chatRoomRepository.save(room);
    }

    // Giao room cho staff
    @Transactional
    public ChatRoom assignStaff(Long roomId, User staff) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setAssignedStaff(staff);
        room.setStatus(EChatRoomStatus.ACTIVE);
        return chatRoomRepository.save(room);
    }

    // Lấy tất cả rooms đang active hoặc waiting (cho admin)
    public List<ChatRoom> getActiveRooms() {
        return chatRoomRepository.findByStatusIn(
                Arrays.asList(EChatRoomStatus.WAITING, EChatRoomStatus.ACTIVE));
    }

    // Lấy tất cả rooms đã đóng (lịch sử chat)
    public List<ChatRoom> getClosedRooms() {
        return chatRoomRepository.findByStatusOrderByLastMessageAtDesc(EChatRoomStatus.CLOSED);
    }

    // Đếm số rooms đang chờ
    public Long countWaitingRooms() {
        return chatRoomRepository.countWaitingRooms(EChatRoomStatus.WAITING);
    }

    // Gửi tin nhắn
    @Transactional
    public ChatMessage sendMessage(Long roomId, String senderName, String content,
            boolean isFromStaff, User sender) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setSender(sender);
        message.setSenderName(senderName);
        message.setContent(content);
        message.setType(EChatMessageType.TEXT);
        message.setIsFromStaff(isFromStaff);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);

        // Cập nhật thời gian tin nhắn cuối
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        return chatMessageRepository.save(message);
    }

    // Gửi tin nhắn hệ thống
    @Transactional
    public ChatMessage sendSystemMessage(Long roomId, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setSenderName("Hệ thống");
        message.setContent(content);
        message.setType(EChatMessageType.SYSTEM);
        message.setIsFromStaff(true);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);

        return chatMessageRepository.save(message);
    }

    // Lấy tin nhắn của room
    public List<ChatMessage> getMessages(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(room);
    }

    // Đánh dấu đã đọc
    @Transactional
    public void markMessagesAsRead(Long roomId, boolean staffReading) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        // Nếu staff đang đọc, mark tin nhắn từ customer là đã đọc
        chatMessageRepository.markAsRead(room, !staffReading);
    }

    // Đếm tin nhắn chưa đọc
    public Long countUnreadMessages(Long roomId, boolean isStaffReading) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        // Nếu staff đang đọc, đếm tin từ customer chưa đọc
        return chatMessageRepository.countUnreadMessages(room, !isStaffReading);
    }

    // ========== DTO CONVERSIONS ==========

    public ChatRoomDTO toRoomDTO(ChatRoom room) {
        return ChatRoomDTO.builder()
                .id(room.getId())
                .userId(room.getUser() != null ? room.getUser().getId() : null)
                .guestSessionId(room.getGuestSessionId())
                .displayName(room.getDisplayName())
                .email(room.getIsGuest() ? room.getGuestEmail()
                        : (room.getUser() != null ? room.getUser().getEmail() : null))
                .isGuest(room.getIsGuest())
                .status(room.getStatus())
                .assignedStaffId(room.getAssignedStaff() != null ? room.getAssignedStaff().getId() : null)
                .assignedStaffName(room.getAssignedStaff() != null ? room.getAssignedStaff().getFullName() : null)
                .createdAt(room.getCreatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .build();
    }

    public ChatMessageDTO toMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSenderName())
                .content(message.getContent())
                .type(message.getType())
                .isFromStaff(message.getIsFromStaff())
                .createdAt(message.getCreatedAt())
                .isRead(message.getIsRead())
                .build();
    }
}
