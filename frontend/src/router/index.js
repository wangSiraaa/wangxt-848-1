import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import store from '@/store'

const routes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '首页概览', roles: ['RANGER', 'LEADER', 'COMMANDER'] }
  },
  {
    path: '/shift-schedule',
    name: 'ShiftSchedule',
    component: () => import('@/views/ShiftSchedule.vue'),
    meta: { title: '班次排定', roles: ['LEADER', 'COMMANDER'] }
  },
  {
    path: '/route-checkin',
    name: 'RouteCheckin',
    component: () => import('@/views/RouteCheckin.vue'),
    meta: { title: '路线打卡', roles: ['RANGER'] }
  },
  {
    path: '/incident-report',
    name: 'IncidentReport',
    component: () => import('@/views/IncidentReport.vue'),
    meta: { title: '异常上报', roles: ['RANGER'] }
  },
  {
    path: '/incident-handle',
    name: 'IncidentHandle',
    component: () => import('@/views/IncidentHandle.vue'),
    meta: { title: '处置反馈', roles: ['COMMANDER'] }
  },
  {
    path: '/query',
    name: 'Query',
    component: () => import('@/views/Query.vue'),
    meta: { title: '查询统计', roles: ['RANGER', 'LEADER', 'COMMANDER'] }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '森林防火巡护值班系统'}`
  
  if (!to.meta || !to.meta.roles) {
    next()
    return
  }
  
  if (store.hasPermission(to)) {
    next()
  } else {
    ElMessage.warning(`您当前角色为${store.roleText}，无权限访问该页面`)
    next(store.getDefaultRoute())
  }
})

export default router
