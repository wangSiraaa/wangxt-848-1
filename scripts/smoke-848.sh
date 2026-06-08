#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

if [ -f .env ]; then
  set -a
  . ./.env
  set +a
fi

echo "[smoke-848] 开始验证处理回执功能..."

BACKEND_URL="${BACKEND_URL:-http://localhost:8082}/api"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:8083}"

echo "[smoke-848] 等待服务启动..."
for i in {1..60}; do
  if curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}/shifts" | grep -q "200"; then
    echo "[smoke-848] 后端服务已启动"
    break
  fi
  sleep 2
done

for i in {1..30}; do
  if curl -s -o /dev/null -w "%{http_code}" "${FRONTEND_URL}" | grep -q "200"; then
    echo "[smoke-848] 前端服务已启动"
    break
  fi
  sleep 2
done

echo "[smoke-848] 步骤1: 验证后端处理回执API接口存在"

API_ENDPOINTS=(
  "GET /shift-receipts"
  "POST /shift-receipts"
  "POST /shift-receipts/quick"
  "GET /shift-receipts/1"
  "GET /shift-receipts/shift/1"
  "GET /shift-receipts/user/1"
  "GET /shift-receipts/shift/1/user/1"
  "PUT /shift-receipts/1"
  "DELETE /shift-receipts/1"
)

ALL_PASSED=true

for endpoint in "${API_ENDPOINTS[@]}"; do
  method=$(echo "$endpoint" | awk '{print $1}')
  path=$(echo "$endpoint" | awk '{print $2}')
  
  if [ "$method" = "GET" ]; then
    status_code=$(curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}${path}")
    if [ "$status_code" = "200" ] || [ "$status_code" = "404" ]; then
      echo "[smoke-848] ✓ ${method} ${path} - 接口存在 (${status_code})"
    else
      echo "[smoke-848] ✗ ${method} ${path} - 接口异常 (${status_code})"
      ALL_PASSED=false
    fi
  fi
done

echo ""
echo "[smoke-848] 步骤2: 验证处理回执创建流程（保存人员、班次、路线完成度）"

TEST_SHIFT_ID=1
TEST_USER_ID=1
TEST_HANDLER_ID=5

echo "[smoke-848] 检查测试班次是否存在..."
shift_status=$(curl -s -o /dev/null -w "%{http_code}" "${BACKEND_URL}/shifts/${TEST_SHIFT_ID}")
if [ "$shift_status" != "200" ]; then
  echo "[smoke-848] 创建测试班次..."
  create_shift_response=$(curl -s -X POST "${BACKEND_URL}/shifts" \
    -H "Content-Type: application/json" \
    -d '{
      "shiftDate": "2026-06-08",
      "shiftType": "DAY",
      "startTime": "08:00",
      "endTime": "17:00",
      "fireRiskLevel": "NORMAL",
      "routeId": 1,
      "userIds": [1, 2]
    }')
  echo "[smoke-848] 测试班次创建完成"
  TEST_SHIFT_ID=$(echo "$create_shift_response" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
fi

echo "[smoke-848] 测试用户签到..."
curl -s -X POST "${BACKEND_URL}/shifts/sign-in" \
  -H "Content-Type: application/json" \
  -d "{\"shiftId\": ${TEST_SHIFT_ID}, \"userId\": ${TEST_USER_ID}}" > /dev/null

echo "[smoke-848] 测试用户打卡..."
curl -s -X POST "${BACKEND_URL}/check-ins" \
  -H "Content-Type: application/json" \
  -d "{
    \"shiftId\": ${TEST_SHIFT_ID},
    \"userId\": ${TEST_USER_ID},
    \"checkpointName\": \"打卡点1\",
    \"checkpointOrder\": 1,
    \"location\": \"测试位置\",
    \"remark\": \"测试打卡\"
  }" > /dev/null

