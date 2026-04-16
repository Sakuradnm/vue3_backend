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
@Table(name = "statistics_overview")
public class StatisticsOverview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "total_courses", nullable = false)
    private Integer totalCourses = 0;

    @Column(name = "total_users", nullable = false)
    private Integer totalUsers = 0;

    @Column(name = "total_posts", nullable = false)
    private Integer totalPosts = 0;

    @Column(name = "total_sub_categories", nullable = false)
    private Integer totalSubCategories = 0;

    @Column(name = "total_categories", nullable = false)
    private Integer totalCategories = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
