package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseReviewRepository extends JpaRepository<CourseReview, Integer> {
    
    // 查询待审核的课程
    List<CourseReview> findByStatusOrderBySubmittedAtDesc(Integer status);
    
    // 查询所有审核记录（按提交时间倒序）
    List<CourseReview> findAllByOrderBySubmittedAtDesc();
}
