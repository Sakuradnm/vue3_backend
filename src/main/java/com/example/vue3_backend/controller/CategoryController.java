package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.CategoryDTO;
import com.example.vue3_backend.dto.CourseTreeDTO;
import com.example.vue3_backend.service.impl.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryService;

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.findAllCategoryDTOs();
    }

    @GetMapping("/{id}")
    public CategoryDTO getCategoryById(@PathVariable Integer id) {
        List<CategoryDTO> all = categoryService.findAllCategoryDTOs();
        return all.stream()
                .filter(cat -> cat.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    /**
     * 获取完整的课程树结构（一次性返回所有分类、子分类和课程）
     */
    @GetMapping("/tree")
    public List<CourseTreeDTO> getFullCourseTree() {
        return categoryService.getFullCourseTree();
    }
}
