package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.CourseDTO;
import com.example.vue3_backend.dto.CourseUploadDTO;
import com.example.vue3_backend.entity.*;
import com.example.vue3_backend.repository.*;
import com.example.vue3_backend.service.CourseService;
import com.example.vue3_backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseDetailRepository courseDetailRepository;

    @Autowired
    private CourseChapterRepository courseChapterRepository;

    @Autowired
    private CourseSectionRepository courseSectionRepository;

    @Autowired
    private ChapterResourceRepository chapterResourceRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private StatisticsService statisticsService;

    @Override
    public List<Course> findCoursesBySubCategoryId(Integer subCategoryId) {
        return courseRepository.findBySubCategoryIdOrderBySortOrderAsc(subCategoryId);
    }

    @Override
    public Course findCourseById(Integer id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    @Override
    public List<Course> findAllCourses() {
        return courseRepository.findAll();
    }

    public List<CourseDTO> findAllCourseDTOsBySubCategoryId(Integer subCategoryId) {
        List<Course> courses;
        if (subCategoryId == null) {
            courses = courseRepository.findAll();
        } else {
            courses = courseRepository.findBySubCategoryIdOrderBySortOrderAsc(subCategoryId);
        }

        return courses.stream().map(course -> {
            CourseDTO dto = new CourseDTO();
            dto.setId(course.getId());
            dto.setName(course.getName());
            dto.setDescription(course.getDescription());
            dto.setSortOrder(course.getSortOrder());
            dto.setSubCategoryId(course.getSubCategory().getId());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Integer uploadCourse(CourseUploadDTO uploadDTO) {
        SubCategory subCategory = subCategoryRepository.findById(uploadDTO.getSubCategoryId())
                .orElseThrow(() -> new RuntimeException("子分类不存在"));

        Course course = new Course();
        course.setName(uploadDTO.getTitle());
        course.setDescription(uploadDTO.getDescription());
        course.setSubCategory(subCategory);
        course.setSortOrder(0);
        course = courseRepository.save(course);

        // 更新课程总数统计
        statisticsService.updateCourseCount();

        CourseDetail detail = new CourseDetail();
        detail.setCourse(course);
        detail.setCourseName(uploadDTO.getTitle());
        detail.setInstructor(uploadDTO.getInstructor() != null ? uploadDTO.getInstructor() : "匿名教师");
        detail.setIntroduction(uploadDTO.getIntroduction() != null ? uploadDTO.getIntroduction() : "");
        detail.setLearningObjectives(uploadDTO.getLearningObjectives() != null ? uploadDTO.getLearningObjectives() : "");
        detail.setMainContent(uploadDTO.getMainContent() != null ? uploadDTO.getMainContent() : "");
        detail.setTargetAudience(uploadDTO.getTargetAudience() != null ? uploadDTO.getTargetAudience() : "");
        detail.setTeachingFeatures(uploadDTO.getTeachingFeatures() != null ? uploadDTO.getTeachingFeatures() : "");
        detail.setOverview(uploadDTO.getDescription());
        courseDetailRepository.save(detail);

        if (uploadDTO.getChapters() != null) {
            int chapterSort = 0;
            for (CourseUploadDTO.ChapterDTO chapterDTO : uploadDTO.getChapters()) {
                // 1. 创建主章
                CourseChapter chapter = new CourseChapter();
                chapter.setCourse(course);
                chapter.setTitle(chapterDTO.getTitle());
                chapter.setSortOrder(++chapterSort);
                chapter = courseChapterRepository.save(chapter);

                // 2. 处理视频资源
                if (chapterDTO.getVideos() != null) {
                    int videoSort = 0;
                    for (CourseUploadDTO.VideoDTO videoDTO : chapterDTO.getVideos()) {
                        // 创建小节
                        CourseSection section = new CourseSection();
                        section.setChapter(chapter);
                        section.setCourse(course);
                        section.setTitle(videoDTO.getTitle());
                        section.setSortOrder(++videoSort);
                        section = courseSectionRepository.save(section);

                        // 创建视频资源
                        ChapterResource resource = new ChapterResource();
                        resource.setChapter(chapter);
                        resource.setSection(section);
                        resource.setCourse(course);
                        resource.setResourceType("video");
                        resource.setTitle(videoDTO.getTitle());
                        resource.setResourceUrl(videoDTO.getResourceUrl() != null ? videoDTO.getResourceUrl() : "");

                        String durationStr = videoDTO.getDuration();
                        if (durationStr != null && durationStr.contains(":")) {
                            String[] parts = durationStr.split(":");
                            int mins = Integer.parseInt(parts[0]);
                            int secs = Integer.parseInt(parts[1]);
                            resource.setDuration(mins * 60 + secs);
                        }
                        resource.setSortOrder(1);
                        chapterResourceRepository.save(resource);
                    }
                }

                // 3. 处理文件资源
                if (chapterDTO.getFiles() != null) {
                    int fileSort = 0;
                    for (CourseUploadDTO.FileDTO fileDTO : chapterDTO.getFiles()) {
                        // 创建小节
                        CourseSection section = new CourseSection();
                        section.setChapter(chapter);
                        section.setCourse(course);
                        section.setTitle(fileDTO.getTitle());
                        section.setSortOrder(1000 + (++fileSort));
                        section = courseSectionRepository.save(section);

                        // 创建文件资源
                        ChapterResource resource = new ChapterResource();
                        resource.setChapter(chapter);
                        resource.setSection(section);
                        resource.setCourse(course);
                        resource.setResourceType("pdf");
                        resource.setTitle(fileDTO.getTitle());
                        resource.setResourceUrl(fileDTO.getResourceUrl() != null ? fileDTO.getResourceUrl() : "");
                        resource.setDuration(0);
                        resource.setSortOrder(1);
                        chapterResourceRepository.save(resource);
                    }
                }
            }
        }

        return course.getId();
    }
}
