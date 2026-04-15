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
@Table(name = "forum_comments")
public class ForumComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "post_id", nullable = false)
    private Integer postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private ForumPost post;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "parent_id", nullable = false)
    private Integer parentId = 0;

    private String content;

    @Column(name = "likes")
    private Integer likes = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
