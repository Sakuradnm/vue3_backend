package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.ForumCommentDTO;
import java.util.List;
import java.util.Map;

public interface ForumCommentService {

    List<ForumCommentDTO> getCommentsByPostId(Integer postId);

    ForumCommentDTO createComment(Map<String, Object> commentData);

    boolean toggleLikeComment(Integer commentId, Long userId, String action);

    boolean isCommentLiked(Integer commentId, Long userId);

    void deleteComment(Integer commentId, Long currentUserId);
    
    ForumCommentDTO getCommentById(Integer commentId);
}
