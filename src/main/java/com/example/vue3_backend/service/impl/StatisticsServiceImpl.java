package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.entity.StatisticsOverview;
import com.example.vue3_backend.repository.*;
import com.example.vue3_backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsOverviewRepository statisticsOverviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<StatisticsOverview> getStatistics() {
        return statisticsOverviewRepository.findFirstByOrderByIdAsc();
    }

    @Override
    public void updateCourseCount() {
        StatisticsOverview stats = getOrCreateStatistics();
        long count = courseRepository.count();
        stats.setTotalCourses((int) count);
        statisticsOverviewRepository.save(stats);
    }

    @Override
    public void updateUserCount() {
        StatisticsOverview stats = getOrCreateStatistics();
        long count = userRepository.count();
        stats.setTotalUsers((int) count);
        statisticsOverviewRepository.save(stats);
    }

    @Override
    public void updatePostCount() {
        StatisticsOverview stats = getOrCreateStatistics();
        long count = forumPostRepository.count();
        stats.setTotalPosts((int) count);
        statisticsOverviewRepository.save(stats);
    }

    @Override
    public void updateSubCategoryCount() {
        StatisticsOverview stats = getOrCreateStatistics();
        long count = subCategoryRepository.count();
        stats.setTotalSubCategories((int) count);
        statisticsOverviewRepository.save(stats);
    }

    @Override
    public void updateCategoryCount() {
        StatisticsOverview stats = getOrCreateStatistics();
        long count = categoryRepository.count();
        stats.setTotalCategories((int) count);
        statisticsOverviewRepository.save(stats);
    }

    @Override
    public void updateAllStatistics() {
        StatisticsOverview stats = getOrCreateStatistics();
        
        stats.setTotalCourses((int) courseRepository.count());
        stats.setTotalUsers((int) userRepository.count());
        stats.setTotalPosts((int) forumPostRepository.count());
        stats.setTotalSubCategories((int) subCategoryRepository.count());
        stats.setTotalCategories((int) categoryRepository.count());
        
        statisticsOverviewRepository.save(stats);
    }

    /**
     * 获取或创建统计数据记录
     */
    private StatisticsOverview getOrCreateStatistics() {
        Optional<StatisticsOverview> optional = statisticsOverviewRepository.findFirstByOrderByIdAsc();
        if (optional.isPresent()) {
            return optional.get();
        } else {
            StatisticsOverview newStats = new StatisticsOverview();
            newStats.setTotalCourses(0);
            newStats.setTotalUsers(0);
            newStats.setTotalPosts(0);
            newStats.setTotalSubCategories(0);
            newStats.setTotalCategories(0);
            return statisticsOverviewRepository.save(newStats);
        }
    }
}
