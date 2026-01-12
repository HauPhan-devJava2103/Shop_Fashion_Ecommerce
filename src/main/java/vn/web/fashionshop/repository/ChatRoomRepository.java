package vn.web.fashionshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.web.fashionshop.entity.User;
import vn.web.fashionshop.entity.chat.ChatRoom;
import vn.web.fashionshop.enums.EChatRoomStatus;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // Tìm room theo user (member)
    Optional<ChatRoom> findByUserAndStatus(User user, EChatRoomStatus status);

    // Tìm room theo guest session ID
    Optional<ChatRoom> findByGuestSessionIdAndStatus(String guestSessionId, EChatRoomStatus status);

    // Tìm tất cả room active hoặc waiting (cho admin)
    @Query("SELECT r FROM ChatRoom r WHERE r.status IN :statuses ORDER BY r.lastMessageAt DESC")
    List<ChatRoom> findByStatusIn(@Param("statuses") List<EChatRoomStatus> statuses);

    // Tìm rooms được giao cho staff
    List<ChatRoom> findByAssignedStaffAndStatus(User staff, EChatRoomStatus status);

    // Tìm rooms chờ xử lý (chưa có ai nhận)
    @Query("SELECT r FROM ChatRoom r WHERE r.status = :status AND r.assignedStaff IS NULL ORDER BY r.createdAt ASC")
    List<ChatRoom> findWaitingRooms(@Param("status") EChatRoomStatus status);

    // Đếm số rooms chờ xử lý
    @Query("SELECT COUNT(r) FROM ChatRoom r WHERE r.status = :status AND r.assignedStaff IS NULL")
    Long countWaitingRooms(@Param("status") EChatRoomStatus status);

    // Tìm rooms đã đóng (lịch sử)
    List<ChatRoom> findByStatusOrderByLastMessageAtDesc(EChatRoomStatus status);
}
