<template>
  <div>
    <h2 class="page-title">
      <el-icon><Tools /></el-icon>
      处置反馈
    </h2>

    <div class="page-container">
      <div class="toolbar">
        <div class="search-form">
          <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 140px;">
            <el-option label="已上报" value="REPORTED" />
            <el-option label="处置中" value="PROCESSING" />
            <el-option label="已解决" value="RESOLVED" />
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
        <el-badge :value="pendingCount" class="item">
          <span style="color: #606266;">待处理: {{ pendingCount }} 件</span>
        </el-badge>
      </div>

      <el-alert
        title="请优先处理高风险异常，确保人员安全，及时处置火情隐患！"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-table :data="incidents" v-loading="loading" border>
        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-icon
              v-if="row.severity === 'HIGH'"
              :size="24"
              color="#f56c6c"
            >
              <BellFilled />
            </el-icon>
            <el-icon v-else-if="row.severity === 'MEDIUM'" :size="20" color="#e6a23c">
              <Warning />
            </el-icon>
            <el-icon v-else :size="18" color="#67c23a">
              <InfoFilled />
            </el-icon>
          </template>
        </el-table-column>
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
        <el-table-column label="上报人" width="100">
          <template #default="{ row }">
            {{ row.reporter?.name || '-' }}
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'CLOSED'"
              type="primary"
              size="small"
              @click="openFeedbackDialog(row)"
            >
              <el-icon><ChatDotRound /></el-icon>
              反馈
            </el-button>
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="feedbackDialogVisible" title="添加处置反馈" width="600px">
      <div v-if="currentIncident" style="margin-bottom: 20px;">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="编号">{{ currentIncident.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ incidentTypeText(currentIncident.incidentType) }}</el-descriptions-item>
          <el-descriptions-item label="严重程度">
            <el-tag :class="`tag-${currentIncident.severity.toLowerCase()}`" size="small">
              {{ severityText(currentIncident.severity) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="位置">{{ currentIncident.location }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 12px; padding: 12px; background: #f5f7fa; border-radius: 4px;">
          <strong>异常描述:</strong> {{ currentIncident.description }}
        </div>
      </div>

      <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackFormRef" label-width="100px">
        <el-form-item label="处置措施" prop="actionTaken">
          <el-input
            v-model="feedbackForm.actionTaken"
            type="textarea"
            :rows="4"
            placeholder="请详细描述已采取的处置措施"
          />
        </el-form-item>
        <el-form-item label="处置结果">
          <el-input v-model="feedbackForm.result" placeholder="请输入处置结果（可选）" />
        </el-form-item>
        <el-form-item label="更新状态">
          <el-radio-group v-model="feedbackForm.status">
            <el-radio value="PROCESSING">处置中</el-radio>
            <el-radio value="RESOLVED">已解决</el-radio>
            <el-radio value="CLOSED">已关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">提交反馈</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="异常详情" width="750px">
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
          <el-descriptions-item label="上报人">{{ currentIncident.reporter?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="上报时间">{{ formatTime(currentIncident.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="位置">{{ currentIncident.location }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(currentIncident.updatedAt) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentIncident.description }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 class="card-title" style="margin-top: 24px;">
          <el-icon><ChatDotRound /></el-icon>
          处置反馈记录
        </h4>
        <el-timeline v-if="feedbacks.length > 0">
          <el-timeline-item
            v-for="(fb, index) in feedbacks"
            :key="index"
            :timestamp="formatTime(fb.createdAt)"
            placement="top"
            :type="fb.result ? 'success' : 'primary'"
          >
            <el-card shadow="hover">
              <div style="font-weight: 600; margin-bottom: 8px; color: #409EFF;">
                <el-icon><User /></el-icon>
                {{ fb.handler?.name || '指挥员' }}
              </div>
              <div style="color: #303133; line-height: 1.6;">{{ fb.actionTaken }}</div>
              <div v-if="fb.result" style="margin-top: 10px; padding: 8px 12px; background: #f0f9eb; border-radius: 4px; color: #67c23a;">
                <strong>处置结果:</strong> {{ fb.result }}
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="empty-state">
          暂无处置反馈，请及时处理！
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { incidentAPI } from '@/api'

const COMMANDER_ID = 6

const loading = ref(false)
const incidents = ref([])
const pendingCount = ref(0)
const searchStatus = ref('')
const searchSeverity = ref('')

const feedbackDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentIncident = ref(null)
const feedbacks = ref([])
const feedbackFormRef = ref(null)

const feedbackForm = reactive({
  incidentId: null,
  handlerId: COMMANDER_ID,
  actionTaken: '',
  result: '',
  status: 'PROCESSING'
})

const feedbackRules = {
  actionTaken: [{ required: true, message: '请输入处置措施', trigger: 'blur' }]
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
    const pendingData = await incidentAPI.getPending()
    pendingCount.value = pendingData.length

    const params = {}
    if (searchStatus.value) params.status = searchStatus.value
    if (searchSeverity.value) params.severity = searchSeverity.value
    if (!searchStatus.value && !searchSeverity.value) {
      incidents.value = pendingData
    } else {
      incidents.value = await incidentAPI.list(params)
    }
  } finally {
    loading.value = false
  }
}

const openFeedbackDialog = (row) => {
  currentIncident.value = row
  feedbackForm.incidentId = row.id
  feedbackForm.actionTaken = ''
  feedbackForm.result = ''
  feedbackForm.status = row.status === 'REPORTED' ? 'PROCESSING' : row.status
  feedbackDialogVisible.value = true
}

const submitFeedback = async () => {
  await feedbackFormRef.value.validate()
  try {
    await incidentAPI.addFeedback(feedbackForm)
    ElMessage.success('处置反馈提交成功')
    feedbackDialogVisible.value = false
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
