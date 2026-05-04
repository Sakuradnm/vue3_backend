package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.entity.User;
import com.example.vue3_backend.entity.UserNotification;
import com.example.vue3_backend.repository.UserRepository;
import com.example.vue3_backend.repository.UserNotificationRepository;
import com.example.vue3_backend.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserNotificationServiceImpl implements UserNotificationService {

    @Autowired
    private UserNotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.example.vue3_backend.service.AdminNoticeService adminNoticeService;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUserNotifications(Long userId) {
        List<UserNotification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // 过滤掉讲师申请类型的通知（这类通知只在管理员通知表显示）
        return notifications.stream()
            .filter(notification -> !"lecturer_application".equals(notification.getNotificationType()))
            .map(notification -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notification.getId());
            map.put("userId", notification.getUser().getId());
            map.put("notificationType", notification.getNotificationType());
            map.put("title", notification.getTitle());
            map.put("content", notification.getContent());
            map.put("relatedType", notification.getRelatedType());
            map.put("relatedId", notification.getRelatedId());
            map.put("isRead", notification.getIsRead());
            map.put("status", notification.getStatus());
            map.put("adminComment", notification.getAdminComment());
            map.put("createdAt", notification.getCreatedAt());
            map.put("readAt", notification.getReadAt());
            
            // 发送者信息（已经在事务中，可以访问延迟加载的对象）
            if (notification.getSender() != null) {
                map.put("senderId", notification.getSender().getId());
                map.put("senderName", notification.getSender().getNickname() != null ? 
                    notification.getSender().getNickname() : notification.getSender().getUsername());
                map.put("senderAvatar", notification.getSender().getAvatarUrl());
            }
            
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        UserNotification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("通知不存在"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作此通知");
        }
        
        notificationRepository.markAsRead(notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.deleteByIdAndUserId(notificationId, userId);
    }

    @Override
    @Transactional
    public void createCommentReplyNotification(Long recipientUserId, Long senderUserId, 
                                               String senderName, Long postId, String postTitle) {
        System.out.println("[DEBUG] 创建评论回复通知: 接收者ID=" + recipientUserId + ", 发送者ID=" + senderUserId);
        
        User recipient = userRepository.findById(recipientUserId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        User sender = userRepository.findById(senderUserId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        UserNotification notification = new UserNotification();
        notification.setUser(recipient);
        notification.setSender(sender);
        notification.setNotificationType("comment_reply");
        notification.setTitle("有人回复了您的评论");
        notification.setContent(senderName + " 回复了您在 \"" + postTitle + "\" 帖子的评论");
        notification.setRelatedType("forum_post");
        notification.setRelatedId(postId);
        notification.setIsRead(false);
        
        UserNotification savedNotification = notificationRepository.save(notification);
        System.out.println("[DEBUG] 通知保存成功，ID=" + savedNotification.getId());
    }

    @Override
    @Transactional
    public void createLikeNotification(Long recipientUserId, Long senderUserId, 
                                      String senderName, Long commentId, String contentPreview) {
        User recipient = userRepository.findById(recipientUserId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        User sender = userRepository.findById(senderUserId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        UserNotification notification = new UserNotification();
        notification.setUser(recipient);
        notification.setSender(sender);
        notification.setNotificationType("like_comment");
        notification.setTitle("有人点赞了您的评论");
        notification.setContent(senderName + " 点赞了您的评论：" + 
            (contentPreview != null && !contentPreview.isEmpty() ? contentPreview : ""));
        notification.setRelatedType("forum_comment");
        notification.setRelatedId(commentId);
        notification.setIsRead(false);
        
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createLecturerApplicationNotification(Long applicantUserId, String reason) {
        System.out.println("[DEBUG] 开始创建讲师申请通知: 申请人ID=" + applicantUserId);
        
        User applicant = userRepository.findById(applicantUserId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取申请人姓名（优先使用昵称）
        String applicantName = applicant.getNickname() != null ? 
            applicant.getNickname() : applicant.getUsername();
        
        // 创建讲师申请通知到admin_notice表（给管理员看）
        System.out.println("[DEBUG] 正在创建admin_notice通知...");
        adminNoticeService.createTeacherApplyNotice(
            applicantName, 
            applicantUserId.intValue(),
            reason // 传递申请理由
        );
        System.out.println("[DEBUG] admin_notice通知创建成功");
        
        System.out.println("[DEBUG] 讲师申请通知创建完成，已发送到admin_notice表");
    }

    @Override
    @Transactional
    public void approveLecturerApplication(Long notificationId, Integer adminId) {
        // 从admin_notice表获取通知
        com.example.vue3_backend.entity.AdminNotice notice = adminNoticeService.getById(notificationId.intValue());
        
        if (notice == null) {
            throw new RuntimeException("通知不存在");
        }
        
        if (notice.getIsRead() != null && notice.getIsRead() == 1) {
            throw new RuntimeException("该申请已经处理过了");
        }
        
        // 标记通知为已读
        notice.setIsRead(1);
        notice.setReadAt(java.time.LocalDateTime.now());
        adminNoticeService.save(notice);
        
        // 更新用户等级为teacher
        Long applicantUserId = notice.getRelatedId().longValue();
        User applicant = userRepository.findById(applicantUserId)
            .orElseThrow(() -> new RuntimeException("申请人不存在"));
        applicant.setLevel(User.Level.teacher);
        userRepository.save(applicant);
        
        // 给申请人发送通过通知到user_notifications表
        UserNotification successNotification = new UserNotification();
        successNotification.setUser(applicant);
        successNotification.setNotificationType("admin_reply");
        successNotification.setTitle("讲师申请通过");
        successNotification.setContent("恭喜您！您的讲师申请已通过审核，现在您可以发布课程了。");
        successNotification.setRelatedType("lecturer_application");
        successNotification.setRelatedId(applicantUserId);
        successNotification.setIsRead(false);
        successNotification.setStatus(1);
        
        notificationRepository.save(successNotification);
    }

    @Override
    @Transactional
    public void rejectLecturerApplication(Long notificationId, Integer adminId, String comment) {
        // 从admin_notice表获取通知
        com.example.vue3_backend.entity.AdminNotice notice = adminNoticeService.getById(notificationId.intValue());
        
        if (notice == null) {
            throw new RuntimeException("通知不存在");
        }
        
        if (notice.getIsRead() != null && notice.getIsRead() == 1) {
            throw new RuntimeException("该申请已经处理过了");
        }
        
        // 标记通知为已读
        notice.setIsRead(1);
        notice.setReadAt(java.time.LocalDateTime.now());
        adminNoticeService.save(notice);
        
        // 给申请人发送拒绝通知到user_notifications表
        Long applicantUserId = notice.getRelatedId().longValue();
        User applicant = userRepository.findById(applicantUserId)
            .orElseThrow(() -> new RuntimeException("申请人不存在"));
        
        UserNotification rejectNotification = new UserNotification();
        rejectNotification.setUser(applicant);
        rejectNotification.setNotificationType("admin_reply");
        rejectNotification.setTitle("讲师申请未通过");
        rejectNotification.setContent("很抱歉，您的讲师申请未通过审核。原因：" + 
            (comment != null ? comment : "不符合要求"));
        rejectNotification.setRelatedType("lecturer_application");
        rejectNotification.setRelatedId(applicantUserId);
        rejectNotification.setIsRead(false);
        rejectNotification.setStatus(2);
        rejectNotification.setAdminComment(comment);
        
        notificationRepository.save(rejectNotification);
    }

    @Override
    public List<Map<String, Object>> getPendingLecturerApplications() {
        // 从admin_notice表查询待处理的讲师申请
        List<com.example.vue3_backend.entity.AdminNotice> notices = adminNoticeService.getNoticesByType("teacher_apply");
        
        return notices.stream().map(notice -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notice.getId());
            map.put("applicantId", notice.getRelatedId());
            
            // 获取申请人信息
            User applicant = null;
            if (notice.getRelatedId() != null) {
                applicant = userRepository.findById(notice.getRelatedId().longValue()).orElse(null);
            }
            
            if (applicant != null) {
                map.put("applicantName", applicant.getNickname() != null ? 
                    applicant.getNickname() : applicant.getUsername());
                map.put("applicantAvatar", applicant.getAvatarUrl());
            } else {
                map.put("applicantName", "未知用户");
                map.put("applicantAvatar", null);
            }
            
            map.put("content", notice.getContent());
            map.put("createdAt", notice.getCreatedAt());
            map.put("isRead", notice.getIsRead());
            map.put("priority", notice.getPriority());
            return map;
        }).collect(Collectors.toList());
    }
}
