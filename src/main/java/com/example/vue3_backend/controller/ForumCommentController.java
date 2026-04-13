package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.ForumCommentDTO;
import com.example.vue3_backend.service.ForumCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumCommentController {

    @Autowired
    private ForumCommentService forumCommentService;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<ForumCommentDTO>> getComments(@PathVariable Integer postId) {
        List<ForumCommentDTO> comments = forumCommentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/comments")
    public ResponseEntity<ForumCommentDTO> createComment(@RequestBody Map<String, Object> commentData) {
        try {
            ForumCommentDTO createdComment = forumCommentService.createComment(commentData);
            return ResponseEntity.ok(createdComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/comments/{id}/like")
    public ResponseEntity<Void> likeComment(@PathVariable Integer id) {
        forumCommentService.likeComment(id);
        return ResponseEntity.ok().build();
    }
}
