package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.CourseDTO;
import com.example.vue3_backend.dto.CourseDetailDTO;
import com.example.vue3_backend.dto.CourseUploadDTO;
import com.example.vue3_backend.service.impl.CourseServiceImpl;
import com.example.vue3_backend.service.CourseDetailService;
import com.example.vue3_backend.service.CourseReviewService;
import com.example.vue3_backend.common.Result;
import com.example.vue3_backend.entity.User;
import com.example.vue3_backend.entity.Course;
import com.example.vue3_backend.entity.UserCourseStudy;
import com.example.vue3_backend.repository.UserRepository;
import com.example.vue3_backend.repository.CourseRepository;
import com.example.vue3_backend.repository.UserCourseStudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseServiceImpl courseService;

    @Autowired
    private CourseDetailService courseDetailService;

    @Autowired
    private CourseReviewService courseReviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserCourseStudyRepository userCourseStudyRepository;

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
            // 修改为提交审核，而不是直接上传
            Integer reviewId = courseReviewService.submitCourseReview(uploadDTO);
            return ResponseEntity.ok(Result.success("课程已提交审核，等待管理员审批", reviewId));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "课程提交失败: " + e.getMessage()));
        }
    }

    /**
     * 用户报名学习课程（立即学习）
     */
    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<Result<Map<String, Object>>> enrollCourse(
            @PathVariable Integer courseId,
            @RequestBody Map<String, Object> request) {
        try {
            // 获取用户ID
            Object userIdObj = request.get("userId");
            Long userId = userIdObj instanceof Long ? (Long) userIdObj : Long.valueOf(userIdObj.toString());

            if (userId == null || userId <= 0) {
                return ResponseEntity.ok(Result.error("用户ID无效"));
            }

            // 检查用户是否存在
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(Result.error("用户不存在"));
            }

            // 检查课程是否存在
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (!courseOpt.isPresent()) {
                return ResponseEntity.ok(Result.error("课程不存在"));
            }

            // 检查是否已经学习过该课程
            Optional<UserCourseStudy> existingStudy = userCourseStudyRepository.findByUserIdAndCourseId(userId, courseId);
            if (existingStudy.isPresent()) {
                Map<String, Object> result = new HashMap<>();
                result.put("alreadyEnrolled", true);
                result.put("message", "您已经在 learning 这门课程");
                return ResponseEntity.ok(Result.success(result));
            }

            // 创建学习记录
            UserCourseStudy study = new UserCourseStudy();
            study.setUser(userOpt.get());
            study.setCourse(courseOpt.get());
            study.setProgressPercent(0.0);
            userCourseStudyRepository.save(study);

            Map<String, Object> result = new HashMap<>();
            result.put("alreadyEnrolled", false);
            result.put("message", "报名成功，开始学习吧！");
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "报名失败: " + e.getMessage()));
        }
    }

    /**
     * 检查用户是否已学习某门课程
     */
    @GetMapping("/{courseId}/check-enrollment")
    public ResponseEntity<Result<Map<String, Object>>> checkEnrollment(
            @PathVariable Integer courseId,
            @RequestParam Long userId) {
        try {
            Optional<UserCourseStudy> study = userCourseStudyRepository.findByUserIdAndCourseId(userId, courseId);
            Map<String, Object> result = new HashMap<>();
            result.put("isEnrolled", study.isPresent());
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取课程的学习人数
     */
    @GetMapping("/{courseId}/students-count")
    public ResponseEntity<Result<Map<String, Object>>> getStudentsCount(@PathVariable Integer courseId) {
        try {
            Long count = userCourseStudyRepository.countByCourseId(courseId);
            Map<String, Object> result = new HashMap<>();
            result.put("studentsCount", count);
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 批量获取多个课程的统计数据（学习人数）
     */
    @PostMapping("/batch-students-count")
    public ResponseEntity<Result<Map<Integer, Long>>> getBatchStudentsCount(@RequestBody List<Integer> courseIds) {
        try {
            Map<Integer, Long> result = new HashMap<>();
            for (Integer courseId : courseIds) {
                Long count = userCourseStudyRepository.countByCourseId(courseId);
                result.put(courseId, count);
            }
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户的学习记录列表
     */
    @GetMapping("/user-study-records")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<Result<List<Map<String, Object>>>> getUserStudyRecords(@RequestParam Long userId) {
        try {
            List<UserCourseStudy> studyRecords = userCourseStudyRepository.findByUserIdOrderByLastLearnedAtDesc(userId);
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            
            for (UserCourseStudy record : studyRecords) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", record.getCourse().getId());
                item.put("courseId", record.getCourse().getId());
                item.put("courseName", record.getCourse().getName());
                item.put("enrolledAt", record.getEnrolledAt());
                item.put("lastLearnedAt", record.getLastLearnedAt());
                item.put("progressPercent", record.getProgressPercent());
                result.add(item);
            }
            
            return ResponseEntity.ok(Result.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }
}
