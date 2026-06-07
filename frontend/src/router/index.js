import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '首页概览' }
  },
  {
    path: '/shift-schedule',
    name: 'ShiftSchedule',
    component: () => import('@/views/ShiftSchedule.vue'),
    meta: { title: '班次排定' }
  },
  {
    path: '/route-checkin',
    name: 'RouteCheckin',
    component: () => import('@/views/RouteCheckin.vue'),
    meta: { title: '路线打卡' }
  },
  {
    path: '/incident-report',
    name: 'IncidentReport',
    component: () => import('@/views/IncidentReport.vue'),
    meta: { title: '异常上报' }
  },
  {
    path: '/incident-handle',
    name: 'IncidentHandle',
    component: () => import('@/views/IncidentHandle.vue'),
    meta: { title: '处置反馈' }
  },
  {
    path: '/query',
    name: 'Query',
    component: () => import('@/views/Query.vue'),
    meta: { title: '查询统计' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '森林防火巡护值班系统'}`
  next()
})

export default router
