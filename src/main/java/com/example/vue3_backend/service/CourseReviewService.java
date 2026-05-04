package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.CourseUploadDTO;
import com.example.vue3_backend.entity.CourseReview;
import java.util.List;

public interface CourseReviewService {
    
    // 提交课程审核
    Integer submitCourseReview(CourseUploadDTO uploadDTO);
    
    // 获取待审核列表
    List<CourseReview> getPendingReviews();
    
    // 获取所有审核记录
    List<CourseReview> getAllReviews();
    
    // 审核通过
    void approveReview(Integer reviewId, Integer reviewerId);
    
    // 审核拒绝
    void rejectReview(Integer reviewId, Integer reviewerId, String comment);

    // 检查课程总表中是否存在相同名称的课程（同一讲师）
    String checkCourseExistsInCourses(String title, Integer subCategoryId, String instructor);

    // 检查审核表中是否存在相同名称的待审核课程（同一讲师）
    String checkCourseExistsInReviews(String title, Integer subCategoryId, String instructor);
}
