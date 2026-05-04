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
@Table(name = "admin_course_review")
public class CourseReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sub_category_id")
    private Integer subCategoryId;

    @Column(name = "category_id")
    private Integer categoryId;

    // 详细信息字段

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "learning_objectives", columnDefinition = "TEXT")
    private String learningObjectives;

    @Column(name = "main_content", columnDefinition = "TEXT")
    private String mainContent;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Column(name = "teaching_features", columnDefinition = "TEXT")
    private String teachingFeatures;

    @Column(name = "instructor", length = 200)
    private String instructor;

    // JSON格式存储章节数据
    @Column(name = "chapters_data", columnDefinition = "LONGTEXT")
    private String chaptersData;

    // 审核状态：0-待审核，1-已通过，2-已拒绝
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Column(name = "reviewer_id")
    private Integer reviewerId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
