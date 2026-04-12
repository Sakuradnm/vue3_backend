package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.OutlineWithResourcesDTO;
import com.example.vue3_backend.service.CourseOutlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseOutlineController {

    @Autowired
    private CourseOutlineService courseOutlineService;

    @GetMapping("/{courseId}/outline")
    public List<OutlineWithResourcesDTO> getCourseOutline(@PathVariable Integer courseId) {
        return courseOutlineService.getOutlineByCourseId(courseId);
    }
}
