INSERT IGNORE INTO sys_user (id, username, name, role, phone) VALUES
(1, 'ranger1', '张三', 'RANGER', '13800138001'),
(2, 'ranger2', '李四', 'RANGER', '13800138002'),
(3, 'ranger3', '王五', 'RANGER', '13800138003'),
(4, 'ranger4', '赵六', 'RANGER', '13800138004'),
(5, 'leader1', '钱七', 'LEADER', '13800138005'),
(6, 'commander1', '孙八', 'COMMANDER', '13800138006');

INSERT IGNORE INTO patrol_route (id, route_name, route_code, description, distance, estimated_time, checkpoints) VALUES
(1, '东线巡护路线', 'ROUTE-EAST-001', '从东门出发，沿山脊线巡护至瞭望塔', 5.5, 120, 6),
(2, '西线巡护路线', 'ROUTE-WEST-001', '从西门出发，沿溪谷巡护至二号检查站', 4.2, 90, 5),
(3, '南线巡护路线', 'ROUTE-SOUTH-001', '从南门出发，环绕水库周边林区', 6.8, 150, 7),
(4, '北线巡护路线', 'ROUTE-NORTH-001', '从北门出发，穿越原始次生林区', 7.5, 180, 8);
