package com.example.vue3_backend.service;

import com.example.vue3_backend.entity.SubCategory;
import java.util.List;

public interface SubCategoryService {
    List<SubCategory> findSubCategoriesByCategoryId(Integer categoryId);
    SubCategory findSubCategoryById(Integer id);
}
