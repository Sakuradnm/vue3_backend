package com.example.vue3_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_comment_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"comment_id", "user_id"})
})
public class ForumCommentLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "comment_id", nullable = false)
    private Integer commentId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public ForumCommentLike() {
        this.createdAt = LocalDateTime.now();
    }
    
    public ForumCommentLike(Integer commentId, Long userId) {
        this.commentId = commentId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getCommentId() {
        return commentId;
    }
    
    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
