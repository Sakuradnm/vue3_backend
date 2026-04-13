package com.example.vue3_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_post_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class ForumPostLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "post_id", nullable = false)
    private Integer postId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public ForumPostLike() {
        this.createdAt = LocalDateTime.now();
    }
    
    public ForumPostLike(Integer postId, Integer userId) {
        this.postId = postId;
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
    
    public Integer getPostId() {
        return postId;
    }
    
    public void setPostId(Integer postId) {
        this.postId = postId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
