<template>
  <div class="apple-page">
    <div class="page-header">
      <div class="header-text">
        <h2>设备资产管理</h2>
        <p>管理粮库内所有物联网传感器节点的生命周期</p>
      </div>
      <el-button type="primary" class="apple-btn" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增设备
      </el-button>
    </div>

    <div class="apple-card-container">
      <el-table :data="devices" class="apple-table">
        <el-table-column prop="deviceId" label="设备标识" width="140" />
        <el-table-column prop="deviceName" label="设备名称" min-width="200" />
        <el-table-column prop="status" label="当前状态" width="120">
          <template #default="{ row }">
            <span class="apple-status-tag" :class="row.status === 1 ? 'online' : 'offline'">
              {{ row.status === 1 ? '在线' : '离线' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="安装时间" width="180">
          <template #default="{ row }">
            {{ dayjs(row.createTime).format('YYYY-MM-DD') }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" class="apple-table-btn" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" class="apple-table-btn danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑设备' : '新增设备'" width="440px" class="apple-dialog">
      <el-form :model="form" label-position="top">
        <el-form-item label="设备唯一标识 (ID)">
          <el-input v-model="form.deviceId" :disabled="isEdit" placeholder="例如: S009" class="apple-input" />
        </el-form-item>
        <el-form-item label="设备名称/位置">
          <el-input v-model="form.deviceName" placeholder="例如: 6号粮仓北侧" class="apple-input" />
        </el-form-item>
        <el-form-item label="初始运行状态">
          <el-radio-group v-model="form.status" class="apple-radio-group">
            <el-radio-button :label="1">在线</el-radio-button>
            <el-radio-button :label="0">离线</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="apple-btn-text">取消</el-button>
          <el-button type="primary" @click="handleSubmit" class="apple-btn">保存更改</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import axios from 'axios'
import dayjs from 'dayjs'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const devices = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive<any>({ id: null, deviceId: '', deviceName: '', status: 1 })
const statusOptions = [
  { label: '在线', value: 1 },
  { label: '离线', value: 0 }
]

const fetchDevices = async () => {
  const res = await axios.get('/api/devices')
  devices.value = res.data.data
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, deviceId: '', deviceName: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要移除设备 "${row.deviceName}" 吗？此操作不可撤销。`, '安全确认', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning',
    buttonSize: 'default'
  }).then(async () => {
    await axios.delete(`/api/devices/${row.id}`)
    ElMessage.success('设备已成功移除')
    fetchDevices()
  })
}

const handleSubmit = async () => {
  if (isEdit.value) {
    await axios.put('/api/devices', form)
  } else {
    await axios.post('/api/devices', form)
  }
  ElMessage.success('配置已同步')
  dialogVisible.value = false
  fetchDevices()
}

onMounted(fetchDevices)
</script>

<style scoped>
.apple-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 10px;
}

.header-text h2 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
}

.header-text p {
  color: var(--apple-text-secondary);
  font-size: 15px;
}

.apple-card-container {
  background: white;
  border-radius: 24px;
  padding: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.02);
}

.apple-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.apple-status-tag {
  padding: 4px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
}

.apple-status-tag.online {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.apple-status-tag.offline {
  background: rgba(142, 142, 147, 0.1);
  color: #8e8e93;
}

.table-actions {
  display: flex;
  gap: 12px;
}

.apple-table-btn {
  font-size: 12px;
  font-weight: 600;
  height: 28px !important;
  padding: 0 12px !important;
  border-radius: 8px !important;
  background: rgba(0, 113, 227, 0.08) !important;
  border: none !important;
  color: var(--apple-blue) !important;
  transition: all 0.2s ease !important;
}

.apple-table-btn:hover {
  background: var(--apple-blue) !important;
  color: white !important;
  transform: translateY(-1px);
}

.apple-table-btn.danger {
  background: rgba(255, 59, 48, 0.08) !important;
  color: #ff3b30 !important;
}

.apple-table-btn.danger:hover {
  background: #ff3b30 !important;
  color: white !important;
}

.apple-btn {
  border-radius: 12px !important;
  height: 40px !important;
  padding: 0 20px !important;
}

.apple-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: #f5f5f7;
  border: 1px solid transparent;
  box-shadow: none !important;
}

.apple-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--apple-blue);
  background: white;
}
</style>

