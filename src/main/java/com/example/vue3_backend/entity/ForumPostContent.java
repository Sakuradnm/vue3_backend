package com.example.vue3_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forum_post_contents")
public class ForumPostContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "post_id", nullable = false, unique = true)
    private Integer postId;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;
}
