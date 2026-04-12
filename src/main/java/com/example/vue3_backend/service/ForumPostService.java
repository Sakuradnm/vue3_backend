package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.ForumPostDTO;
import com.example.vue3_backend.dto.ForumPostDetailDTO;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface ForumPostService {

    List<ForumPostDTO> getAllPosts(String category, String keyword, String sortBy);

    Optional<ForumPostDetailDTO> getPostById(Integer id);

    void incrementViews(Integer id);

    void incrementLikes(Integer id);

    void incrementComments(Integer id);

    ForumPostDTO createPost(Map<String, Object> postData);
}
