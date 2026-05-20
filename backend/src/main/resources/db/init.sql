-- 校园饭搭子系统数据库初始化脚本
CREATE DATABASE IF NOT EXISTS mealbuddy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mealbuddy;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(255),
    credit_score DECIMAL(5,1) DEFAULT 100.0,
    role VARCHAR(20) DEFAULT 'USER',
    status INT DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户偏好表
CREATE TABLE IF NOT EXISTS user_preference (
    preference_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    taste_tags VARCHAR(500),
    taboo VARCHAR(500),
    favorite_canteen VARCHAR(200),
    meal_time_period VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 约饭组表
CREATE TABLE IF NOT EXISTS group_activity (
    group_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    initiator_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    canteen VARCHAR(100) NOT NULL,
    meal_time DATETIME NOT NULL,
    max_people INT NOT NULL,
    current_people INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'RECRUITING',
    remark VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (initiator_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 申请表
CREATE TABLE IF NOT EXISTS application (
    application_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    remark VARCHAR(200),
    FOREIGN KEY (group_id) REFERENCES group_activity(group_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评价表
CREATE TABLE IF NOT EXISTS evaluation (
    evaluation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    rating DECIMAL(2,1) NOT NULL,
    content VARCHAR(500),
    is_anonymous BOOLEAN DEFAULT FALSE,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES group_activity(group_id),
    FOREIGN KEY (from_user_id) REFERENCES user(user_id),
    FOREIGN KEY (to_user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NOT NULL,
    msg_type VARCHAR(30),
    related_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- AI聊天记录表
CREATE TABLE IF NOT EXISTS ai_chat_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_question TEXT NOT NULL,
    ai_reply TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 管理员操作日志表
CREATE TABLE IF NOT EXISTS admin_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    target_type VARCHAR(30),
    target_id BIGINT,
    operation_detail VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
