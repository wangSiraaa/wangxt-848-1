<template>
  <div>
    <h2 class="page-title">
      <el-icon><DataAnalysis /></el-icon>
      首页概览
    </h2>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <div class="stat-card">
          <div class="label">今日班次总数</div>
          <div class="value">{{ dashboard.todayTotal || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card warning">
          <div class="label">待执行</div>
          <div class="value">{{ dashboard.todayPending || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card success">
          <div class="label">进行中</div>
          <div class="value">{{ dashboard.todayInProgress || 0 }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card info">
          <div class="label">已完成</div>
          <div class="value">{{ dashboard.todayCompleted || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <div class="page-container">
          <div class="card-title">
            <el-icon><Warning /></el-icon>
            待处理异常
          </div>
          <el-row :gutter="16" style="margin-bottom: 20px;">
            <el-col :span="8">
              <div class="stat-card danger" style="padding: 16px;">
                <div class="label" style="font-size: 13px;">高风险</div>
                <div class="value" style="font-size: 28px;">{{ dashboard.highRiskIncidents || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card warning" style="padding: 16px;">
                <div class="label" style="font-size: 13px;">中风险</div>
                <div class="value" style="font-size: 28px;">{{ dashboard.mediumRiskIncidents || 0 }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card info" style="padding: 16px;">
                <div class="label" style="font-size: 13px;">低风险</div>
                <div class="value" style="font-size: 28px;">{{ dashboard.lowRiskIncidents || 0 }}</div>
              </div>
            </el-col>
          </el-row>
          <el-table :data="recentIncidents" v-loading="loading" size="small">
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ incidentTypeText(row.incidentType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="严重程度" width="100">
              <template #default="{ row }">
                <el-tag :class="`tag-${row.severity.toLowerCase()}`" size="small">
                  {{ severityText(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="位置" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag type="info" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="上报时间" width="160">
              <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-container">
          <div class="card-title">
            <el-icon><List /></el-icon>
            今日班次
          </div>
          <el-table :data="todayShifts" v-loading="loading" size="small">
            <el-table-column label="班次类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.shiftType === 'DAY' ? 'success' : 'primary'" size="small">
                  {{ row.shiftType === 'DAY' ? '白班' : '夜班' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="130">
              <template #default="{ row }">
                {{ row.startTime }} - {{ row.endTime }}
              </template>
            </el-table-column>
            <el-table-column label="火险等级" width="100">
              <template #default="{ row }">
                <el-tag :class="`tag-${row.fireRiskLevel === 'HIGH' ? 'high' : row.fireRiskLevel === 'MEDIUM' ? 'medium' : 'low'}`" size="small">
                  {{ fireRiskText(row.fireRiskLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="route.routeName" label="路线" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">
                  {{ shiftStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { queryAPI, incidentAPI, shiftAPI } from '@/api'

const loading = ref(false)
const dashboard = ref({})
const recentIncidents = ref([])
const todayShifts = ref([])

const loadData = async () => {
  loading.value = true
  try {
    dashboard.value = await queryAPI.dashboard()
    
    const [incidents, shifts] = await Promise.all([
      incidentAPI.getPending(),
      shiftAPI.list({ date: dayjs().format('YYYY-MM-DD') })
    ])
    recentIncidents.value = incidents.slice(0, 5)
    todayShifts.value = shifts
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

const incidentTypeText = (type) => ({
  FIRE: '火情',
  ILLEGAL: '违法用火',
  DAMAGE: '设施损坏',
  OTHER: '其他'
}[type] || type)

const severityText = (s) => ({ HIGH: '高', MEDIUM: '中', LOW: '低' }[s] || s)
const statusText = (s) => ({
  REPORTED: '已上报',
  PROCESSING: '处置中',
  RESOLVED: '已解决',
  CLOSED: '已关闭'
}[s] || s)

const fireRiskText = (level) => ({
  HIGH: '高火险',
  NORMAL: '正常',
  LOW: '低'
}[level] || level)

const shiftStatusText = (s) => ({
  PENDING: '待执行',
  IN_PROGRESS: '进行中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}[s] || s)

const statusType = (s) => ({
  PENDING: 'warning',
  IN_PROGRESS: 'primary',
  COMPLETED: 'success',
  CANCELLED: 'info'
}[s] || '')

onMounted(loadData)
</script>
