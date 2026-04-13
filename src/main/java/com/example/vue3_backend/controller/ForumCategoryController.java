package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.ForumCategoryDTO;
import com.example.vue3_backend.service.ForumCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum-categories")
public class ForumCategoryController {

    @Autowired
    private ForumCategoryService forumCategoryService;

    @GetMapping
    public ResponseEntity<List<ForumCategoryDTO>> getCategories() {
        List<ForumCategoryDTO> categories = forumCategoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<ForumCategoryDTO> createCategory(@RequestBody ForumCategoryDTO categoryDTO) {
        ForumCategoryDTO created = forumCategoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        forumCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
