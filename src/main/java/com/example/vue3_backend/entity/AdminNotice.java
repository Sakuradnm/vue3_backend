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
@Table(name = "admin_notice")
public class AdminNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // 通知类型: course_review-课程审核, user_register-用户注册, teacher_apply-讲师申请
    @Column(name = "notice_type", length = 50, nullable = false)
    private String noticeType;

    // 通知标题
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    // 通知内容（简短描述）
    @Column(name = "content", length = 500, nullable = false)
    private String content;

    // 关联记录ID（如审核记录ID、用户ID等）
    @Column(name = "related_id")
    private Integer relatedId;

    // 是否已读: 0-未读, 1-已读
    @Column(name = "is_read")
    private Integer isRead = 0;

    // 优先级: 0-普通, 1-重要, 2-紧急
    @Column(name = "priority")
    private Integer priority = 0;

    // 创建时间
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 阅读时间
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = 0;
        }
        if (priority == null) {
            priority = 0;
        }
    }
}
