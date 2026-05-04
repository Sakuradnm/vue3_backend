package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseManageDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer subCategoryId;
    private Integer sortOrder;
    private Integer status;  // 0-上架, 1-下架
    private Integer courseCount;  // 子章节数量（用于显示）
}
