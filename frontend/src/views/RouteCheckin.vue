<template>
  <div>
    <h2 class="page-title">
      <el-icon><Location /></el-icon>
      路线打卡
    </h2>

    <div class="page-container">
      <h3 class="card-title">
        <el-icon><List /></el-icon>
        我的班次
      </h3>

      <el-table :data="myShifts" v-loading="loading" border style="margin-bottom: 24px;">
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
        <el-table-column prop="route.routeName" label="巡护路线" />
        <el-table-column label="完成度" width="180">
          <template #default="{ row }">
            <div v-if="statisticsMap[row.id]">
              <el-progress
                :percentage="statisticsMap[row.id].completionRate || 0"
                :status="statisticsMap[row.id].completionRate === 100 ? 'success' : ''"
              />
              <div class="progress-text">
                {{ statisticsMap[row.id].completedCheckpoints || 0 }} / {{ statisticsMap[row.id].totalCheckpoints || 0 }} 个打卡点
              </div>
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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              size="small"
              @click="signIn(row)"
            >
              <el-icon><Right /></el-icon>
              签到
            </el-button>
            <el-button
              v-if="row.status === 'IN_PROGRESS'"
              type="primary"
              size="small"
              @click="openCheckinDialog(row)"
            >
              <el-icon><Location /></el-icon>
              打卡
            </el-button>
            <el-button
              v-if="row.status === 'IN_PROGRESS'"
              type="success"
              size="small"
              @click="signOut(row)"
            >
              <el-icon><SwitchButton /></el-icon>
              签退
            </el-button>
            <el-button
              v-if="row.status === 'IN_PROGRESS'"
              size="small"
              @click="viewCheckinRecords(row)"
            >
              记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="selectedShift" class="checkin-area">
        <h3 class="card-title">
          <el-icon><MapLocation /></el-icon>
          路线打卡点 - {{ selectedShift.route?.routeName }}
        </h3>
        
        <div class="route-info">
          <div class="detail-item">
            <span class="detail-label">总打卡点:</span>
            <span class="detail-value">{{ selectedShift.route?.checkpoints || 0 }} 个</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">路线距离:</span>
            <span class="detail-value">{{ selectedShift.route?.distance || 0 }} 公里</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">预计时长:</span>
            <span class="detail-value">{{ selectedShift.route?.estimatedTime || 0 }} 分钟</span>
          </div>
        </div>

        <el-alert
          v-if="selectedShift.fireRiskLevel === 'HIGH'"
          title="今日为高火险日，请特别注意火情隐患，双人协同巡护！"
          type="error"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        />

        <el-steps :active="currentCheckpointStep" finish-status="success" simple>
          <el-step
            v-for="(point, index) in checkpoints"
            :key="index"
            :title="point.name"
            :status="point.status"
          />
        </el-steps>

        <el-table :data="checkinRecords" size="small" style="margin-top: 20px;">
          <el-table-column prop="checkpointOrder" label="顺序" width="80" align="center" />
          <el-table-column prop="checkpointName" label="打卡点名称" />
          <el-table-column prop="checkInTime" label="打卡时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.checkInTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="location" label="位置" />
          <el-table-column prop="remark" label="备注" />
        </el-table>
      </div>

      <div v-else class="empty-state">
        <el-icon :size="48" color="#909399"><Pointer /></el-icon>
        <p>请选择一个进行中的班次开始打卡</p>
      </div>
    </div>

    <el-dialog v-model="checkinDialogVisible" title="打卡" width="500px">
      <el-form :model="checkinForm" ref="checkinFormRef" label-width="100px">
        <el-form-item label="打卡点">
          <el-select v-model="checkinForm.checkpointOrder" style="width: 100%;">
            <el-option
              v-for="(p, i) in availableCheckpoints"
              :key="i"
              :label="`${i + 1}. ${p.name}`"
              :value="i + 1"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="打卡点名称">
          <el-input v-model="checkinForm.checkpointName" placeholder="请输入打卡点名称" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="checkinForm.location" placeholder="请输入位置信息" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkinForm.remark" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkinDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckin">确认打卡</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordsDialogVisible" title="打卡记录" width="700px">
      <el-table :data="checkinRecords" size="small">
        <el-table-column prop="checkpointOrder" label="顺序" width="80" align="center" />
        <el-table-column prop="checkpointName" label="打卡点名称" />
        <el-table-column prop="checkInTime" label="打卡时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.checkInTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { shiftAPI, checkInAPI } from '@/api'

