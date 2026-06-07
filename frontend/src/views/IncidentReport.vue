<template>
  <div>
    <h2 class="page-title">
      <el-icon><Warning /></el-icon>
      异常上报
    </h2>

    <div class="page-container">
      <div class="toolbar">
        <div class="search-form">
          <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 140px;">
            <el-option label="已上报" value="REPORTED" />
            <el-option label="处置中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
          <el-select v-model="searchSeverity" placeholder="严重程度" clearable style="width: 140px;">
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
          <el-button type="primary" @click="loadIncidents">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </div>
        <el-button type="danger" @click="openReportDialog">
          <el-icon><Plus /></el-icon>
          上报异常
        </el-button>
      </div>

      <el-table :data="incidents" v-loading="loading" border>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column label="类型" width="110">
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
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上报时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="reportDialogVisible" title="上报异常" width="600px">
      <el-alert
        title="如遇紧急火情，请立即拨打12119报警，并同步上报系统！"
        type="error"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />
      <el-form :model="reportForm" :rules="reportRules" ref="reportFormRef" label-width="100px">
        <el-form-item label="异常类型" prop="incidentType">
          <el-select v-model="reportForm.incidentType" style="width: 100%;">
            <el-option label="火情" value="FIRE" />
            <el-option label="违法用火" value="ILLEGAL" />
            <el-option label="设施损坏" value="DAMAGE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-radio-group v-model="reportForm.severity">
            <el-radio value="HIGH">
              <el-tag class="tag-high">高</el-tag>
            </el-radio>
            <el-radio value="MEDIUM">
              <el-tag class="tag-medium">中</el-tag>
            </el-radio>
            <el-radio value="LOW">
              <el-tag class="tag-low">低</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联班次">
          <el-select v-model="reportForm.shiftId" clearable style="width: 100%;">
            <el-option
              v-for="shift in myShifts"
              :key="shift.id"
              :label="`${shift.shiftDate} ${shift.shiftType === 'DAY' ? '白班' : '夜班'}`"
              :value="shift.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="reportForm.location" placeholder="请输入具体位置" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="reportForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述异常情况"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReport">确认上报</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="异常详情" width="700px">
      <div v-if="currentIncident">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="编号">{{ currentIncident.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ incidentTypeText(currentIncident.incidentType) }}</el-descriptions-item>
          <el-descriptions-item label="严重程度">
            <el-tag :class="`tag-${currentIncident.severity.toLowerCase()}`">
              {{ severityText(currentIncident.severity) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentIncident.status)">
              {{ statusText(currentIncident.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="位置">{{ currentIncident.location }}</el-descriptions-item>
          <el-descriptions-item label="上报时间">{{ formatTime(currentIncident.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentIncident.description }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="card-title" style="margin-top: 24px;">处置反馈</h4>
        <el-timeline v-if="feedbacks.length > 0">
          <el-timeline-item
            v-for="(fb, index) in feedbacks"
            :key="index"
            :timestamp="formatTime(fb.createdAt)"
            placement="top"
          >
            <el-card shadow="never">
              <div style="font-weight: 600; margin-bottom: 8px;">
                {{ fb.handler?.name || '指挥员' }}
              </div>
              <div style="color: #606266;">{{ fb.actionTaken }}</div>
              <div v-if="fb.result" style="margin-top: 8px; color: #67c23a;">
                结果: {{ fb.result }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="empty-state">
          暂无处置反馈
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { incidentAPI, shiftAPI } from '@/api'

const CURRENT_USER_ID = 1

const loading = ref(false)
const incidents = ref([])
const myShifts = ref([])
const searchStatus = ref('')
const searchSeverity = ref('')

const reportDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentIncident = ref(null)
const feedbacks = ref([])
const reportFormRef = ref(null)

const reportForm = reactive({
  reporterId: CURRENT_USER_ID,
  incidentType: '',
  severity: 'LOW',
  shiftId: null,
  location: '',
  description: ''
})

const reportRules = {
  incidentType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
}

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

const statusType = (s) => ({
  REPORTED: 'danger',
  PROCESSING: 'warning',
  RESOLVED: 'success',
  CLOSED: 'info'
}[s] || '')

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

const loadIncidents = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchStatus.value) params.status = searchStatus.value
    if (searchSeverity.value) params.severity = searchSeverity.value
    
    let data = await incidentAPI.list(params)
    data = data.filter(i => i.reporterId === CURRENT_USER_ID)
    incidents.value = data
  } finally {
    loading.value = false
  }
}

const loadMyShifts = async () => {
  const data = await shiftAPI.getByUser(CURRENT_USER_ID)
  myShifts.value = data.filter(s => s.status === 'IN_PROGRESS')
  if (myShifts.value.length > 0) {
    reportForm.shiftId = myShifts.value[0].id
  }
}

const openReportDialog = () => {
  reportForm.incidentType = ''
  reportForm.severity = 'LOW'
  reportForm.location = ''
  reportForm.description = ''
  loadMyShifts()
  reportDialogVisible.value = true
}

const submitReport = async () => {
  await reportFormRef.value.validate()
  try {
    await incidentAPI.report(reportForm)
    ElMessage.success('异常上报成功')
    reportDialogVisible.value = false
    loadIncidents()
  } catch (e) {
    console.error(e)
  }
}

const viewDetail = async (row) => {
  currentIncident.value = row
  feedbacks.value = await incidentAPI.getFeedbacks(row.id)
  detailDialogVisible.value = true
}

onMounted(loadIncidents)
</script>
