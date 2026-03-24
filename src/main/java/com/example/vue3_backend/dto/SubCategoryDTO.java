package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer categoryId;
}
