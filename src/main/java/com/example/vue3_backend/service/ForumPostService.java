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

    /**
     * 点赞或取消点赞帖子（根据用户ID）
     * @param action "like"=点赞, "unlike"=取消点赞, "toggle"=切换
     * @return true表示当前已点赞，false表示未点赞
     */
    boolean toggleLikePost(Integer postId, Integer userId, String action);

    /**
     * 检查用户是否已点赞某帖子
     * @return true表示已点赞，false表示未点赞
     */
    boolean isPostLiked(Integer postId, Integer userId);

    void incrementComments(Integer id);

    /**
     * 减少帖子评论数（删除评论时调用）
     */
    void decrementComments(Integer id);

    ForumPostDTO createPost(Map<String, Object> postData);
}