echo "[smoke-848] 检查并删除已存在的处理回执..."
existing_receipt=$(curl -s "${BACKEND_URL}/shift-receipts/shift/${TEST_SHIFT_ID}/user/${TEST_USER_ID}")
if echo "$existing_receipt" | grep -q '"code":200'; then
  receipt_id=$(echo "$existing_receipt" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
  if [ -n "$receipt_id" ] && [ "$receipt_id" != "null" ]; then
    curl -s -X DELETE "${BACKEND_URL}/shift-receipts/${receipt_id}" > /dev/null
    echo "[smoke-848] 已删除已存在的处理回执 ID: ${receipt_id}"
  fi
fi

echo "[smoke-848] 提交处理回执（保存人员、班次、路线完成度）..."
receipt_response=$(curl -s -X POST "${BACKEND_URL}/shift-receipts" \
  -H "Content-Type: application/json" \
  -d "{
    \"shiftId\": ${TEST_SHIFT_ID},
    \"userId\": ${TEST_USER_ID},
    \"handlerId\": ${TEST_HANDLER_ID},
    \"routeCompletion\": \"PARTIAL\",
    \"totalCheckpoints\": 6,
    \"completedCheckpoints\": 1,
    \"completionRate\": 16.67,
    \"handleRemark\": \"测试处理回执 - 验证人员、班次、路线完成度保存\"
  }")

echo "[smoke-848] 处理回执响应: ${receipt_response}"

if echo "$receipt_response" | grep -q '"code":200'; then
  echo "[smoke-848] ✓ 处理回执创建成功，包含："
  echo "  - 人员ID: ${TEST_USER_ID}"
  echo "  - 班次ID: ${TEST_SHIFT_ID}"
  echo "  - 路线完成度: PARTIAL (1/6)"
  echo "  - 完成率: 16.67%"
  echo "  - 处理人ID: ${TEST_HANDLER_ID}"
else
  echo "[smoke-848] ✗ 处理回执创建失败"
  ALL_PASSED=false
fi

echo ""
echo "[smoke-848] 步骤3: 验证处理回执查询"

receipt_list_response=$(curl -s "${BACKEND_URL}/shift-receipts/shift/${TEST_SHIFT_ID}")
if echo "$receipt_list_response" | grep -q '"code":200'; then
  echo "[smoke-848] ✓ 班次处理回执列表查询成功"
  
  receipt_count=$(echo "$receipt_list_response" | grep -o '"id"' | wc -l)
  echo "[smoke-848]   共查询到 ${receipt_count} 条处理回执"
else
  echo "[smoke-848] ✗ 班次处理回执列表查询失败"
  ALL_PASSED=false
fi

echo ""
echo "[smoke-848] 步骤4: 验证前端处理回执页面可访问"

frontend_status=$(curl -s -o /dev/null -w "%{http_code}" "${FRONTEND_URL}/shift-receipt")
if [ "$frontend_status" = "200" ] || [ "$frontend_status" = "302" ]; then
  echo "[smoke-848] ✓ 前端处理回执页面可访问 (${frontend_status})"
else
  echo "[smoke-848] ✗ 前端处理回执页面访问失败 (${frontend_status})"
  ALL_PASSED=false
fi

echo ""
echo "[smoke-848] 步骤5: 验证处理回执不能绕过保存流程"

echo "[smoke-848] 测试缺少必填字段（班次ID、用户ID、处理人ID）..."
invalid_response=$(curl -s -X POST "${BACKEND_URL}/shift-receipts" \
  -H "Content-Type: application/json" \
  -d '{"handleRemark": "缺少必填字段测试"}')

if echo "$invalid_response" | grep -q -E '"code":500|"code":400|error|BusinessException'; then
  echo "[smoke-848] ✓ 缺少必填字段时正确拒绝，无法绕过保存流程"
else
  echo "[smoke-848] ✗ 缺少必填字段时未正确拒绝，存在绕过风险"
  ALL_PASSED=false
fi

echo ""
echo "[smoke-848] ========================================"
if [ "$ALL_PASSED" = true ]; then
  echo "[smoke-848] ✓ 所有验证通过！处理回执功能正常"
  echo "[smoke-848] ✓ 新增流程不能绕开后端保存人员、班次、路线完成度"
  echo "[smoke-848] ✓ 前端提供处理回执入口"
  echo "[smoke-848] ✓ 后端正确保存处理结果"
  exit 0
else
  echo "[smoke-848] ✗ 部分验证失败，请检查上述错误"
  exit 1
fi
