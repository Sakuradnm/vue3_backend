package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailDTO {
    private Integer id;
    private Integer courseId;
    private String courseName;
    private String overview;
    
    // 新增字段
    private String introduction;
    private String learningObjectives;
    private String mainContent;
    private String targetAudience;
    private String teachingFeatures;
    private String instructor;

    // 关联的课程信息
    private String courseDescription;
    private Integer subCategoryId;
    private Integer categoryId;
}
