package vn.web.fashionshop.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.chat.ChatMessage;
import vn.web.fashionshop.entity.chat.ChatRoom;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Lấy tin nhắn theo room, sắp xếp theo thời gian
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);

    // Lấy tin nhắn theo room với phân trang
    Page<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);

    // Đếm số tin nhắn chưa đọc trong room
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatRoom = :room AND m.isRead = false AND m.isFromStaff = :isFromStaff")
    Long countUnreadMessages(@Param("room") ChatRoom room, @Param("isFromStaff") Boolean isFromStaff);

    // Đánh dấu tất cả tin nhắn trong room là đã đọc
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.chatRoom = :room AND m.isFromStaff = :isFromStaff")
    void markAsRead(@Param("room") ChatRoom room, @Param("isFromStaff") Boolean isFromStaff);

    // Lấy tin nhắn mới nhất của room
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = :room ORDER BY m.createdAt DESC LIMIT 1")
    ChatMessage findLatestMessage(@Param("room") ChatRoom room);
}
