package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.OutlineWithResourcesDTO;
import com.example.vue3_backend.entity.ChapterResource;
import com.example.vue3_backend.entity.CourseChapter;
import com.example.vue3_backend.entity.CourseSection;
import com.example.vue3_backend.repository.ChapterResourceRepository;
import com.example.vue3_backend.repository.CourseChapterRepository;
import com.example.vue3_backend.repository.CourseSectionRepository;
import com.example.vue3_backend.service.CourseOutlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseOutlineServiceImpl implements CourseOutlineService {

    @Autowired
    private CourseChapterRepository courseChapterRepository;

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private ChapterResourceRepository chapterResourceRepository;

    @Override
    public List<OutlineWithResourcesDTO> getOutlineByCourseId(Integer courseId) {
        // 1. 获取所有主章
        List<CourseChapter> chapters = courseChapterRepository.findByCourseIdOrderBySortOrder(courseId);
        
        if (chapters.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 获取所有章节ID
        List<Integer> chapterIds = chapters.stream()
                .map(CourseChapter::getId)
                .collect(Collectors.toList());

        // 3. 获取所有章节对应的小节
        List<CourseSection> sections = courseSectionRepository.findByChapterIdsOrderBySortOrder(chapterIds);

        // 4. 获取所有小节ID
        List<Integer> sectionIds = sections.stream()
                .map(CourseSection::getId)
                .collect(Collectors.toList());

        // 5. 获取所有资源
        List<ChapterResource> resources = chapterIds.isEmpty() ? 
                Collections.emptyList() : 
                chapterResourceRepository.findBySectionIds(sectionIds);

        // 6. 构建资源Map: sectionId -> List<ResourceDTO>
        Map<Integer, List<OutlineWithResourcesDTO.ResourceDTO>> resourceMap = new HashMap<>();
        for (ChapterResource r : resources) {
            OutlineWithResourcesDTO.ResourceDTO dto = new OutlineWithResourcesDTO.ResourceDTO();
            dto.setId(r.getId());
            dto.setResourceType(r.getResourceType());
            dto.setTitle(r.getTitle());
            dto.setResourceUrl(r.getResourceUrl());
            dto.setDuration(r.getDuration());
            dto.setSortOrder(r.getSortOrder());
            
            resourceMap.computeIfAbsent(r.getSection().getId(), k -> new ArrayList<>()).add(dto);
        }

        // 7. 构建小节Map: chapterId -> List<SectionDTO>
        Map<Integer, List<OutlineWithResourcesDTO.SectionDTO>> sectionMap = new HashMap<>();
        for (CourseSection section : sections) {
            OutlineWithResourcesDTO.SectionDTO sectionDTO = new OutlineWithResourcesDTO.SectionDTO();
            sectionDTO.setId(section.getId());
            sectionDTO.setChapterId(section.getChapter().getId());
            sectionDTO.setTitle(section.getTitle());
            sectionDTO.setSortOrder(section.getSortOrder());
            sectionDTO.setResources(resourceMap.getOrDefault(section.getId(), new ArrayList<>()));
            
            sectionMap.computeIfAbsent(section.getChapter().getId(), k -> new ArrayList<>()).add(sectionDTO);
        }

        // 8. 构建最终结果
        return chapters.stream().map(chapter -> {
            OutlineWithResourcesDTO dto = new OutlineWithResourcesDTO();
            dto.setId(chapter.getId());
            dto.setCourseId(chapter.getCourse().getId());
            dto.setParentId(0); // 主章的parentId为0
            dto.setTitle(chapter.getTitle());
            dto.setSortOrder(chapter.getSortOrder());
            dto.setSections(sectionMap.getOrDefault(chapter.getId(), new ArrayList<>()));
            return dto;
        }).collect(Collectors.toList());
    }
}
