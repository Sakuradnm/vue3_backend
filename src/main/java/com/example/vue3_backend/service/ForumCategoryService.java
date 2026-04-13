package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.ForumCategoryDTO;
import java.util.List;

public interface ForumCategoryService {
    List<ForumCategoryDTO> getAllCategories();
    ForumCategoryDTO createCategory(ForumCategoryDTO categoryDTO);
    void deleteCategory(Integer id);
}
