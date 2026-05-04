package com.example.vue3_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories3")
@JsonIgnoreProperties({"subCategory"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id", nullable = false)
    private SubCategory subCategory;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // 课程状态: 0-正常（上架）, 1-下架
    @Column(name = "status")
    private Integer status = 0;

    // 统计字段（用于优化课程列表页性能）
    @Column(name = "rating_avg")
    private Double ratingAvg = 0.0;  // 课程平均评分(0-5)

    @Column(name = "rating_count")
    private Integer ratingCount = 0;  // 评分总数

    @Column(name = "chapter_count")
    private Integer chapterCount = 0;  // 章节数

    @Column(name = "video_count")
    private Integer videoCount = 0;  // 视频节数

    @Column(name = "total_sections")
    private Integer totalSections = 0;  // 总课时数

    @Column(name = "students_count")
    private Integer studentsCount = 0;  // 学习人数(从user_course_study统计)
}
