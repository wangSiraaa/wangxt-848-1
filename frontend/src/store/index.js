import { reactive } from 'vue'

const roles = [
  { id: 1, username: 'ranger1', name: '张三', role: 'RANGER' },
  { id: 5, username: 'leader1', name: '钱七', role: 'LEADER' },
  { id: 6, username: 'commander1', name: '孙八', role: 'COMMANDER' }
]

const state = reactive({
  currentRoleIndex: 0
})

export const store = {
  state,
  
  get currentUser() {
    return roles[state.currentRoleIndex]
  },
  
  get roleText() {
    const map = { RANGER: '护林员', LEADER: '值班长', COMMANDER: '指挥员' }
    return map[this.currentUser.role] || '未知'
  },
  
  get isRanger() {
    return this.currentUser.role === 'RANGER'
  },
  
  get isLeader() {
    return this.currentUser.role === 'LEADER'
  },
  
  get isCommander() {
    return this.currentUser.role === 'COMMANDER'
  },
  
  switchRole() {
    state.currentRoleIndex = (state.currentRoleIndex + 1) % roles.length
    return this.currentUser
  },
  
  hasPermission(route) {
    if (!route.meta || !route.meta.roles) {
      return true
    }
    return route.meta.roles.includes(this.currentUser.role)
  },
  
  getDefaultRoute() {
    if (this.isLeader) {
      return '/shift-schedule'
    } else if (this.isCommander) {
      return '/incident-handle'
    } else {
      return '/route-checkin'
    }
  }
}

export default store
