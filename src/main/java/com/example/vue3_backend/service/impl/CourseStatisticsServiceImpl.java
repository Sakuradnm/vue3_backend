package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.entity.Course;
import com.example.vue3_backend.repository.CourseRepository;
import com.example.vue3_backend.repository.UserCourseStudyRepository;
import com.example.vue3_backend.service.CourseStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;

/**
 * 课程统计服务实现类
 */
@Service
public class CourseStatisticsServiceImpl implements CourseStatisticsService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserCourseStudyRepository userCourseStudyRepository;

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public void updateCourseStatistics(Integer courseId) {
        updateCourseRatingStatistics(courseId);
        updateCourseOutlineStatistics(courseId);
        updateCourseStudentsCount(courseId);
    }

    @Override
    @Transactional
    public void updateAllCoursesStatistics() {
        List<Course> courses = courseRepository.findAll();
        for (Course course : courses) {
            updateCourseStatistics(course.getId());
        }
    }

    @Override
    @Transactional
    public void updateCourseRatingStatistics(Integer courseId) {
        // 使用原生SQL查询平均评分和评分数量
        String sql = "SELECT AVG(rating) as avg_rating, COUNT(id) as rating_count " +
                     "FROM course_ratings " +
                     "WHERE course_id = :courseId";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("courseId", courseId);
        Object[] result = (Object[]) query.getSingleResult();
        
        Double avgRating = result[0] != null ? ((Number) result[0]).doubleValue() : 0.0;
        Integer ratingCount = result[1] != null ? ((Number) result[1]).intValue() : 0;
        
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.setRatingAvg(avgRating);
            course.setRatingCount(ratingCount);
            courseRepository.save(course);
        }
    }

    @Override
    @Transactional
    public void updateCourseStudentsCount(Integer courseId) {
        Long studentsCount = userCourseStudyRepository.countByCourseId(courseId);
        
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.setStudentsCount(studentsCount.intValue());
            courseRepository.save(course);
        }
    }

    @Override
    @Transactional
    public void updateCourseOutlineStatistics(Integer courseId) {
        // 使用原生SQL查询章节数、视频数、课时数
        // 注意：实际表名是 course_chapters1(主章), course_chapters2(小节), course_chapters3(资源)
        String sql = "SELECT " +
                     "  COUNT(DISTINCT cc.id) as chapter_count, " +
                     "  COUNT(DISTINCT CASE WHEN cr.resource_type = 'video' THEN cr.id END) as video_count, " +
                     "  COUNT(DISTINCT cs.id) as total_sections " +
                     "FROM course_chapters1 cc " +
                     "LEFT JOIN course_chapters2 cs ON cs.chapter_id = cc.id " +
                     "LEFT JOIN course_chapters3 cr ON cr.section_id = cs.id " +
                     "WHERE cc.course_id = :courseId";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("courseId", courseId);
        Object[] result = (Object[]) query.getSingleResult();
        
        Integer chapterCount = result[0] != null ? ((Number) result[0]).intValue() : 0;
        Integer videoCount = result[1] != null ? ((Number) result[1]).intValue() : 0;
        Integer totalSections = result[2] != null ? ((Number) result[2]).intValue() : 0;
        
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.setChapterCount(chapterCount);
            course.setVideoCount(videoCount);
            course.setTotalSections(totalSections);
            courseRepository.save(course);
        }
    }
}
