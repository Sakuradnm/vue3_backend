package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.CourseDetailDTO;
import java.util.Optional;

public interface CourseDetailService {
    Optional<CourseDetailDTO> getCourseDetailByCourseId(Integer courseId);
}
