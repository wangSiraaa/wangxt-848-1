CREATE DATABASE IF NOT EXISTS forest_patrol DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE forest_patrol;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'RANGER-护林员, LEADER-值班长, COMMANDER-指挥员',
    phone VARCHAR(20),
    status INT DEFAULT 1 COMMENT '1-正常, 0-停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS patrol_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_name VARCHAR(100) NOT NULL,
    route_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    distance DECIMAL(10,2) COMMENT '公里',
    estimated_time INT COMMENT '预计时长(分钟)',
    checkpoints INT DEFAULT 0 COMMENT '打卡点数',
    status INT DEFAULT 1 COMMENT '1-启用, 0-停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS work_shift (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_date DATE NOT NULL,
    shift_type VARCHAR(20) NOT NULL COMMENT 'DAY-白班, NIGHT-夜班',
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    fire_risk_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT 'LOW-低, NORMAL-正常, HIGH-高火险',
    route_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING-待执行, IN_PROGRESS-进行中, COMPLETED-已完成, CANCELLED-已取消',
    sign_in_time DATETIME,
    sign_out_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (route_id) REFERENCES patrol_route(id),
    INDEX idx_shift_date (shift_date),
    INDEX idx_fire_risk (fire_risk_level),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS shift_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    sign_in_time DATETIME,
    sign_out_time DATETIME,
    FOREIGN KEY (shift_id) REFERENCES work_shift(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    UNIQUE KEY uk_shift_user (shift_id, user_id),
    INDEX idx_shift_id (shift_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS check_in_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    checkpoint_name VARCHAR(100) NOT NULL,
    checkpoint_order INT NOT NULL,
    check_in_time DATETIME NOT NULL,
    location VARCHAR(200),
    remark VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shift_id) REFERENCES work_shift(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (route_id) REFERENCES patrol_route(id),
    INDEX idx_shift_id (shift_id),
    INDEX idx_user_id (user_id),
    INDEX idx_check_time (check_in_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS incident_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT,
    reporter_id BIGINT NOT NULL,
    incident_type VARCHAR(50) NOT NULL COMMENT 'FIRE-火情, ILLEGAL-违法用火, DAMAGE-设施损坏, OTHER-其他',
    severity VARCHAR(20) NOT NULL DEFAULT 'LOW' COMMENT 'LOW-低, MEDIUM-中, HIGH-高',
    location VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    images VARCHAR(1000),
    status VARCHAR(20) DEFAULT 'REPORTED' COMMENT 'REPORTED-已上报, PROCESSING-处置中, RESOLVED-已解决, CLOSED-已关闭',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shift_id) REFERENCES work_shift(id),
    FOREIGN KEY (reporter_id) REFERENCES sys_user(id),
    INDEX idx_status (status),
    INDEX idx_severity (severity),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS incident_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    handler_id BIGINT NOT NULL,
    action_taken TEXT NOT NULL,
    result VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (incident_id) REFERENCES incident_report(id),
    FOREIGN KEY (handler_id) REFERENCES sys_user(id),
    INDEX idx_incident_id (incident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS shift_receipt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    handler_id BIGINT NOT NULL,
    route_completion VARCHAR(20) NOT NULL COMMENT 'COMPLETED-完成, PARTIAL-部分完成, NOT_STARTED-未开始',
    total_checkpoints INT NOT NULL DEFAULT 0,
    completed_checkpoints INT NOT NULL DEFAULT 0,
    completion_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    sign_in_time DATETIME,
    sign_out_time DATETIME,
    handle_remark VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shift_id) REFERENCES work_shift(id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (handler_id) REFERENCES sys_user(id),
    UNIQUE KEY uk_shift_user (shift_id, user_id),
    INDEX idx_shift_id (shift_id),
    INDEX idx_user_id (user_id),
    INDEX idx_handler_id (handler_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_user (username, name, role, phone) VALUES
('ranger1', '张三', 'RANGER', '13800138001'),
('ranger2', '李四', 'RANGER', '13800138002'),
('ranger3', '王五', 'RANGER', '13800138003'),
('ranger4', '赵六', 'RANGER', '13800138004'),
('leader1', '钱七', 'LEADER', '13800138005'),
('commander1', '孙八', 'COMMANDER', '13800138006');

INSERT INTO patrol_route (route_name, route_code, description, distance, estimated_time, checkpoints) VALUES
('东线巡护路线', 'ROUTE-EAST-001', '从东门出发，沿山脊线巡护至瞭望塔', 5.5, 120, 6),
('西线巡护路线', 'ROUTE-WEST-001', '从西门出发，沿溪谷巡护至二号检查站', 4.2, 90, 5),
('南线巡护路线', 'ROUTE-SOUTH-001', '从南门出发，环绕水库周边林区', 6.8, 150, 7),
('北线巡护路线', 'ROUTE-NORTH-001', '从北门出发，穿越原始次生林区', 7.5, 180, 8);
