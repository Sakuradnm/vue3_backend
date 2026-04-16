package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.CourseDTO;
import com.example.vue3_backend.dto.CourseDetailDTO;
import com.example.vue3_backend.dto.CourseUploadDTO;
import com.example.vue3_backend.service.impl.CourseServiceImpl;
import com.example.vue3_backend.service.CourseDetailService;
import com.example.vue3_backend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private CourseDetailService courseDetailService;

    @GetMapping
    public List<CourseDTO> getAllCourses() {
        return courseService.findAllCourseDTOsBySubCategoryId(null);
    }

    @GetMapping("/sub-category/{subCategoryId}")
    public List<CourseDTO> getCoursesBySubCategoryId(@PathVariable Integer subCategoryId) {
        return courseService.findAllCourseDTOsBySubCategoryId(subCategoryId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailDTO> getCourseById(@PathVariable Integer id) {
        Optional<CourseDetailDTO> courseDetail = courseDetailService.getCourseDetailByCourseId(id);
        if (courseDetail.isPresent()) {
            return ResponseEntity.ok(courseDetail.get());
        } else {
            // 当course_details不存在时，返回一个基本的课程信息
            CourseDetailDTO defaultDetail = new CourseDetailDTO();
            defaultDetail.setCourseId(id);
            defaultDetail.setCourseName("课程" + id);
            defaultDetail.setOverview("该课程暂无详细描述");
            defaultDetail.setIntroduction("课程介绍暂未完善");
            defaultDetail.setLearningObjectives("学习目标暂未完善");
            defaultDetail.setMainContent("主要内容暂未完善");
            defaultDetail.setTargetAudience("适用人群暂未完善");
            defaultDetail.setTeachingFeatures("教学特色暂未完善");
            defaultDetail.setInstructor("待定讲师");
            return ResponseEntity.ok(defaultDetail);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Result<Integer>> uploadCourse(@RequestBody CourseUploadDTO uploadDTO) {
        try {
            Integer courseId = courseService.uploadCourse(uploadDTO);
            return ResponseEntity.ok(Result.success("课程上传成功", courseId));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "课程上传失败: " + e.getMessage()));
        }
    }
}
