package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseUploadDTO {
    private String title;
    private String description;
    private Integer subCategoryId;
    private Integer categoryId;
    
    // 新增字段
    private String introduction;      // 课程简介
    private String learningObjectives; // 学习目标
    private String mainContent;        // 主要内容
    private String targetAudience;     // 适用人群
    private String teachingFeatures;   // 教学特色
    private String instructor;         // 讲师
    
    private List<ChapterDTO> chapters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterDTO {
        private String title;
        private List<VideoDTO> videos;
        private List<FileDTO> files;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoDTO {
        private String title;
        private String duration;
        private String resourceUrl;
        // fileName字段已从数据库中删除
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDTO {
        private String title;
        // fileName字段已从数据库中删除
        private String resourceUrl;
        // fileSize字段已从数据库中删除
    }
}
