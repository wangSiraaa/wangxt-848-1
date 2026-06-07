<template>
  <div>
    <h2 class="page-title">
      <el-icon><Calendar /></el-icon>
      班次排定
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
          <el-select v-model="searchFireRisk" placeholder="火险等级" clearable style="width: 140px;">
            <el-option label="高火险" value="HIGH" />
            <el-option label="正常" value="NORMAL" />
            <el-option label="低" value="LOW" />
          </el-select>
          <el-button type="primary" @click="loadShifts">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </div>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          新增排班
        </el-button>
      </div>

      <el-alert
        title="高火险日必须双人值守！系统会自动校验排班人员数量。"
        type="warning"
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
              <span v-if="row.fireRiskLevel === 'HIGH' && row.users && row.users.length < 2" class="warning-text">
                ⚠️ 人数不足
              </span>
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
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              @click="openEditDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              size="small"
              type="danger"
              @click="cancelShift(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑班次' : '新增班次'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="值班日期" prop="shiftDate">
          <el-date-picker
            v-model="form.shiftDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="班次类型" prop="shiftType">
          <el-radio-group v-model="form.shiftType">
            <el-radio value="DAY">白班</el-radio>
            <el-radio value="NIGHT">夜班</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="form.startTime"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="form.endTime"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="火险等级" prop="fireRiskLevel">
          <el-select v-model="form.fireRiskLevel" style="width: 100%;">
            <el-option label="高火险" value="HIGH" />
            <el-option label="正常" value="NORMAL" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="巡护路线" prop="routeId">
          <el-select v-model="form.routeId" style="width: 100%;">
            <el-option
              v-for="route in routes"
              :key="route.id"
              :label="route.routeName"
              :value="route.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="值班人员" prop="userIds">
          <el-select
            v-model="form.userIds"
            multiple
            placeholder="请选择值班人员"
            style="width: 100%;"
          >
            <el-option
              v-for="user in rangers"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
          <div v-if="form.fireRiskLevel === 'HIGH'" class="tip-text">
            <el-icon color="#e6a23c"><Warning /></el-icon>
            高火险日请选择至少2名护林员
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveShift">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { shiftAPI, routeAPI, userAPI } from '@/api'

const loading = ref(false)
const shifts = ref([])
const routes = ref([])
const rangers = ref([])
const searchDate = ref(dayjs().format('YYYY-MM-DD'))
const searchFireRisk = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const form = reactive({
  shiftDate: dayjs().format('YYYY-MM-DD'),
  shiftType: 'DAY',
  startTime: '08:00',
  endTime: '17:00',
  fireRiskLevel: 'NORMAL',
  routeId: null,
  userIds: []
})

const rules = {
  shiftDate: [{ required: true, message: '请选择值班日期', trigger: 'change' }],
  shiftType: [{ required: true, message: '请选择班次类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  fireRiskLevel: [{ required: true, message: '请选择火险等级', trigger: 'change' }],
  routeId: [{ required: true, message: '请选择巡护路线', trigger: 'change' }],
  userIds: [{ required: true, message: '请选择值班人员', trigger: 'change' }]
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

const loadShifts = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchDate.value) {
      params.date = searchDate.value
    }
    shifts.value = await shiftAPI.list(params)
    if (searchFireRisk.value) {
      shifts.value = shifts.value.filter(s => s.fireRiskLevel === searchFireRisk.value)
    }
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  const [routeData, rangerData] = await Promise.all([
    routeAPI.getActive(),
    userAPI.getRangers()
  ])
  routes.value = routeData
  rangers.value = rangerData
}

const openCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  Object.assign(form, {
    shiftDate: dayjs().format('YYYY-MM-DD'),
    shiftType: 'DAY',
    startTime: '08:00',
    endTime: '17:00',
    fireRiskLevel: 'NORMAL',
    routeId: routes.value[0]?.id || null,
    userIds: []
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    shiftDate: row.shiftDate,
    shiftType: row.shiftType,
    startTime: row.startTime,
    endTime: row.endTime,
    fireRiskLevel: row.fireRiskLevel,
    routeId: row.routeId,
    userIds: row.users.map(u => u.id)
  })
  dialogVisible.value = true
}

const saveShift = async () => {
  await formRef.value.validate()
  
  if (form.fireRiskLevel === 'HIGH' && form.userIds.length < 2) {
    ElMessage.error('高火险日必须双人值守！请至少选择2名护林员')
    return
  }

  try {
    if (isEdit.value) {
      await shiftAPI.update(editId.value, form)
      ElMessage.success('班次更新成功')
    } else {
      await shiftAPI.create(form)
      ElMessage.success('班次创建成功')
    }
    dialogVisible.value = false
    loadShifts()
  } catch (e) {
    console.error(e)
  }
}

const cancelShift = async (row) => {
  try {
    await ElMessageBox.confirm('确定取消该班次吗？', '提示', {
      type: 'warning'
    })
    await shiftAPI.cancel(row.id)
    ElMessage.success('班次已取消')
    loadShifts()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadOptions()
  loadShifts()
})
</script>

<style scoped>
.warning-text {
  color: #f56c6c;
  font-size: 12px;
}

.tip-text {
  margin-top: 8px;
  font-size: 12px;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.users-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
</style>
