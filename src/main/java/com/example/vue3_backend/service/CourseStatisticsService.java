package com.example.vue3_backend.service;

/**
 * 课程统计服务接口
 * 用于更新和同步课程的统计数据
 */
public interface CourseStatisticsService {
    
    /**
     * 更新指定课程的统计数据
     * @param courseId 课程ID
     */
    void updateCourseStatistics(Integer courseId);
    
    /**
     * 更新所有课程的统计数据
     */
    void updateAllCoursesStatistics();
    
    /**
     * 更新课程评分统计（当有新评分时调用）
     * @param courseId 课程ID
     */
    void updateCourseRatingStatistics(Integer courseId);
    
    /**
     * 更新课程学习人数（当有新用户报名时调用）
     * @param courseId 课程ID
     */
    void updateCourseStudentsCount(Integer courseId);
    
    /**
     * 更新课程大纲统计（当课程大纲变更时调用）
     * @param courseId 课程ID
     */
    void updateCourseOutlineStatistics(Integer courseId);
}
