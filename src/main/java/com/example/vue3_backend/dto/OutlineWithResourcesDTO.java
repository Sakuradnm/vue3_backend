package com.example.vue3_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutlineWithResourcesDTO {
    private Integer id;
    private Integer courseId;
    private Integer parentId;
    private String title;
    private Integer sortOrder;
    private List<SectionDTO> sections;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionDTO {
        private Integer id;
        private Integer chapterId;
        private String title;
        private Integer sortOrder;
        private List<ResourceDTO> resources;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceDTO {
        private Integer id;
        private String resourceType;
        private String title;
        // fileName字段已从数据库中删除
        private String resourceUrl;
        private Integer duration;
        // fileSize字段已从数据库中删除
        private Integer sortOrder;
    }
}
