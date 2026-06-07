# 森林防火巡护值班系统

## 功能角色
- **护林员**: 查看班次和路线，进行路线打卡
- **值班长**: 排班管理
- **指挥员**: 跟踪处置反馈

## 业务规则
- 高火险日必须双人值守
- 路线未完成不能签退

## 页面模块
1. 班次排定
2. 路线打卡
3. 异常上报
4. 处置反馈
5. 查询统计

## 快速启动
```bash
docker-compose up -d
```

## 访问地址
- 前端: http://localhost:8080
- 后端API: http://localhost:8081/api
- API文档: http://localhost:8081/api/swagger-ui.html
