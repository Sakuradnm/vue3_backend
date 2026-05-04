package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.ForumMainCategoryDTO;
import com.example.vue3_backend.service.ForumMainCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum-main-categories")
public class ForumMainCategoryController {

    @Autowired
    private ForumMainCategoryService forumMainCategoryService;

    @GetMapping
    public ResponseEntity<List<ForumMainCategoryDTO>> getMainCategories() {
        List<ForumMainCategoryDTO> categories = forumMainCategoryService.getAllMainCategories();
        return ResponseEntity.ok(categories);
    }
}
