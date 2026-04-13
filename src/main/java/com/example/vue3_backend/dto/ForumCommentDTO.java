package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumCommentDTO {
    private Integer id;
    private Integer postId;
    private Long userId;
    private String username;
    private String avatar;
    private Integer parentId;
    private String content;
    private Integer likes;
    private String createdAt;
    private List<ForumCommentDTO> children;
}
