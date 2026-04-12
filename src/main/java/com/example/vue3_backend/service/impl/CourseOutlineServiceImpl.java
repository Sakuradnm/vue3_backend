package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.OutlineWithResourcesDTO;
import com.example.vue3_backend.entity.ChapterResource;
import com.example.vue3_backend.entity.CourseOutline;
import com.example.vue3_backend.repository.ChapterResourceRepository;
import com.example.vue3_backend.repository.CourseOutlineRepository;
import com.example.vue3_backend.service.CourseOutlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseOutlineServiceImpl implements CourseOutlineService {

    @Autowired
    private CourseOutlineRepository courseOutlineRepository;

    @Autowired
    private ChapterResourceRepository chapterResourceRepository;

    @Override
    public List<OutlineWithResourcesDTO> getOutlineByCourseId(Integer courseId) {
        List<CourseOutline> outlines = courseOutlineRepository.findByCourseIdOrderBySortOrder(courseId);
        
        if (outlines.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> outlineIds = outlines.stream()
                .map(CourseOutline::getId)
                .collect(Collectors.toList());

        List<ChapterResource> resources = chapterResourceRepository.findByOutlineIds(outlineIds);

        Map<Integer, List<OutlineWithResourcesDTO.ResourceDTO>> resourceMap = resources.stream()
                .map(r -> {
                    OutlineWithResourcesDTO.ResourceDTO dto = new OutlineWithResourcesDTO.ResourceDTO();
                    dto.setId(r.getId());
                    dto.setResourceType(r.getResourceType());
                    dto.setTitle(r.getTitle());
                    dto.setResourceUrl(r.getResourceUrl());
                    dto.setDuration(r.getDuration());
                    dto.setSortOrder(r.getSortOrder());
                    return dto;
                })
                .collect(Collectors.groupingBy(r -> {
                    ChapterResource res = resources.stream()
                            .filter(resource -> resource.getId().equals(
                                resources.indexOf(r) >= 0 ? r.getId() : null))
                            .findFirst().orElse(null);
                    return res != null ? res.getOutline().getId() : 0;
                }));

        Map<Integer, List<OutlineWithResourcesDTO.ResourceDTO>> finalResourceMap = new HashMap<>();
        for (ChapterResource r : resources) {
            OutlineWithResourcesDTO.ResourceDTO dto = new OutlineWithResourcesDTO.ResourceDTO();
            dto.setId(r.getId());
            dto.setResourceType(r.getResourceType());
            dto.setTitle(r.getTitle());
            dto.setResourceUrl(r.getResourceUrl());
            dto.setDuration(r.getDuration());
            dto.setSortOrder(r.getSortOrder());
            
            finalResourceMap.computeIfAbsent(r.getOutline().getId(), k -> new ArrayList<>()).add(dto);
        }

        return outlines.stream().map(outline -> {
            OutlineWithResourcesDTO dto = new OutlineWithResourcesDTO();
            dto.setId(outline.getId());
            dto.setCourseId(outline.getCourse().getId());
            dto.setParentId(outline.getParentId());
            dto.setTitle(outline.getTitle());
            dto.setSortOrder(outline.getSortOrder());
            dto.setResources(finalResourceMap.getOrDefault(outline.getId(), new ArrayList<>()));
            return dto;
        }).collect(Collectors.toList());
    }
}
