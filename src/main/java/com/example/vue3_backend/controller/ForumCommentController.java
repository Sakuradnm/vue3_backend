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
            // 检查 userId 是否存在
            if (!likeData.containsKey("userId") || likeData.get("userId") == null) {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "缺少用户ID");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 处理 userId 类型转换（可能是 Integer 或 Long）
            Object userIdObj = likeData.get("userId");
            Integer userId;
            if (userIdObj instanceof Integer) {
                userId = (Integer) userIdObj;
            } else if (userIdObj instanceof Long) {
                userId = ((Long) userIdObj).intValue();
            } else {
                userId = Integer.valueOf(userIdObj.toString());
            }
            
            boolean liked = forumCommentService.toggleLikeComment(id, userId, 
                likeData.containsKey("action") ? (String) likeData.get("action") : "toggle");
            
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
    public ResponseEntity<Map<String, Object>> getCommentLikeStatus(@PathVariable Integer id, @RequestParam Integer userId) {
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
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> deleteData) {
        try {
            // 检查必需参数
            if (!deleteData.containsKey("currentUserId") || !deleteData.containsKey("postOwnerId")) {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "缺少必要参数");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Integer currentUserId = Integer.valueOf(deleteData.get("currentUserId").toString());
            Integer postOwnerId = Integer.valueOf(deleteData.get("postOwnerId").toString());
            
            boolean deleted = forumCommentService.deleteComment(id, currentUserId, postOwnerId);
            
            if (deleted) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "无权限删除或评论不存在");
                return ResponseEntity.status(403).body(errorResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
