import request from '@/utils/request'

// 流程定义列表
export function listFlowDef(query) {
  return request({
    url: '/flow/definition/list',
    method: 'get',
    params: query
  })
}

// 流程定义详情
export function getFlowDef(id) {
  return request({
    url: '/flow/definition/' + id,
    method: 'get'
  })
}

// 新增流程定义
export function addFlowDef(data) {
  return request({
    url: '/flow/definition',
    method: 'post',
    data: data
  })
}

// 修改流程定义
export function updateFlowDef(data) {
  return request({
    url: '/flow/definition',
    method: 'put',
    data: data
  })
}

// 删除流程定义
export function delFlowDef(ids) {
  return request({
    url: '/flow/definition/' + ids,
    method: 'delete'
  })
}

// 根据flowCode查询流程定义列表
export function listFlowDefByFlowCode(flowCode, latestPublished = false) {
  return request({
    url: '/flow/definition/listByFlowCode',
    method: 'get',
    params: { flowCode, latestPublished }
  })
}

// 发布流程
export function publishFlowDef(id) {
  return request({
    url: '/flow/definition/publish/' + id,
    method: 'post'
  })
}

// 版本迭代
export function iterateFlowDef(id) {
  return request({
    url: '/flow/definition/iterate/' + id,
    method: 'post'
  })
}

