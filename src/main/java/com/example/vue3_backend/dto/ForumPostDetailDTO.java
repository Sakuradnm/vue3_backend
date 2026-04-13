package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostDetailDTO {
    private Integer id;
    private Integer userId;  // 帖子所有者ID
    private String category;
    private String categoryLabel;
    private String title;
    private String subtitle;
    private String preview;
    private String content;
    private String author; // 用户昵称
    private String avatar; // 用户头像URL
    private String createdAt;
    private Integer views;
    private Integer likes;
    private Integer comments;
    private List<String> tags;
    private Boolean pinned;
    private Boolean solved;
    private Boolean hot;
    private Integer score;
}
