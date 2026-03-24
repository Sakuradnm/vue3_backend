package com.example.vue3_backend.service;

import com.example.vue3_backend.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAllCategories();
    Category findCategoryById(Integer id);
}
