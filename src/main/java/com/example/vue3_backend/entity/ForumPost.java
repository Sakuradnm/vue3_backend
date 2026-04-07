package com.example.vue3_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_posts")
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "category", length = 20, nullable = false)
    private String category;

    @Column(name = "category_label", length = 20, nullable = false)
    private String categoryLabel;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "preview", columnDefinition = "TEXT")
    private String preview;

    @Column(name = "author", length = 50, nullable = false)
    private String author;

    @Column(name = "avatar", length = 10)
    private String avatar;

    @Column(name = "avatar_color", length = 7)
    private String avatarColor;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "views")
    private Integer views = 0;

    @Column(name = "likes")
    private Integer likes = 0;

    @Column(name = "comments")
    private Integer comments = 0;

    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    @Column(name = "pinned")
    private Boolean pinned = false;

    @Column(name = "solved")
    private Boolean solved = false;

    @Column(name = "hot")
    private Boolean hot = false;

    @Column(name = "score")
    private Integer score = 0;
}
