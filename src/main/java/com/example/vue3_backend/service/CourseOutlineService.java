package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.OutlineWithResourcesDTO;
import java.util.List;

public interface CourseOutlineService {
    List<OutlineWithResourcesDTO> getOutlineByCourseId(Integer courseId);
}
