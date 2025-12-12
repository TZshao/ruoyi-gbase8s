import request from '@/utils/request'

// 实例列表
export function listInstance(query) {
  return request({
    url: '/flow/instance/list',
    method: 'get',
    params: query
  })
}

// 实例详情
export function getInstance(id) {
  return request({
    url: '/flow/instance/' + id,
    method: 'get'
  })
}

// 新建实例（发起流程）
export function startInstance(data) {
  return request({
    url: '/flow/instance/start',
    method: 'post',
    data: data
  })
}

// 更新实例
export function updateInstance(data) {
  return request({
    url: '/flow/instance',
    method: 'put',
    data: data
  })
}

// 提交实例
export function submitInstance(id) {
  return request({
    url: '/flow/instance/' + id + '/submit',
    method: 'post'
  })
}

// 删除实例
export function delInstance(ids) {
  return request({
    url: '/flow/instance/' + ids,
    method: 'delete'
  })
}

// 审批操作
export function approveInstance(id, data) {
  return request({
    url: `/flow/instance/${id}/action`,
    method: 'post',
    data: data
  })
}

// 审批记录
export function listInstanceActions(id) {
  return request({
    url: `/flow/instance/${id}/actions`,
    method: 'get'
  })
}

