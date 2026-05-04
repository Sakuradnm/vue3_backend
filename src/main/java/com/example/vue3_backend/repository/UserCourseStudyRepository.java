package com.example.vue3_backend.repository;

import com.example.vue3_backend.entity.UserCourseStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseStudyRepository extends JpaRepository<UserCourseStudy, Long> {
    
    // 查询用户是否已经学习了某门课程
    Optional<UserCourseStudy> findByUserIdAndCourseId(Long userId, Integer courseId);
    
    // 查询某门课程的所有学习记录
    List<UserCourseStudy> findByCourseId(Integer courseId);
    
    // 统计某门课程的学习人数
    @Query("SELECT COUNT(ucs) FROM UserCourseStudy ucs WHERE ucs.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Integer courseId);
    
    // 查询某个用户的所有学习记录
    List<UserCourseStudy> findByUserId(Long userId);
    
    // 查询某个用户的所有学习记录，按最后学习时间降序排序（使用JOIN FETCH避免懒加载问题）
    @Query("SELECT ucs FROM UserCourseStudy ucs JOIN FETCH ucs.course WHERE ucs.user.id = :userId ORDER BY ucs.lastLearnedAt DESC")
    List<UserCourseStudy> findByUserIdOrderByLastLearnedAtDesc(@Param("userId") Long userId);
}
