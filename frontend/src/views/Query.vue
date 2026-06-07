<template>
  <div>
    <h2 class="page-title">
      <el-icon><Search /></el-icon>
      查询统计
    </h2>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="班次查询" name="shifts">
        <div class="page-container">
          <div class="search-form" style="margin-bottom: 20px;">
            <el-form :inline="true">
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="shiftSearch.startDate"
                  type="date"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="shiftSearch.endDate"
                  type="date"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="shiftSearch.status" clearable style="width: 140px;">
                  <el-option label="待执行" value="PENDING" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="已完成" value="COMPLETED" />
                  <el-option label="已取消" value="CANCELLED" />
                </el-select>
              </el-form-item>
              <el-form-item label="火险等级">
                <el-select v-model="shiftSearch.fireRiskLevel" clearable style="width: 140px;">
                  <el-option label="高火险" value="HIGH" />
                  <el-option label="正常" value="NORMAL" />
                  <el-option label="低" value="LOW" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="queryShifts">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
                <el-button @click="exportShiftData">
                  <el-icon><Download /></el-icon>
                  导出
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="shiftData" v-loading="loading" border>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="shiftDate" label="日期" width="120" />
            <el-table-column label="班次类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.shiftType === 'DAY' ? 'success' : 'primary'" size="small">
                  {{ row.shiftType === 'DAY' ? '白班' : '夜班' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="routeName" label="巡护路线" min-width="140" />
            <el-table-column label="值守人员" min-width="150">
              <template #default="{ row }">
                <span v-if="row.users && row.users.length > 0">
                  <el-tag
                    v-for="user in row.users"
                    :key="user.id"
                    type="info"
                    size="small"
                    style="margin-right: 5px;"
                  >
                    {{ user.realName }}
                  </el-tag>
                </span>
                <span v-else style="color: #999;">未分配</span>
              </template>
            </el-table-column>
            <el-table-column label="火险等级" width="100">
              <template #default="{ row }">
                <el-tag
                  :type="getFireRiskTagType(row.fireRiskLevel)"
                  size="small"
                >
                  {{ getFireRiskLabel(row.fireRiskLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="完成度" width="120">
              <template #default="{ row }">
                <el-progress
                  :percentage="row.completionRate || 0"
                  :stroke-width="10"
                  :color="getProgressColor(row.completionRate)"
                />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewShiftDetail(row)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="异常查询" name="incidents">
        <div class="page-container">
          <div class="search-form" style="margin-bottom: 20px;">
            <el-form :inline="true">
              <el-form-item label="开始日期">
                <el-date-picker
                  v-model="incidentSearch.startDate"
                  type="date"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="结束日期">
                <el-date-picker
                  v-model="incidentSearch.endDate"
                  type="date"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="异常类型">
                <el-select v-model="incidentSearch.type" clearable style="width: 160px;">
                  <el-option label="火情隐患" value="FIRE_HAZARD" />
                  <el-option label="非法进入" value="TRESPASSING" />
                  <el-option label="设备故障" value="EQUIPMENT_FAILURE" />
                  <el-option label="野生动物" value="WILDLIFE" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
              <el-form-item label="严重程度">
                <el-select v-model="incidentSearch.severity" clearable style="width: 140px;">
                  <el-option label="紧急" value="CRITICAL" />
                  <el-option label="高" value="HIGH" />
                  <el-option label="中" value="MEDIUM" />
                  <el-option label="低" value="LOW" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="incidentSearch.status" clearable style="width: 140px;">
                  <el-option label="待处理" value="PENDING" />
                  <el-option label="处理中" value="IN_PROGRESS" />
                  <el-option label="已处理" value="RESOLVED" />
                  <el-option label="已关闭" value="CLOSED" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="queryIncidents">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data="incidentData" v-loading="incidentLoading" border>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column prop="reportTime" label="上报时间" width="170" />
            <el-table-column label="异常类型" width="120">
              <template #default="{ row }">
                <el-tag type="warning" size="small">{{ getIncidentTypeLabel(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="严重程度" width="100">
              <template #default="{ row }">
                <el-tag :type="getSeverityTagType(row.severity)" size="small">
                  {{ getSeverityLabel(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reporterName" label="上报人" width="100" />
            <el-table-column prop="location" label="位置" min-width="140" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getIncidentStatusTagType(row.status)" size="small">
                  {{ getIncidentStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link size="small" @click="viewIncidentDetail(row)">
                  详情
                </el-button>
                <el-button
                  v-if="row.status !== 'RESOLVED' && row.status !== 'CLOSED'"
                  type="success"
                  link
                  size="small"
                  @click="handleFeedback(row)"
                >
                  处置
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="detailVisible" title="班次详情" width="700px">
      <div v-if="currentShift">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="班次编号">{{ currentShift.id }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ currentShift.shiftDate }}</el-descriptions-item>
          <el-descriptions-item label="班次类型">
            {{ currentShift.shiftType === 'DAY' ? '白班' : '夜班' }}
          </el-descriptions-item>
          <el-descriptions-item label="火险等级">
            {{ getFireRiskLabel(currentShift.fireRiskLevel) }}
          </el-descriptions-item>
          <el-descriptions-item label="巡护路线">{{ currentShift.routeName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ getStatusLabel(currentShift.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="完成度" :span="2">
            <el-progress
              :percentage="currentShift.completionRate || 0"
              :stroke-width="12"
            />
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 10px;">值守人员</h4>
        <el-table :data="currentShift.users || []" border size="small">
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="username" label="账号" width="140" />
          <el-table-column label="签到时间" width="170">
            <template #default="{ row }">
              {{ row.signInTime || '未签到' }}
            </template>
          </el-table-column>
          <el-table-column label="签退时间" width="170">
            <template #default="{ row }">
              {{ row.signOutTime || '未签退' }}
            </template>
          </el-table-column>
          <el-table-column label="打卡数">
            <template #default="{ row }">
              {{ row.checkpointCount || 0 }} / {{ currentShift.routeCheckpoints || 0 }}
            </template>
          </el-table-column>
        </el-table>

        <h4 style="margin: 20px 0 10px;">打卡记录</h4>
        <el-table :data="checkInRecords" border size="small" v-loading="recordsLoading">
          <el-table-column prop="checkpointOrder" label="打卡点" width="80" />
          <el-table-column prop="checkpointName" label="打卡点名称" min-width="140" />
          <el-table-column prop="checkTime" label="打卡时间" width="170" />
          <el-table-column prop="location" label="位置" min-width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'" size="small">
                {{ row.status === 'NORMAL' ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="incidentDetailVisible" title="异常详情" width="650px">
      <div v-if="currentIncident">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="编号">{{ currentIncident.id }}</el-descriptions-item>
          <el-descriptions-item label="上报时间">{{ currentIncident.reportTime }}</el-descriptions-item>
          <el-descriptions-item label="异常类型">
            {{ getIncidentTypeLabel(currentIncident.type) }}
          </el-descriptions-item>
          <el-descriptions-item label="严重程度">
            <el-tag :type="getSeverityTagType(currentIncident.severity)" size="small">
              {{ getSeverityLabel(currentIncident.severity) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="上报人">{{ currentIncident.reporterName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ getIncidentStatusLabel(currentIncident.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="位置" :span="2">{{ currentIncident.location }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentIncident.description }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentIncident.feedbacks && currentIncident.feedbacks.length > 0">
          <h4 style="margin: 20px 0 10px;">处置记录</h4>
          <el-timeline>
            <el-timeline-item
              v-for="feedback in currentIncident.feedbacks"
              :key="feedback.id"
              :timestamp="feedback.feedbackTime"
              :type="feedback.status === 'RESOLVED' ? 'success' : 'primary'"
            >
              <el-card>
                <h4>{{ feedback.handlerName }} - {{ getIncidentStatusLabel(feedback.status) }}</h4>
                <p>{{ feedback.remark }}</p>
                <p v-if="feedback.actionTaken" style="color: #606266; font-size: 13px;">
                  处置措施：{{ feedback.actionTaken }}
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="feedbackVisible" title="处置反馈" width="550px">
      <el-form :model="feedbackForm" label-width="100px">
        <el-form-item label="处理状态" required>
          <el-select v-model="feedbackForm.status" style="width: 100%;">
            <el-option label="处理中" value="IN_PROGRESS" />
            <el-option label="已处理" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="处置措施">
          <el-input
            v-model="feedbackForm.actionTaken"
            type="textarea"
            :rows="3"
            placeholder="请输入处置措施"
          />
        </el-form-item>
        <el-form-item label="备注" required>
          <el-input
            v-model="feedbackForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入处置备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import { queryAPI, incidentAPI } from '../api/index'

const activeTab = ref('shifts')
const loading = ref(false)
const incidentLoading = ref(false)
const recordsLoading = ref(false)

const shiftSearch = reactive({
  startDate: '',
  endDate: '',
  status: '',
  fireRiskLevel: ''
})

const incidentSearch = reactive({
  startDate: '',
  endDate: '',
  type: '',
  severity: '',
  status: ''
})

const shiftData = ref([])
const incidentData = ref([])
const checkInRecords = ref([])

const detailVisible = ref(false)
const incidentDetailVisible = ref(false)
const feedbackVisible = ref(false)
const currentShift = ref(null)
const currentIncident = ref(null)

const feedbackForm = reactive({
  incidentId: null,
  status: 'IN_PROGRESS',
  actionTaken: '',
  remark: ''
})

const queryShifts = async () => {
  loading.value = true
  try {
    const res = await queryAPI.getShiftList({
      page: 0,
      size: 100,
      ...shiftSearch
    })
    if (res.code === 200) {
      shiftData.value = res.data?.content || res.data || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    ElMessage.error('查询异常')
  } finally {
    loading.value = false
  }
}

const exportShiftData = () => {
  ElMessage.success('导出功能待实现')
}

const queryIncidents = async () => {
  incidentLoading.value = true
  try {
    const res = await queryAPI.getIncidentList({
      page: 0,
      size: 100,
      ...incidentSearch
    })
    if (res.code === 200) {
      incidentData.value = res.data?.content || res.data || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (e) {
    ElMessage.error('查询异常')
  } finally {
    incidentLoading.value = false
  }
}

const viewShiftDetail = async (row) => {
  currentShift.value = row
  detailVisible.value = true
  recordsLoading.value = true
  try {
    const res = await queryAPI.getShiftDetail(row.id)
    if (res.code === 200) {
      checkInRecords.value = res.data?.checkInRecords || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    recordsLoading.value = false
  }
}

const viewIncidentDetail = async (row) => {
  currentIncident.value = row
  incidentDetailVisible.value = true
  try {
    const res = await incidentAPI.getDetail(row.id)
    if (res.code === 200) {
      currentIncident.value = res.data
    }
  } catch (e) {
    console.error(e)
  }
}

const handleFeedback = (row) => {
  feedbackForm.incidentId = row.id
  feedbackForm.status = 'IN_PROGRESS'
  feedbackForm.actionTaken = ''
  feedbackForm.remark = ''
  feedbackVisible.value = true
}

const submitFeedback = async () => {
  if (!feedbackForm.remark) {
    ElMessage.warning('请输入处置备注')
    return
  }
  try {
    const res = await incidentAPI.submitFeedback({
      incidentId: feedbackForm.incidentId,
      status: feedbackForm.status,
      actionTaken: feedbackForm.actionTaken,
      remark: feedbackForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('处置反馈成功')
      feedbackVisible.value = false
      queryIncidents()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交异常')
  }
}

const getFireRiskLabel = (level) => {
  const map = { HIGH: '高火险', NORMAL: '正常', LOW: '低' }
  return map[level] || level
}

const getFireRiskTagType = (level) => {
  const map = { HIGH: 'danger', NORMAL: 'warning', LOW: 'success' }
  return map[level] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    PENDING: '待执行',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = {
    PENDING: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const getProgressColor = (percentage) => {
  if (percentage >= 100) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

const getIncidentTypeLabel = (type) => {
  const map = {
    FIRE_HAZARD: '火情隐患',
    TRESPASSING: '非法进入',
    EQUIPMENT_FAILURE: '设备故障',
    WILDLIFE: '野生动物',
    OTHER: '其他'
  }
  return map[type] || type
}

const getSeverityLabel = (severity) => {
  const map = { CRITICAL: '紧急', HIGH: '高', MEDIUM: '中', LOW: '低' }
  return map[severity] || severity
}

const getSeverityTagType = (severity) => {
  const map = { CRITICAL: 'danger', HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return map[severity] || 'info'
}

const getIncidentStatusLabel = (status) => {
  const map = {
    PENDING: '待处理',
    IN_PROGRESS: '处理中',
    RESOLVED: '已处理',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const getIncidentStatusTagType = (status) => {
  const map = {
    PENDING: 'danger',
    IN_PROGRESS: 'warning',
    RESOLVED: 'success',
    CLOSED: 'info'
  }
  return map[status] || 'info'
}

queryShifts()
queryIncidents()
</script>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-container {
  padding: 10px 0;
}

.search-form {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 6px;
}
</style>
