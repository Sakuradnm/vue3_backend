package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.ForumCommentDTO;
import java.util.List;
import java.util.Map;

public interface ForumCommentService {

    List<ForumCommentDTO> getCommentsByPostId(Integer postId);

    ForumCommentDTO createComment(Map<String, Object> commentData);

    void likeComment(Integer id);
}
