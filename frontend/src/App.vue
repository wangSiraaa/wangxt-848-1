<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
        <el-icon :size="32" color="#409EFF"><Tree /></el-icon>
        <h1>森林防火巡护值班系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click">
          <span class="user-info">
            <el-avatar :size="32" :icon="UserFilled" />
            <span class="username">{{ currentUser.name }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>
                <span>角色: {{ roleText }}</span>
              </el-dropdown-item>
              <el-dropdown-item divided @click="switchRole">
                切换角色
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="app-aside">
        <el-menu
          :default-active="activeMenu"
          router
          class="side-menu"
          background-color="#1f2d3d"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>首页概览</span>
          </el-menu-item>
          <el-menu-item v-if="isLeader" index="/shift-schedule">
            <el-icon><Calendar /></el-icon>
            <span>班次排定</span>
          </el-menu-item>
          <el-menu-item v-if="isRanger" index="/route-checkin">
            <el-icon><Location /></el-icon>
            <span>路线打卡</span>
          </el-menu-item>
          <el-menu-item v-if="isRanger" index="/incident-report">
            <el-icon><Warning /></el-icon>
            <span>异常上报</span>
          </el-menu-item>
          <el-menu-item v-if="isCommander" index="/incident-handle">
            <el-icon><Tools /></el-icon>
            <span>处置反馈</span>
          </el-menu-item>
          <el-menu-item index="/query">
            <el-icon><Search /></el-icon>
            <span>查询统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="$route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import store from '@/store'

const route = useRoute()
const router = useRouter()
const activeMenu = ref(route.path)

const currentUser = computed(() => store.currentUser)
const roleText = computed(() => store.roleText)
const isRanger = computed(() => store.isRanger)
const isLeader = computed(() => store.isLeader)
const isCommander = computed(() => store.isCommander)

const switchRole = () => {
  const user = store.switchRole()
  ElMessage.success(`已切换到${store.roleText}角色: ${user.name}`)
  
  if (!store.hasPermission(route)) {
    router.push(store.getDefaultRoute())
  }
}

watch(() => route.path, (path) => {
  activeMenu.value = path
})

if (!route.path || route.path === '/') {
  router.push('/dashboard')
}
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  background: linear-gradient(90deg, #1f2d3d 0%, #2c3e50 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h1 {
  color: #fff;
  font-size: 22px;
  margin: 0;
  font-weight: 600;
  letter-spacing: 2px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255,255,255,0.1);
}

.username {
  font-size: 14px;
}

.app-aside {
  background: #1f2d3d;
}

.side-menu {
  height: 100%;
  border-right: none;
}

.app-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
