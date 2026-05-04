package com.example.vue3_backend.service;

import com.example.vue3_backend.entity.UserNotification;
import java.util.List;
import java.util.Map;

public interface UserNotificationService {
    
    // 获取用户的通知列表
    List<Map<String, Object>> getUserNotifications(Long userId);
    
    // 获取未读通知数量
    Long getUnreadCount(Long userId);
    
    // 标记通知为已读
    void markAsRead(Long notificationId, Long userId);
    
    // 标记所有通知为已读
    void markAllAsRead(Long userId);
    
    // 删除通知
    void deleteNotification(Long notificationId, Long userId);
    
    // 创建评论回复通知
    void createCommentReplyNotification(Long recipientUserId, Long senderUserId, 
                                        String senderName, Long postId, String postTitle);
    
    // 创建点赞通知
    void createLikeNotification(Long recipientUserId, Long senderUserId, 
                               String senderName, Long commentId, String contentPreview);
    
    // 创建讲师申请通知（发送给管理员）
    void createLecturerApplicationNotification(Long applicantUserId, String reason);
    
    // 审核讲师申请（通过/拒绝）
    void approveLecturerApplication(Long notificationId, Integer adminId);
    void rejectLecturerApplication(Long notificationId, Integer adminId, String comment);
    
    // 获取待处理的讲师申请列表（管理员用）
    List<Map<String, Object>> getPendingLecturerApplications();
}
