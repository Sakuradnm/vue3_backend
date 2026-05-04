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
@Table(name = "course_chapters3")
public class ChapterResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private CourseChapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    // 注意：数据库表course_chapters3中没有course_id字段，只通过chapter_id和section_id关联

    @Column(name = "resource_type", length = 20, nullable = false)
    private String resourceType;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    // fileName字段已从数据库中删除，使用Transient标记不映射到数据库
    @Transient
    private String fileName;

    @Column(name = "resource_url", columnDefinition = "TEXT", nullable = false)
    private String resourceUrl;

    // duration字段已从数据库中删除，使用Transient标记不映射到数据库
    @Transient
    private Integer duration = 0;

    // fileSize字段已从数据库中删除，使用Transient标记不映射到数据库
    @Transient
    private Long fileSize;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
