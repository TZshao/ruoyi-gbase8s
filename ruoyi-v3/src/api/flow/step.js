import request from '@/utils/request'

// 查询步骤列表
export function listFlowStep(query) {
  return request({
    url: '/flow/step/list',
    method: 'get',
    params: query
  })
}

// 查询步骤详细
export function getFlowStep(id) {
  return request({
    url: '/flow/step/' + id,
    method: 'get'
  })
}

// 新增步骤
export function addFlowStep(data) {
  return request({
    url: '/flow/step',
    method: 'post',
    data: data
  })
}

// 修改步骤
export function updateFlowStep(data) {
  return request({
    url: '/flow/step',
    method: 'put',
    data: data
  })
}

// 删除步骤
export function delFlowStep(ids) {
  return request({
    url: '/flow/step/' + ids,
    method: 'delete'
  })
}

