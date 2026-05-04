package com.example.vue3_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_notifications")
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 接收通知的用户

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;  // 发送通知的用户（可为空）

    @Column(name = "notification_type", length = 50, nullable = false)
    private String notificationType;  // 通知类型

    @Column(name = "title", length = 200, nullable = false)
    private String title;  // 通知标题

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;  // 通知内容

    @Column(name = "related_type", length = 50)
    private String relatedType;  // 关联类型

    @Column(name = "related_id")
    private Long relatedId;  // 关联ID

    @Column(name = "is_read")
    private Boolean isRead = false;  // 是否已读

    @Column(name = "status")
    private Integer status;  // 状态：0-待处理, 1-通过, 2-拒绝

    @Column(name = "admin_comment", columnDefinition = "TEXT")
    private String adminComment;  // 管理员备注/拒绝原因

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
