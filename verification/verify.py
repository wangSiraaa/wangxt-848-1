#!/usr/bin/env python3
import requests
import time
import json
import sys

BASE_URL = "http://backend:8081/api"

def log(message, level="INFO"):
    print(f"[{level}] {message}", flush=True)

def wait_for_backend():
    log("等待后端服务启动...")
    max_retries = 30
    for i in range(max_retries):
        try:
            response = requests.get(f"{BASE_URL}/users", timeout=5)
            if response.status_code == 200:
                log("后端服务已就绪")
                return True
        except Exception as e:
            log(f"后端未就绪 ({i+1}/{max_retries}): {str(e)}", "WARN")
            time.sleep(3)
    log("后端服务超时未启动", "ERROR")
    return False

def get_test_users():
    log("获取测试用户列表...")
    try:
        response = requests.get(f"{BASE_URL}/users")
        if response.status_code == 200:
            data = response.json()
            users = data.get("data", []) or data
            ranger_users = [u for u in users if u.get("role") == "RANGER"]
            log(f"获取到 {len(users)} 个用户，其中护林员 {len(ranger_users)} 人")
            for u in ranger_users[:2]:
                log(f"  - {u.get('name')} (ID: {u.get('id')}, 角色: {u.get('role')})")
            return users
    except Exception as e:
        log(f"获取用户失败: {str(e)}", "ERROR")
    return []

def get_test_routes():
    log("获取巡护路线列表...")
    try:
        response = requests.get(f"{BASE_URL}/routes")
        if response.status_code == 200:
            data = response.json()
            routes = data.get("data", []) or data
            log(f"获取到 {len(routes)} 条路线")
            for r in routes:
                log(f"  - {r.get('routeName')} (ID: {r.get('id')}, 打卡点: {r.get('checkpoints')}个)")
            return routes
    except Exception as e:
        log(f"获取路线失败: {str(e)}", "ERROR")
    return []

def test_high_fire_risk_single_user_rejected(users, routes):
    log("\n" + "="*60)
    log("测试用例1: 高火险日单人排班被拒绝", "TEST")
    log("="*60)

    if len(users) < 1 or len(routes) < 1:
        log("测试数据不足，跳过测试", "ERROR")
        return False

    ranger_users = [u for u in users if u.get("role") == "RANGER"]
    if len(ranger_users) < 1:
        log("没有护林员用户，跳过测试", "ERROR")
        return False

    single_user_id = ranger_users[0]["id"]
    route_id = routes[0]["id"]

    test_payload = {
        "shiftDate": "2025-08-01",
        "shiftType": "DAY",
        "routeId": route_id,
        "fireRiskLevel": "HIGH",
        "userIds": [single_user_id],
        "startTime": "08:00:00",
        "endTime": "18:00:00"
    }

    log(f"测试请求: 高火险日 + 单人排班 (用户ID: {single_user_id})")
    log(f"请求体: {json.dumps(test_payload, ensure_ascii=False)}")

    try:
        response = requests.post(
            f"{BASE_URL}/shifts",
            json=test_payload,
            headers={"Content-Type": "application/json"}
        )

        log(f"响应状态码: {response.status_code}")
        log(f"响应体: {response.text}")

        result = response.json()
        code = result.get("code")
        message = result.get("message", "")

        if code != 200 and "双人" in message or code != 200 and "2名" in message:
            log("✅ 测试通过: 高火险日单人排班被正确拒绝", "PASS")
            log(f"   拒绝原因: {message}")
            return True
        elif code == 200:
            log("❌ 测试失败: 高火险日单人排班应该被拒绝，但实际创建成功", "FAIL")
            return False
        else:
            log(f"❌ 测试失败: 错误码={code}, 消息={message}", "FAIL")
            return False

    except Exception as e:
        log(f"❌ 测试异常: {str(e)}", "FAIL")
        return False

def test_normal_risk_single_user_accepted(users, routes):
    log("\n" + "="*60)
    log("测试用例2: 正常火险日单人排班允许", "TEST")
    log("="*60)

    ranger_users = [u for u in users if u.get("role") == "RANGER"]
    if len(ranger_users) < 1 or len(routes) < 1:
        log("测试数据不足，跳过测试", "ERROR")
        return False

    single_user_id = ranger_users[0]["id"]
    route_id = routes[0]["id"]

    test_payload = {
        "shiftDate": "2025-08-12",
        "shiftType": "DAY",
        "routeId": route_id,
        "fireRiskLevel": "NORMAL",
        "userIds": [single_user_id],
        "startTime": "08:00:00",
        "endTime": "18:00:00"
    }

    log(f"测试请求: 正常火险日 + 单人排班 (用户ID: {single_user_id})")

    try:
        response = requests.post(
            f"{BASE_URL}/shifts",
            json=test_payload,
            headers={"Content-Type": "application/json"}
        )

        log(f"响应状态码: {response.status_code}")
        result = response.json()
        code = result.get("code")

        if code == 200:
            log("✅ 测试通过: 正常火险日单人排班允许创建", "PASS")
            shift_id = result.get("data", {}).get("id")
            log(f"   创建的班次ID: {shift_id}")
            return True
        else:
            log(f"❌ 测试失败: 正常火险日单人排班应该允许，但被拒绝。消息: {result.get('message')}", "FAIL")
            return False

    except Exception as e:
        log(f"❌ 测试异常: {str(e)}", "FAIL")
        return False