const CURRENT_USER_ID = 1

const loading = ref(false)
const myShifts = ref([])
const selectedShift = ref(null)
const statisticsMap = ref({})
const checkinRecords = ref([])
const currentUserId = ref(CURRENT_USER_ID)

const checkinDialogVisible = ref(false)
const recordsDialogVisible = ref(false)
const checkinFormRef = ref(null)

const checkinForm = reactive({
  shiftId: null,
  userId: currentUserId.value,
  checkpointName: '',
  checkpointOrder: 1,
  location: '',
  remark: ''
})

const checkpoints = computed(() => {
  if (!selectedShift.value?.route) return []
  const total = selectedShift.value.route.checkpoints || 0
  const result = []
  for (let i = 1; i <= total; i++) {
    const record = checkinRecords.value.find(r => r.checkpointOrder === i)
    result.push({
      name: `打卡点${i}`,
      status: record ? 'success' : (i === nextCheckpoint.value ? 'process' : 'wait')
    })
  }
  return result
})

const availableCheckpoints = computed(() => {
  if (!selectedShift.value?.route) return []
  const total = selectedShift.value.route.checkpoints || 0
  const result = []
  for (let i = 1; i <= total; i++) {
    const exists = checkinRecords.value.find(r => r.checkpointOrder === i)
    if (!exists) {
      result.push({ name: `打卡点${i}`, order: i })
    }
  }
  return result
})

const nextCheckpoint = computed(() => {
  const done = checkinRecords.value.map(r => r.checkpointOrder).sort((a, b) => a - b)
  for (let i = 1; i <= (selectedShift.value?.route?.checkpoints || 0); i++) {
    if (!done.includes(i)) return i
  }
  return done.length + 1
})

const currentCheckpointStep = computed(() => {
  return nextCheckpoint.value - 1
})

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

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

const loadMyShifts = async () => {
  loading.value = true
  try {
    myShifts.value = await shiftAPI.getByUser(currentUserId.value)
    
    const statsPromises = myShifts.value.map(shift => 
      shiftAPI.getStatistics(shift.id).then(stat => ({ shiftId: shift.id, stat }))
    )
    const stats = await Promise.all(statsPromises)
    const map = {}
    stats.forEach(s => { map[s.shiftId] = s.stat })
    statisticsMap.value = map

    const inProgress = myShifts.value.find(s => s.status === 'IN_PROGRESS')
    if (inProgress) {
      selectedShift.value = inProgress
      loadCheckinRecords(inProgress.id)
    }
  } finally {
    loading.value = false
  }
}

const loadCheckinRecords = async (shiftId) => {
  checkinRecords.value = await checkInAPI.getByShiftAndUser(shiftId, currentUserId.value)
}

const signIn = async (row) => {
  try {
    await ElMessageBox.confirm('确定签到开始值班吗？', '签到确认', { type: 'info' })
    await shiftAPI.signIn({ shiftId: row.id, userId: currentUserId.value })
    ElMessage.success('签到成功')
    selectedShift.value = row
    loadMyShifts()
    loadCheckinRecords(row.id)
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const signOut = async (row) => {
  try {
    await ElMessageBox.confirm('确定签退吗？签退后无法继续打卡。', '签退确认', { type: 'warning' })
    await shiftAPI.signOut({ shiftId: row.id, userId: currentUserId.value })
    ElMessage.success('签退成功')
    loadMyShifts()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const openCheckinDialog = (row) => {
  selectedShift.value = row
  checkinForm.shiftId = row.id
  checkinForm.checkpointOrder = nextCheckpoint.value
  checkinForm.checkpointName = `打卡点${nextCheckpoint.value}`
  checkinForm.location = ''
  checkinForm.remark = ''
  loadCheckinRecords(row.id)
  checkinDialogVisible.value = true
}

const submitCheckin = async () => {
  try {
    await checkInAPI.checkIn(checkinForm)
    ElMessage.success('打卡成功')
    checkinDialogVisible.value = false
    loadCheckinRecords(selectedShift.value.id)
    loadMyShifts()
  } catch (e) {
    console.error(e)
  }
}

const viewCheckinRecords = async (row) => {
  selectedShift.value = row
  await loadCheckinRecords(row.id)
  recordsDialogVisible.value = true
}

onMounted(loadMyShifts)
</script>

<style scoped>
.checkin-area {
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.route-info {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-label {
  color: #909399;
}

.detail-value {
  font-weight: 600;
  color: #303133;
}
</style>
