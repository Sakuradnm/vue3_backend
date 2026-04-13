-- 论坛帖子点赞记录表
CREATE TABLE IF NOT EXISTS `forum_post_likes` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` INT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`) COMMENT '唯一约束：同一用户对同一帖子只能点赞一次',
    KEY `idx_post_id` (`post_id`) COMMENT '帖子ID索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    CONSTRAINT `fk_forum_post_likes_post` FOREIGN KEY (`post_id`) REFERENCES `forum_posts` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_forum_post_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛帖子点赞记录表';

-- 论坛评论点赞记录表
CREATE TABLE IF NOT EXISTS `forum_comment_likes` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `comment_id` INT NOT NULL COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`) COMMENT '唯一约束：同一用户对同一评论只能点赞一次',
    KEY `idx_comment_id` (`comment_id`) COMMENT '评论ID索引',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    CONSTRAINT `fk_forum_comment_likes_comment` FOREIGN KEY (`comment_id`) REFERENCES `forum_comments` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_forum_comment_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛评论点赞记录表';
