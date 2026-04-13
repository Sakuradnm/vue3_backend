-- 创建论坛分类表
CREATE TABLE IF NOT EXISTS forum_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id VARCHAR(50) NOT NULL UNIQUE,
    label VARCHAR(50) NOT NULL,
    icon VARCHAR(10) NOT NULL,
    color VARCHAR(20) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛分类表';

-- 为forum_posts表添加subtitle字段
ALTER TABLE forum_posts ADD COLUMN IF NOT EXISTS subtitle VARCHAR(200) COMMENT '副标题';

-- 插入默认分类数据
INSERT INTO forum_categories (category_id, label, icon, color, sort_order) VALUES
('frontend', '前端开发', '⬡', '#00c8ff', 1),
('algo', '算法与数据', '◆', '#a78bfa', 2),
('backend', '后端架构', '◉', '#ff6b35', 3),
('design', 'UI/UX 设计', '◇', '#ffd93d', 4),
('career', '求职经验', '◎', '#00ffb4', 5)
ON DUPLICATE KEY UPDATE label=VALUES(label);
