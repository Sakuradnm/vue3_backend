package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer sortOrder;
    private Integer subCategoryId;
    private Integer status;  // 课程状态：0-上架, 1-下架
    
    // 统计字段（用于优化课程列表页性能）
    private Double ratingAvg = 0.0;  // 课程平均评分(0-5)
    private Integer ratingCount = 0;  // 评分总数
    private Integer chapterCount = 0;  // 章节数
    private Integer videoCount = 0;  // 视频节数
    private Integer totalSections = 0;  // 总课时数
    private Integer studentsCount = 0;  // 学习人数
}
