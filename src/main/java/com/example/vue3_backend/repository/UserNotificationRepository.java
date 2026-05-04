package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    
    // 查询用户的通知列表（按创建时间倒序）
    @Query("SELECT n FROM UserNotification n JOIN FETCH n.user WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    List<UserNotification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // 查询用户的未读通知数量
    @Query("SELECT COUNT(n) FROM UserNotification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    // 查询特定类型的通知
    @Query("SELECT n FROM UserNotification n JOIN FETCH n.user WHERE n.user.id = :userId AND n.notificationType = :type ORDER BY n.createdAt DESC")
    List<UserNotification> findByUserIdAndTypeOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("type") String type);
    
    // 标记通知为已读
    @Modifying
    @Query("UPDATE UserNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId);
    
    // 标记用户的所有通知为已读
    @Modifying
    @Query("UPDATE UserNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
    
    // 删除通知
    @Modifying
    @Query("DELETE FROM UserNotification n WHERE n.id = :notificationId AND n.user.id = :userId")
    void deleteByIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
    
    // 查询待处理的讲师申请通知（管理员用）
    @Query("SELECT n FROM UserNotification n JOIN FETCH n.user WHERE n.notificationType = 'lecturer_application' AND n.status = 0 ORDER BY n.createdAt DESC")
    List<UserNotification> findPendingLecturerApplications();
}
