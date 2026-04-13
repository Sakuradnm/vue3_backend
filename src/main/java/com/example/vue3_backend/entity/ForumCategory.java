package com.example.vue3_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_categories")
public class ForumCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "label", length = 50, nullable = false, unique = true)
    private String label;

    @Column(name = "color", length = 20, nullable = false)
    private String color;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
