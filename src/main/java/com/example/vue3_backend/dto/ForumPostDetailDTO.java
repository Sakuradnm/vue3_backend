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
    private String preview;
    private String content;
    private String author;
    private String avatar;
    private String createdAt;
    private Integer views;
    private Integer likes;
    private Integer comments;
    private List<String> tags;
    private Boolean hot;
    private Integer score;
}
