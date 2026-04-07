package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.ForumPostDTO;
import com.example.vue3_backend.dto.ForumPostDetailDTO;
import com.example.vue3_backend.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/forum")
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

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable Integer id) {
        forumPostService.incrementLikes(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{id}/comment")
    public ResponseEntity<Void> commentPost(@PathVariable Integer id) {
        forumPostService.incrementComments(id);
        return ResponseEntity.ok().build();
    }
}
