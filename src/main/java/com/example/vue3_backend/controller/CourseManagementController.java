package com.example.vue3_backend.controller;

import com.example.vue3_backend.common.Result;
import com.example.vue3_backend.dto.CourseManageDTO;
import com.example.vue3_backend.entity.Course;
import com.example.vue3_backend.entity.CourseDetail;
import com.example.vue3_backend.entity.CourseChapter;
import com.example.vue3_backend.entity.CourseSection;
import com.example.vue3_backend.entity.ChapterResource;
import com.example.vue3_backend.repository.CourseRepository;
import com.example.vue3_backend.repository.CourseDetailRepository;
import com.example.vue3_backend.repository.CourseChapterRepository;
import com.example.vue3_backend.repository.CourseSectionRepository;
import com.example.vue3_backend.repository.ChapterResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/courses")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CourseManagementController {

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

    // 获取所有课程列表（包含状态）
    @GetMapping
    public ResponseEntity<Result<List<CourseManageDTO>>> getAllCourses() {
        try {
            List<Course> courses = courseRepository.findAll();
            
            // 转换为 DTO
            List<CourseManageDTO> dtoList = courses.stream().map(course -> {
                CourseManageDTO dto = new CourseManageDTO();
                dto.setId(course.getId());
                dto.setName(course.getName());
                dto.setDescription(course.getDescription());
                dto.setSubCategoryId(course.getSubCategory().getId());
                dto.setSortOrder(course.getSortOrder());
                dto.setStatus(course.getStatus() != null ? course.getStatus() : 0);
                
                // 统计子章节数量
                Long chapterCount = courseChapterRepository.countByCourseId(course.getId());
                dto.setCourseCount(chapterCount.intValue());
                
                return dto;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(Result.success(dtoList));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Result.error(500, "获取课程列表失败：" + e.getMessage()));
        }
    }

    // 更新课程状态（上架/下架）
    @PutMapping("/{id}/status")
    public ResponseEntity<Result<CourseManageDTO>> updateCourseStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> data) {
        try {
            Course course = courseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));

            Integer status = data.get("status");
            if (status == null || (status != 0 && status != 1)) {
                return ResponseEntity.ok(Result.error(400, "无效的状态值，应为 0（上架）或 1（下架）"));
            }

            course.setStatus(status);
            Course updatedCourse = courseRepository.save(course);

            CourseManageDTO dto = new CourseManageDTO();
            dto.setId(updatedCourse.getId());
            dto.setName(updatedCourse.getName());
            dto.setDescription(updatedCourse.getDescription());
            dto.setSubCategoryId(updatedCourse.getSubCategory().getId());
            dto.setSortOrder(updatedCourse.getSortOrder());
            dto.setStatus(updatedCourse.getStatus());
            
            Long chapterCount = courseChapterRepository.countByCourseId(updatedCourse.getId());
            dto.setCourseCount(chapterCount.intValue());

            String message = status == 0 ? "课程已上架" : "课程已下架";
            return ResponseEntity.ok(Result.success(message, dto));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(400, e.getMessage()));
        }
    }

    // 删除课程（级联删除所有关联数据）
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Result<Void>> deleteCourse(@PathVariable Integer id) {
        try {
            Course course = courseRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("课程不存在"));

            // 1. 删除关联的视频/文件资源
            List<CourseChapter> chapters = courseChapterRepository.findByCourseIdOrderBySortOrder(id);
            for (CourseChapter chapter : chapters) {
                // 删除章节下的所有资源
                chapterResourceRepository.deleteByChapterId(chapter.getId());
                // 删除章节下的所有小节
                courseSectionRepository.deleteByChapterId(chapter.getId());
            }
            
            // 2. 删除所有章节
            courseChapterRepository.deleteByCourseId(id);
            
            // 3. 删除课程详情
            courseDetailRepository.deleteByCourseId(id);
            
            // 4. 删除课程
            courseRepository.deleteById(id);
            
            // 5. 重置AUTO_INCREMENT，避免 ID 跳号
            courseRepository.resetAutoIncrement();
            courseDetailRepository.resetAutoIncrement();
            courseChapterRepository.resetAutoIncrement();
            courseSectionRepository.resetAutoIncrement();
            chapterResourceRepository.resetAutoIncrement();
            
            return ResponseEntity.ok(Result.success("课程及其关联数据已彻底删除", null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Result.error(400, "删除失败：" + e.getMessage()));
        }
    }
}
