import request from '@/utils/request'

export const userAPI = {
  list: () => request.get('/users'),
  getRangers: () => request.get('/users/rangers'),
  getByRole: (role) => request.get(`/users/role/${role}`),
  get: (id) => request.get(`/users/${id}`),
  create: (data) => request.post('/users', data),
  update: (id, data) => request.put(`/users/${id}`, data),
  delete: (id) => request.delete(`/users/${id}`)
}

export const routeAPI = {
  list: () => request.get('/routes'),
  getActive: () => request.get('/routes/active'),
  get: (id) => request.get(`/routes/${id}`),
  create: (data) => request.post('/routes', data),
  update: (id, data) => request.put(`/routes/${id}`, data),
  delete: (id) => request.delete(`/routes/${id}`)
}

export const shiftAPI = {
  list: (params) => request.get('/shifts', { params }),
  getByUser: (userId) => request.get(`/shifts/user/${userId}`),
  get: (id) => request.get(`/shifts/${id}`),
  getStatistics: (id) => request.get(`/shifts/${id}/statistics`),
  create: (data) => request.post('/shifts', data),
  update: (id, data) => request.put(`/shifts/${id}`, data),
  signIn: (data) => request.post('/shifts/sign-in', data),
  signOut: (data) => request.post('/shifts/sign-out', data),
  cancel: (id) => request.post(`/shifts/${id}/cancel`)
}

export const checkInAPI = {
  getByShift: (shiftId) => request.get(`/check-ins/shift/${shiftId}`),
  getByShiftAndUser: (shiftId, userId) => request.get(`/check-ins/shift/${shiftId}/user/${userId}`),
  checkIn: (data) => request.post('/check-ins', data),
  count: (shiftId) => request.get(`/check-ins/shift/${shiftId}/count`)
}

export const incidentAPI = {
  list: (params) => request.get('/incidents', { params }),
  getPending: () => request.get('/incidents/pending'),
  get: (id) => request.get(`/incidents/${id}`),
  getFeedbacks: (id) => request.get(`/incidents/${id}/feedbacks`),
  report: (data) => request.post('/incidents', data),
  addFeedback: (data) => request.post('/incidents/feedback', data),
  updateStatus: (id, status) => request.put(`/incidents/${id}/status?status=${status}`)
}

export const queryAPI = {
  dashboard: () => request.get('/query/dashboard'),
  getShiftDetail: (shiftId) => request.get(`/query/shift/${shiftId}`),
  queryShifts: (params) => request.get('/query/shifts', { params }),
  queryIncidents: (params) => request.get('/query/incidents', { params })
}