def test_high_fire_risk_double_user_accepted(users, routes):
    log("\n" + "="*60)
    log("测试用例3: 高火险日双人排班允许", "TEST")
    log("="*60)

    ranger_users = [u for u in users if u.get("role") == "RANGER"]
    if len(ranger_users) < 2 or len(routes) < 1:
        log(f"测试数据不足(护林员:{len(ranger_users)}人)，跳过测试", "ERROR")
        return False

    user_ids = [ranger_users[0]["id"], ranger_users[1]["id"]]
    route_id = routes[0]["id"]

    test_payload = {
        "shiftDate": "2025-08-13",
        "shiftType": "DAY",
        "routeId": route_id,
        "fireRiskLevel": "HIGH",
        "userIds": user_ids,
        "startTime": "08:00:00",
        "endTime": "18:00:00"
    }

    log(f"测试请求: 高火险日 + 双人排班 (用户IDs: {user_ids})")

    try:
        response = requests.post(
            f"{BASE_URL}/shifts",
            json=test_payload,
            headers={"Content-Type": "application/json"}
        )

        log(f"响应状态码: {response.status_code}")
        result = response.json()
        code = result.get("code")

        if code == 200:
            log("✅ 测试通过: 高火险日双人排班允许创建", "PASS")
            shift_id = result.get("data", {}).get("id")
            log(f"   创建的班次ID: {shift_id}")
            return True
        else:
            log(f"❌ 测试失败: 高火险日双人排班应该允许，但被拒绝。消息: {result.get('message')}", "FAIL")
            return False

    except Exception as e:
        log(f"❌ 测试异常: {str(e)}", "FAIL")
        return False

def test_incomplete_route_signout_rejected(users, routes):
    log("\n" + "="*60)
    log("测试用例4: 路线未完成不能签退", "TEST")
    log("="*60)

    ranger_users = [u for u in users if u.get("role") == "RANGER"]
    if len(ranger_users) < 1 or len(routes) < 1:
        log("测试数据不足，跳过测试", "ERROR")
        return False

    route_with_checkpoints = None
    for r in routes:
        if r.get("checkpoints", 0) > 0:
            route_with_checkpoints = r
            break

    if not route_with_checkpoints:
        log("没有包含打卡点的路线，跳过测试", "WARN")
        return True

    log(f"使用路线: {route_with_checkpoints['routeName']} (打卡点: {route_with_checkpoints['checkpoints']}个)")

    shift_payload = {
        "shiftDate": "2025-08-14",
        "shiftType": "DAY",
        "routeId": route_with_checkpoints["id"],
        "fireRiskLevel": "NORMAL",
        "userIds": [ranger_users[0]["id"]],
        "startTime": "08:00:00",
        "endTime": "18:00:00"
    }

    log("步骤1: 创建一个班次...")
    try:
        resp = requests.post(f"{BASE_URL}/shifts", json=shift_payload)
        result = resp.json()
        if result.get("code") != 200:
            log(f"创建班次失败: {result.get('message')}", "ERROR")
            return False
        shift_id = result.get("data", {}).get("id")
        log(f"创建班次成功，ID: {shift_id}")
    except Exception as e:
        log(f"创建班次异常: {str(e)}", "ERROR")
        return False

    log("步骤2: 签到...")
    signin_payload = {
        "shiftId": shift_id,
        "userId": ranger_users[0]["id"]
    }
    try:
        resp = requests.post(f"{BASE_URL}/shifts/sign-in", json=signin_payload)
        result = resp.json()
        if result.get("code") != 200:
            log(f"签到失败: {result.get('message')}", "ERROR")
            return False
        log("签到成功")
    except Exception as e:
        log(f"签到异常: {str(e)}", "ERROR")
        return False

    log(f"步骤3: 尝试签退（未完成{route_with_checkpoints['checkpoints']}个打卡点）...")
    signout_payload = {
        "shiftId": shift_id,
        "userId": ranger_users[0]["id"]
    }
    try:
        resp = requests.post(f"{BASE_URL}/shifts/sign-out", json=signout_payload)
        log(f"响应状态码: {resp.status_code}")
        result = resp.json()
        code = result.get("code")
        message = result.get("message", "")

        if code != 200 and ("未完成" in message or "打卡点" in message):
            log("✅ 测试通过: 路线未完成时签退被正确拒绝", "PASS")
            log(f"   拒绝原因: {message}")
            return True
        elif code == 200:
            log("❌ 测试失败: 路线未完成应该拒绝签退，但实际签退成功", "FAIL")
            return False
        else:
            log(f"❌ 测试失败: 错误码={code}, 消息={message}", "FAIL")
            return False

    except Exception as e:
        log(f"❌ 测试异常: {str(e)}", "FAIL")
        return False

def main():
    log("森林防火巡护值班系统 - 验证测试启动")
    log("="*60)

    if not wait_for_backend():
        sys.exit(1)

    users = get_test_users()
    routes = get_test_routes()

    results = []

    results.append(("高火险日单人排班被拒绝", test_high_fire_risk_single_user_rejected(users, routes)))
    results.append(("正常火险日单人排班允许", test_normal_risk_single_user_accepted(users, routes)))
    results.append(("高火险日双人排班允许", test_high_fire_risk_double_user_accepted(users, routes)))
    results.append(("路线未完成不能签退", test_incomplete_route_signout_rejected(users, routes)))

    log("\n" + "="*60)
    log("测试结果汇总", "SUMMARY")
    log("="*60)

    passed = 0
    failed = 0
    for name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        log(f"{status} - {name}")
        if result:
            passed += 1
        else:
            failed += 1

    log("-" * 60)
    log(f"总计: {len(results)} 个测试, 通过: {passed}, 失败: {failed}")

    if failed > 0:
        log("\n❌ 存在测试失败，请检查业务逻辑", "ERROR")
        sys.exit(1)
    else:
        log("\n✅ 所有测试通过！业务规则验证成功", "SUCCESS")
        sys.exit(0)

if __name__ == "__main__":
    main()
