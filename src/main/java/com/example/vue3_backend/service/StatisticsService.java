package com.example.vue3_backend.service;

import com.example.vue3_backend.entity.StatisticsOverview;
import java.util.Optional;

public interface StatisticsService {
    
    /**
     * 获取统计数据
     */
    Optional<StatisticsOverview> getStatistics();
    
    /**
     * 更新课程总数
     */
    void updateCourseCount();
    
    /**
     * 更新用户总数
     */
    void updateUserCount();
    
    /**
     * 更新发帖总数
     */
    void updatePostCount();
    
    /**
     * 更新专题总数（二级分类总数）
     */
    void updateSubCategoryCount();
    
    /**
     * 更新一级分类总数
     */
    void updateCategoryCount();
    
    /**
     * 更新所有统计数据
     */
    void updateAllStatistics();
}
