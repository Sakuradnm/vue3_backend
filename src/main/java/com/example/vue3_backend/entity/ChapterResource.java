package com.example.vue3_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chapter_resources")
public class ChapterResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outline_id", nullable = false)
    private CourseOutline outline;

    @Column(name = "resource_type", length = 20, nullable = false)
    private String resourceType;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "resource_url", length = 500)
    private String resourceUrl;

    @Column(name = "duration")
    private Integer duration = 0;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
