package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumPostDTO {
    private Integer id;
    private String category;
    private String categoryLabel;
    private String title;
    private String preview;
    private String author;
    private String avatar;
    private LocalDateTime createdAt;
    private Integer views;
    private Integer likes;
    private Integer comments;
    private List<String> tags;
    private Boolean hot;
    private Integer score;
    private String timeAgo;
}
