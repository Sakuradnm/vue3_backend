package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.CourseDetailDTO;
import com.example.vue3_backend.entity.CourseDetail;
import com.example.vue3_backend.repository.CourseDetailRepository;
import com.example.vue3_backend.service.CourseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CourseDetailServiceImpl implements CourseDetailService {

    @Autowired
    private CourseDetailRepository courseDetailRepository;

    @Override
    public Optional<CourseDetailDTO> getCourseDetailByCourseId(Integer courseId) {
        return courseDetailRepository.findByCourseIdWithCourse(courseId)
                .map(this::convertToDTO);
    }

    private CourseDetailDTO convertToDTO(CourseDetail courseDetail) {
        CourseDetailDTO dto = new CourseDetailDTO();
        dto.setId(courseDetail.getId());
        dto.setCourseId(courseDetail.getCourse().getId());
        dto.setCourseName(courseDetail.getCourseName());
        dto.setOverview(courseDetail.getOverview());
        
        // 设置新字段
        dto.setIntroduction(courseDetail.getIntroduction());
        dto.setLearningObjectives(courseDetail.getLearningObjectives());
        dto.setMainContent(courseDetail.getMainContent());
        dto.setTargetAudience(courseDetail.getTargetAudience());
        dto.setTeachingFeatures(courseDetail.getTeachingFeatures());
        dto.setInstructor(courseDetail.getInstructor());

        if (courseDetail.getCourse() != null) {
            dto.setCourseDescription(courseDetail.getCourse().getDescription());
            if (courseDetail.getCourse().getSubCategory() != null) {
                dto.setSubCategoryId(courseDetail.getCourse().getSubCategory().getId());
                if (courseDetail.getCourse().getSubCategory().getCategory() != null) {
                    dto.setCategoryId(courseDetail.getCourse().getSubCategory().getCategory().getId());
                }
            }
        }

        return dto;
    }
}
