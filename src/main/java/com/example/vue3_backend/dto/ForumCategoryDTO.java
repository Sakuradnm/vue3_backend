package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForumCategoryDTO {
    private Integer id;
    private String categoryId;
    private String label;
    private String color;
    private Integer sortOrder;
}
