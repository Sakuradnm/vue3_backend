package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.SubCategoryDTO;
import com.example.vue3_backend.service.impl.SubCategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sub-categories")
public class SubCategoryController {

    @Autowired
    private SubCategoryServiceImpl subCategoryService;

    @GetMapping
    public List<SubCategoryDTO> getAllSubCategories() {
        return null; // 如果需要所有二级分类可以实现
    }

    @GetMapping("/category/{categoryId}")
    public List<SubCategoryDTO> getSubCategoriesByCategoryId(@PathVariable Integer categoryId) {
        return subCategoryService.findAllSubCategoryDTOsByCategoryId(categoryId);
    }
}
