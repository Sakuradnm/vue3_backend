package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.ForumPostDTO;
import com.example.vue3_backend.dto.ForumPostDetailDTO;
import com.example.vue3_backend.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ForumPostController {

    @Autowired
    private ForumPostService forumPostService;

    @GetMapping("/posts")
    public List<ForumPostDTO> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "hot") String sortBy
    ) {
        return forumPostService.getAllPosts(category, keyword, sortBy);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ForumPostDetailDTO> getPostById(@PathVariable Integer id) {
        forumPostService.incrementViews(id);
        Optional<ForumPostDetailDTO> post = forumPostService.getPostById(id);
        return post.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/posts")
    public ResponseEntity<ForumPostDTO> createPost(@RequestBody Map<String, Object> postData) {
        try {
            ForumPostDTO createdPost = forumPostService.createPost(postData);
            return ResponseEntity.ok(createdPost);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable Integer id, @RequestBody Map<String, Object> likeData) {
        try {
            // 检查 userId 是否存在
            if (!likeData.containsKey("userId") || likeData.get("userId") == null) {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "缺少用户ID");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            Integer userId = Integer.valueOf(likeData.get("userId").toString());
            String action = likeData.containsKey("action") ? (String) likeData.get("action") : "toggle";
            boolean liked = forumPostService.toggleLikePost(id, userId, action);
            
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

    @GetMapping("/posts/{id}/like-status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable Integer id, @RequestParam Integer userId) {
        try {
            boolean liked = forumPostService.isPostLiked(id, userId);
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

    @PostMapping("/posts/{id}/comment")
    public ResponseEntity<Void> commentPost(@PathVariable Integer id) {
        forumPostService.incrementComments(id);
        return ResponseEntity.ok().build();
    }
}
