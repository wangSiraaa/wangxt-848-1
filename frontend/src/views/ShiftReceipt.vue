<template>
  <div>
    <h2 class="page-title">
      <el-icon><CircleCheck /></el-icon>
      处理回执
    </h2>

    <div class="page-container">
      <div class="toolbar">
        <div class="search-form">
          <el-date-picker
            v-model="searchDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
          <el-select v-model="searchStatus" placeholder="班次状态" clearable style="width: 140px;">
            <el-option label="待执行" value="PENDING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
          <el-button type="primary" @click="loadShifts">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </div>
      </div>

      <el-alert
        title="处理回执需确认人员、班次、路线完成度信息，确保巡护任务完整闭环。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-table :data="shifts" v-loading="loading" border>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="shiftDate" label="日期" width="120" />
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
            <el-tag :class="`tag-${row.fireRiskLevel === 'HIGH' ? 'high' : row.fireRiskLevel === 'NORMAL' ? 'medium' : 'low'}`" size="small">
              {{ fireRiskText(row.fireRiskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="route.routeName" label="巡护路线" min-width="150" />
        <el-table-column label="值班人员" min-width="180">
          <template #default="{ row }">
            <div class="users-list">
              <el-tag
                v-for="user in row.users"
                :key="user.id"
                type="info"
                size="small"
                style="margin-right: 4px; margin-bottom: 4px;"
              >
                {{ user.name }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="回执状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="receiptStatusMap[row.id]" type="success" size="small">
              已回执
            </el-tag>
            <el-tag v-else type="warning" size="small">
              待回执
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="openReceiptDialog(row)"
            >
              <el-icon><CircleCheck /></el-icon>
              处理回执
            </el-button>
            <el-button
              size="small"
              @click="viewReceipt(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="receiptDialogVisible"
      title="处理回执"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentShift" style="margin-bottom: 20px;">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="班次编号">{{ currentShift.id }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ currentShift.shiftDate }}</el-descriptions-item>
          <el-descriptions-item label="班次类型">
            {{ currentShift.shiftType === 'DAY' ? '白班' : '夜班' }}
          </el-descriptions-item>
          <el-descriptions-item label="火险等级">
            {{ fireRiskText(currentShift.fireRiskLevel) }}
          </el-descriptions-item>
          <el-descriptions-item label="巡护路线">{{ currentShift.route?.routeName }}</el-descriptions-item>
          <el-descriptions-item label="总打卡点">{{ currentShift.route?.checkpoints || 0 }} 个</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form :model="receiptForm" :rules="receiptRules" ref="receiptFormRef" label-width="100px">
        <el-form-item label="值班人员" prop="userId">
          <el-select v-model="receiptForm.userId" style="width: 100%;">
            <el-option
              v-for="user in currentShift?.users || []"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="处理人" prop="handlerId">
          <el-select v-model="receiptForm.handlerId" style="width: 100%;">
            <el-option
              v-for="handler in handlers"
              :key="handler.id"
              :label="handler.name"
              :value="handler.id"
            />
          </el-select>
        </el-form-item>

        <div v-if="selectedUserStats" class="stats-panel">
          <h4 class="stats-title">
            <el-icon><DataAnalysis /></el-icon>
            巡护完成情况
          </h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">签到时间</div>
                <div class="stat-value">{{ selectedUserStats.signInTime || '未签到' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">签退时间</div>
                <div class="stat-value">{{ selectedUserStats.signOutTime || '未签退' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="stat-label">路线完成度</div>
                <div class="stat-value">{{ selectedUserStats.completionRate || 0 }}%</div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 16px;">
            <el-col :span="12">
              <div class="stat-item">
                <div class="stat-label">完成打卡点</div>
                <div class="stat-value">
                  {{ selectedUserStats.completedCheckpoints || 0 }} / {{ selectedUserStats.totalCheckpoints || 0 }} 个
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item">
                <div class="stat-label">完成状态</div>
                <div class="stat-value">
                  <el-tag :type="getCompletionTagType(selectedUserStats.routeCompletion)" size="small">
                    {{ getCompletionText(selectedUserStats.routeCompletion) }}
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-progress
            :percentage="selectedUserStats.completionRate || 0"
            :status="selectedUserStats.completionRate === 100 ? 'success' : ''"
            style="margin-top: 16px;"
          />
        </div>

        <el-form-item label="路线完成情况" prop="routeCompletion">
          <el-radio-group v-model="receiptForm.routeCompletion">
            <el-radio value="COMPLETED">已完成</el-radio>
            <el-radio value="PARTIAL">部分完成</el-radio>
            <el-radio value="NOT_STARTED">未开始</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="总打卡点" prop="totalCheckpoints">
          <el-input-number v-model="receiptForm.totalCheckpoints" :min="0" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="已完成打卡点" prop="completedCheckpoints">
          <el-input-number v-model="receiptForm.completedCheckpoints" :min="0" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="完成率(%)" prop="completionRate">
          <el-input-number
            v-model="receiptForm.completionRate"
            :min="0"
            :max="100"
            :precision="2"
            :step="1"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="处理备注" prop="handleRemark">
          <el-input
            v-model="receiptForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注，说明巡护情况"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="receiptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReceipt">
          <el-icon><CircleCheck /></el-icon>
          提交回执
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="回执详情" width="700px">
      <div v-if="currentReceipt">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="回执编号">{{ currentReceipt.id }}</el-descriptions-item>
          <el-descriptions-item label="班次编号">{{ currentReceipt.shiftId }}</el-descriptions-item>
          <el-descriptions-item label="值班人员">{{ currentReceipt.user?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ currentReceipt.handler?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="路线完成情况">
            <el-tag :type="getCompletionTagType(currentReceipt.routeCompletion)" size="small">
              {{ getCompletionText(currentReceipt.routeCompletion) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="完成率">{{ currentReceipt.completionRate }}%</el-descriptions-item>
          <el-descriptions-item label="完成打卡点">
            {{ currentReceipt.completedCheckpoints }} / {{ currentReceipt.totalCheckpoints }} 个
          </el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatTime(currentReceipt.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="签到时间">{{ formatTime(currentReceipt.signInTime) || '未签到' }}</el-descriptions-item>
          <el-descriptions-item label="签退时间">{{ formatTime(currentReceipt.signOutTime) || '未签退' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2">{{ currentReceipt.handleRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else class="empty-state">
        <el-icon :size="48" color="#909399"><Document /></el-icon>
        <p>暂无处理回执</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { shiftAPI, userAPI, receiptAPI, checkInAPI } from '@/api'
import store from '@/store'

const loading = ref(false)
const shifts = ref([])
const handlers = ref([])
const searchDate = ref(dayjs().format('YYYY-MM-DD'))
const searchStatus = ref('')

const receiptDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentShift = ref(null)
const currentReceipt = ref(null)
const receiptFormRef = ref(null)
const receiptStatusMap = ref({})
const selectedUserStats = ref(null)

const receiptForm = reactive({
  shiftId: null,
  userId: null,
  handlerId: null,
  routeCompletion: 'COMPLETED',
  totalCheckpoints: 0,
  completedCheckpoints: 0,
  completionRate: 0,
  handleRemark: ''
})

const receiptRules = {
  userId: [{ required: true, message: '请选择值班人员', trigger: 'change' }],
  handlerId: [{ required: true, message: '请选择处理人', trigger: 'change' }],
  routeCompletion: [{ required: true, message: '请选择路线完成情况', trigger: 'change' }],
  totalCheckpoints: [{ required: true, message: '请输入总打卡点数', trigger: 'blur' }],
  completedCheckpoints: [{ required: true, message: '请输入已完成打卡点数', trigger: 'blur' }],
  completionRate: [{ required: true, message: '请输入完成率', trigger: 'blur' }]
}

const fireRiskText = (level) => ({
  HIGH: '高火险',
  NORMAL: '正常',
  LOW: '低'
}[level] || level)

const statusText = (s) => ({
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

const getCompletionText = (s) => ({
  COMPLETED: '已完成',
  PARTIAL: '部分完成',
  NOT_STARTED: '未开始'
}[s] || s)

const getCompletionTagType = (s) => ({
  COMPLETED: 'success',
  PARTIAL: 'warning',
  NOT_STARTED: 'danger'
}[s] || 'info')

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : ''

const loadShifts = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchDate.value) {
      params.date = searchDate.value
    }
    shifts.value = await shiftAPI.list(params)
    if (searchStatus.value) {
      shifts.value = shifts.value.filter(s => s.status === searchStatus.value)
    }
    
    const receiptPromises = shifts.value.map(shift => 
      receiptAPI.getByShift(shift.id).then(receipts => ({ shiftId: shift.id, receipts }))
    )
    const receiptResults = await Promise.all(receiptPromises)
    const statusMap = {}
    receiptResults.forEach(r => {
      if (r.receipts && r.receipts.length > 0) {
        statusMap[r.shiftId] = true
      }
    })
    receiptStatusMap.value = statusMap
  } finally {
    loading.value = false
  }
}

const loadHandlers = async () => {
  const [leaders, commanders] = await Promise.all([
    userAPI.getByRole('LEADER'),
    userAPI.getByRole('COMMANDER')
  ])
  handlers.value = [...leaders, ...commanders]
}

const loadUserStats = async (shiftId, userId) => {
  try {
    const [checkInRecords, statistics] = await Promise.all([
      checkInAPI.getByShiftAndUser(shiftId, userId),
      shiftAPI.getStatistics(shiftId)
    ])
    
    const shift = shifts.value.find(s => s.id === shiftId)
    const shiftUser = shift?.users?.find(u => u.id === userId)
    
    const completed = checkInRecords ? checkInRecords.length : 0
    const total = statistics?.totalCheckpoints || 0
    const rate = total > 0 ? Math.round((completed / total) * 100 * 100) / 100 : 0
    
    let routeCompletion = 'NOT_STARTED'
    if (total === 0) {
      routeCompletion = 'NOT_STARTED'
    } else if (completed >= total) {
      routeCompletion = 'COMPLETED'
    } else if (completed > 0) {
      routeCompletion = 'PARTIAL'
    }
    
    selectedUserStats.value = {
      signInTime: shiftUser?.signInTime ? formatTime(shiftUser.signInTime) : (shift?.signInTime ? formatTime(shift.signInTime) : ''),
      signOutTime: shiftUser?.signOutTime ? formatTime(shiftUser.signOutTime) : (shift?.signOutTime ? formatTime(shift.signOutTime) : ''),
      completedCheckpoints: completed,
      totalCheckpoints: total,
      completionRate: rate,
      routeCompletion: routeCompletion
    }
    
    receiptForm.totalCheckpoints = total
    receiptForm.completedCheckpoints = completed
    receiptForm.completionRate = rate
    receiptForm.routeCompletion = routeCompletion
    receiptForm.signInTime = shiftUser?.signInTime || shift?.signInTime
    receiptForm.signOutTime = shiftUser?.signOutTime || shift?.signOutTime
  } catch (e) {
    console.error('加载用户统计失败', e)
    selectedUserStats.value = null
  }
}

watch(() => receiptForm.userId, (newVal) => {
  if (newVal && receiptForm.shiftId) {
    loadUserStats(receiptForm.shiftId, newVal)
  }
})

const openReceiptDialog = (row) => {
  currentShift.value = row
  receiptForm.shiftId = row.id
  receiptForm.userId = row.users?.[0]?.id || null
  receiptForm.handlerId = store.currentUser.id
  receiptForm.routeCompletion = 'COMPLETED'
  receiptForm.handleRemark = ''
  selectedUserStats.value = null
  
  if (receiptForm.userId) {
    loadUserStats(row.id, receiptForm.userId)
  }
  
  receiptDialogVisible.value = true
}

const submitReceipt = async () => {
  await receiptFormRef.value.validate()
  
  try {
    await receiptAPI.create(receiptForm)
    ElMessage.success('处理回执提交成功，已保存人员、班次、路线完成度信息')
    receiptDialogVisible.value = false
    loadShifts()
  } catch (e) {
    console.error(e)
  }
}

const viewReceipt = async (row) => {
  try {
    const receipts = await receiptAPI.getByShift(row.id)
    if (receipts && receipts.length > 0) {
      currentReceipt.value = receipts[0]
    } else {
      currentReceipt.value = null
    }
    detailDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadShifts()
  loadHandlers()
})
</script>

<style scoped>
.stats-panel {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.stats-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-item {
  background: #fff;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.users-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>
