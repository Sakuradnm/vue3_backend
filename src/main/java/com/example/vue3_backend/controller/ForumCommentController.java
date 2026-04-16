package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.ForumCommentDTO;
import com.example.vue3_backend.service.ForumCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
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
    public ResponseEntity<Map<String, Object>> likeComment(@PathVariable Integer id, @RequestBody Map<String, Object> likeData) {
        try {
            if (!likeData.containsKey("userId") || likeData.get("userId") == null) {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "缺少用户ID");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Long userId = Long.valueOf(likeData.get("userId").toString());
            String action = likeData.containsKey("action") ? (String) likeData.get("action") : "toggle";
            boolean liked = forumCommentService.toggleLikeComment(id, userId, action);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("liked", liked);
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "用户ID格式错误");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/comments/{id}/like-status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable Integer id, @RequestParam Long userId) {
        try {
            boolean liked = forumCommentService.isCommentLiked(id, userId);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("liked", liked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "获取点赞状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Integer id, @RequestBody Map<String, Object> deleteData) {
        try {
            if (!deleteData.containsKey("currentUserId") || deleteData.get("currentUserId") == null) {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "缺少用户ID");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Long currentUserId = Long.valueOf(deleteData.get("currentUserId").toString());
            forumCommentService.deleteComment(id, currentUserId);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "评论删除成功");
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(403).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
