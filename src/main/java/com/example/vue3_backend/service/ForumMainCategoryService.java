package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.ForumMainCategoryDTO;
import java.util.List;

public interface ForumMainCategoryService {
    List<ForumMainCategoryDTO> getAllMainCategories();
}
