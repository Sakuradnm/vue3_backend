package com.example.vue3_backend.controller;

import com.example.vue3_backend.dto.CourseUploadDTO;
import com.example.vue3_backend.entity.CourseReview;
import com.example.vue3_backend.service.CourseReviewService;
import com.example.vue3_backend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/course-review")
@CrossOrigin
public class CourseReviewController {

    @Autowired
    private CourseReviewService courseReviewService;

    // 提交课程审核
    @PostMapping("/submit")
    public ResponseEntity<Result<Integer>> submitReview(@RequestBody CourseUploadDTO uploadDTO) {
        try {
            Integer reviewId = courseReviewService.submitCourseReview(uploadDTO);
            return ResponseEntity.ok(Result.success("课程已提交审核", reviewId));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "提交失败: " + e.getMessage()));
        }
    }

    // 获取待审核列表
    @GetMapping("/pending")
    public ResponseEntity<Result<List<CourseReview>>> getPendingReviews() {
        try {
            List<CourseReview> reviews = courseReviewService.getPendingReviews();
            return ResponseEntity.ok(Result.success(reviews));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    // 获取所有审核记录
    @GetMapping("/all")
    public ResponseEntity<Result<List<CourseReview>>> getAllReviews() {
        try {
            List<CourseReview> reviews = courseReviewService.getAllReviews();
            return ResponseEntity.ok(Result.success(reviews));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }

    // 审核通过
    @PostMapping("/{id}/approve")
    public ResponseEntity<Result<Void>> approveReview(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        try {
            Integer reviewerId = (Integer) params.get("reviewerId");
            if (reviewerId == null) {
                reviewerId = 1; // 默认管理员ID
            }
            courseReviewService.approveReview(id, reviewerId);
            return ResponseEntity.ok(Result.success("审核通过", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "审核失败: " + e.getMessage()));
        }
    }

    // 审核拒绝
    @PostMapping("/{id}/reject")
    public ResponseEntity<Result<Void>> rejectReview(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        try {
            Integer reviewerId = (Integer) params.get("reviewerId");
            String comment = (String) params.get("comment");
            
            if (reviewerId == null) {
                reviewerId = 1; // 默认管理员ID
            }
            
            courseReviewService.rejectReview(id, reviewerId, comment);
            return ResponseEntity.ok(Result.success("已拒绝", null));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "操作失败: " + e.getMessage()));
        }
    }

    // 检查课程名称是否重复
    @PostMapping("/check-duplicate")
    public ResponseEntity<Result<String>> checkDuplicate(@RequestBody Map<String, Object> params) {
        try {
            String title = (String) params.get("title");
            Integer subCategoryId = (Integer) params.get("subCategoryId");
            String instructor = (String) params.get("instructor");
            
            if (title == null || subCategoryId == null || instructor == null) {
                return ResponseEntity.ok(Result.error(400, "参数不完整"));
            }
            
            // 检查课程总表中是否存在
            String courseCheckResult = courseReviewService.checkCourseExistsInCourses(title, subCategoryId, instructor);
            if (!courseCheckResult.equals("PASS")) {
                return ResponseEntity.ok(Result.error(500, courseCheckResult));
            }
            
            // 检查审核表中是否存在待审核记录
            String reviewCheckResult = courseReviewService.checkCourseExistsInReviews(title, subCategoryId, instructor);
            if (!reviewCheckResult.equals("PASS")) {
                return ResponseEntity.ok(Result.error(500, reviewCheckResult));
            }
            
            return ResponseEntity.ok(Result.success("PASS"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(500, "查询失败: " + e.getMessage()));
        }
    }
}
