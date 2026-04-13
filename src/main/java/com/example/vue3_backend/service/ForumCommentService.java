package com.example.vue3_backend.service;

import com.example.vue3_backend.dto.ForumCommentDTO;
import java.util.List;
import java.util.Map;

public interface ForumCommentService {

    List<ForumCommentDTO> getCommentsByPostId(Integer postId);

    ForumCommentDTO createComment(Map<String, Object> commentData);

    void likeComment(Integer id);

    /**
     * 点赞或取消点赞评论（根据用户ID）
     * @param action "like"=点赞, "unlike"=取消点赞, "toggle"=切换
     * @return true表示当前已点赞，false表示未点赞
     */
    boolean toggleLikeComment(Integer commentId, Integer userId, String action);

    /**
     * 检查用户是否已点赞某评论
     * @return true表示已点赞，false表示未点赞
     */
    boolean isCommentLiked(Integer commentId, Integer userId);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param currentUserId 当前用户ID
     * @param postOwnerId 帖子所有者ID
     * @return true表示删除成功，false表示无权限或删除失败
     */
    boolean deleteComment(Integer commentId, Integer currentUserId, Integer postOwnerId);
}
