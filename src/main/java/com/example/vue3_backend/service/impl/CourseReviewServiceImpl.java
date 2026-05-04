package com.example.vue3_backend.service.impl;

import com.example.vue3_backend.dto.CourseUploadDTO;
import com.example.vue3_backend.entity.*;
import com.example.vue3_backend.repository.*;
import com.example.vue3_backend.service.CourseReviewService;
import com.example.vue3_backend.service.AdminNoticeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseReviewServiceImpl implements CourseReviewService {

    @Autowired
    private CourseReviewRepository courseReviewRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

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
    private ObjectMapper objectMapper;

    @Autowired
    private AdminNoticeService adminNoticeService;

    @Override
    public Integer submitCourseReview(CourseUploadDTO uploadDTO) {
        try {
            // 验证子分类是否存在
            SubCategory subCategory = subCategoryRepository.findById(uploadDTO.getSubCategoryId())
                    .orElseThrow(() -> new RuntimeException("子分类不存在"));

            // 检查课程总表中是否存在同名课程
            String courseCheckResult = checkCourseExistsInCourses(uploadDTO.getTitle(), uploadDTO.getSubCategoryId(), uploadDTO.getInstructor());
            if (!courseCheckResult.equals("PASS")) {
                throw new RuntimeException(courseCheckResult);
            }
            
            // 检查审核表中是否存在待审核记录
            String reviewCheckResult = checkCourseExistsInReviews(uploadDTO.getTitle(), uploadDTO.getSubCategoryId(), uploadDTO.getInstructor());
            if (!reviewCheckResult.equals("PASS")) {
                throw new RuntimeException(reviewCheckResult);
            }

            // 将章节数据转换为JSON字符串
            String chaptersJson = objectMapper.writeValueAsString(uploadDTO.getChapters());

            // 创建审核记录
            CourseReview review = new CourseReview();
            review.setTitle(uploadDTO.getTitle());
            review.setDescription(uploadDTO.getDescription());
            review.setInstructor(uploadDTO.getInstructor());
            review.setSubCategoryId(uploadDTO.getSubCategoryId());
            review.setCategoryId(uploadDTO.getCategoryId());
            
            // 详细信息
            review.setIntroduction(uploadDTO.getIntroduction());
            review.setLearningObjectives(uploadDTO.getLearningObjectives());
            review.setMainContent(uploadDTO.getMainContent());
            review.setTargetAudience(uploadDTO.getTargetAudience());
            review.setTeachingFeatures(uploadDTO.getTeachingFeatures());
            
            // 章节数据
            review.setChaptersData(chaptersJson);
            
            // 审核状态：0-待审核
            review.setStatus(0);

            review = courseReviewRepository.save(review);
            
            // 创建管理员通知
            try {
                adminNoticeService.createCourseReviewNotice(
                    uploadDTO.getInstructor(),
                    uploadDTO.getTitle(),
                    review.getId()
                );
            } catch (Exception e) {
                // 通知创建失败不影响主流程
                System.err.println("创建通知失败: " + e.getMessage());
            }
            
            return review.getId();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("章节数据序列化失败: " + e.getMessage());
        }
    }

    @Override
    public List<CourseReview> getPendingReviews() {
        return courseReviewRepository.findByStatusOrderBySubmittedAtDesc(0);
    }

    @Override
    public List<CourseReview> getAllReviews() {
        return courseReviewRepository.findAllByOrderBySubmittedAtDesc();
    }

    @Override
    @Transactional
    public void approveReview(Integer reviewId, Integer reviewerId) {
        // 获取审核记录
        CourseReview review = courseReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("审核记录不存在"));

        if (review.getStatus() != 0) {
            throw new RuntimeException("该课程已经审核过了");
        }

        try {
            // 解析章节数据
            List<CourseUploadDTO.ChapterDTO> chapters = objectMapper.readValue(
                    review.getChaptersData(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, CourseUploadDTO.ChapterDTO.class)
            );

            // 1. 创建课程（categories3表）
            Course course = new Course();
            course.setName(review.getTitle());
            course.setDescription(review.getDescription());
            
            // 设置子分类关联
            SubCategory subCategory = subCategoryRepository.findById(review.getSubCategoryId())
                    .orElseThrow(() -> new RuntimeException("子分类不存在"));
            course.setSubCategory(subCategory);
            course.setSortOrder(0);
            
            course = courseRepository.save(course);

            // 2. 创建课程详情（course_details表）
            CourseDetail detail = new CourseDetail();
            detail.setCourse(course);
            detail.setCourseName(review.getTitle());
            detail.setOverview(review.getDescription());
            detail.setIntroduction(review.getIntroduction());
            detail.setLearningObjectives(review.getLearningObjectives());
            detail.setMainContent(review.getMainContent());
            detail.setTargetAudience(review.getTargetAudience());
            detail.setTeachingFeatures(review.getTeachingFeatures());
            detail.setInstructor(review.getInstructor());
            
            courseDetailRepository.save(detail);

            // 3. 创建章节、小节和资源
            int chapterSortOrder = 1;
            for (CourseUploadDTO.ChapterDTO chapterDTO : chapters) {
                // 创建章节
                CourseChapter chapter = new CourseChapter();
                chapter.setCourse(course);
                chapter.setTitle(chapterDTO.getTitle());
                chapter.setSortOrder(chapterSortOrder++);
                
                chapter = courseChapterRepository.save(chapter);

                int sectionSortOrder = 1;

                // 处理视频资源
                if (chapterDTO.getVideos() != null) {
                    for (CourseUploadDTO.VideoDTO videoDTO : chapterDTO.getVideos()) {
                        // 创建小节
                        CourseSection section = new CourseSection();
                        section.setChapter(chapter);
                        section.setTitle(videoDTO.getTitle());
                        section.setSectionType("video");
                        section.setDuration(videoDTO.getDuration());
                        section.setSortOrder(sectionSortOrder++);
                        
                        section = courseSectionRepository.save(section);

                        // 创建资源记录
                        ChapterResource resource = new ChapterResource();
                        resource.setChapter(chapter);
                        resource.setSection(section);
                        resource.setResourceType("video");
                        resource.setTitle(videoDTO.getTitle());
                        // fileName字段已从数据库中删除，不再设置
                        resource.setResourceUrl(videoDTO.getResourceUrl());
                        // fileSize字段已从数据库中删除，不再设置
                        // duration是String类型（如"15:30"），需要转换为Integer（秒）
                        try {
                            if (videoDTO.getDuration() != null && !videoDTO.getDuration().isEmpty()) {
                                String[] parts = videoDTO.getDuration().split(":");
                                int minutes = Integer.parseInt(parts[0]);
                                int seconds = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                                resource.setDuration(minutes * 60 + seconds);
                            } else {
                                resource.setDuration(0);
                            }
                        } catch (Exception e) {
                            resource.setDuration(0);
                        }
                        resource.setSortOrder(sectionSortOrder - 1);
                        
                        chapterResourceRepository.save(resource);
                    }
                }

                // 处理文件资源
                if (chapterDTO.getFiles() != null) {
                    for (CourseUploadDTO.FileDTO fileDTO : chapterDTO.getFiles()) {
                        // 创建小节
                        CourseSection section = new CourseSection();
                        section.setChapter(chapter);
                        section.setTitle(fileDTO.getTitle());
                        section.setSectionType("document");
                        section.setDuration(null);
                        section.setSortOrder(sectionSortOrder++);
                        
                        section = courseSectionRepository.save(section);

                        // 创建资源记录
                        ChapterResource resource = new ChapterResource();
                        resource.setChapter(chapter);
                        resource.setSection(section);
                        resource.setResourceType("document");
                        resource.setTitle(fileDTO.getTitle());
                        // fileName字段已从数据库中删除，不再设置
                        resource.setResourceUrl(fileDTO.getResourceUrl());
                        // fileSize字段已从数据库中删除，不再设置
                        resource.setDuration(null);
                        resource.setSortOrder(sectionSortOrder - 1);
                        
                        chapterResourceRepository.save(resource);
                    }
                }
            }

            // 4. 更新审核状态为已通过
            review.setStatus(1);
            review.setReviewerId(reviewerId);
            review.setReviewedAt(LocalDateTime.now());
            courseReviewRepository.save(review);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("章节数据反序列化失败: " + e.getMessage());
        }
    }

    @Override
    public void rejectReview(Integer reviewId, Integer reviewerId, String comment) {
        CourseReview review = courseReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("审核记录不存在"));

        if (review.getStatus() != 0) {
            throw new RuntimeException("该课程已经审核过了");
        }

        // 更新审核状态为已拒绝
        review.setStatus(2);
        review.setReviewerId(reviewerId);
        review.setReviewComment(comment);
        review.setReviewedAt(LocalDateTime.now());
        
        courseReviewRepository.save(review);
    }

    @Override
    public String checkCourseExistsInCourses(String title, Integer subCategoryId, String instructor) {
        // 查询course_details表，通过course关联sub_category
        List<CourseDetail> details = courseDetailRepository.findAll();
        for (CourseDetail detail : details) {
            if (detail.getCourseName().equals(title) && 
                detail.getCourse().getSubCategory().getId().equals(subCategoryId)) {
                // 找到同名同分类的课程
                if (detail.getInstructor() != null && detail.getInstructor().equals(instructor)) {
                    return "课程里已经有您提交过该专业课程，请勿重复提交";
                } else {
                    // 不同讲师，允许提交
                    return "PASS";
                }
            }
        }
        return "PASS";
    }

    @Override
    public String checkCourseExistsInReviews(String title, Integer subCategoryId, String instructor) {
        // 查询待审核的记录
        List<CourseReview> reviews = courseReviewRepository.findByStatusOrderBySubmittedAtDesc(0);
        for (CourseReview review : reviews) {
            if (review.getTitle().equals(title) && 
                review.getSubCategoryId().equals(subCategoryId)) {
                // 找到同名同分类的待审核记录
                if (review.getInstructor() != null && review.getInstructor().equals(instructor)) {
                    return "您已提交过该专业课程，请等待管理员审核，切勿重复提交";
                } else {
                    // 不同讲师，允许提交
                    return "PASS";
                }
            }
        }
        return "PASS";
    }
}
