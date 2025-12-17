import request from '@/utils/request'

// 查询API接口统计列表
export function listApiStatistics(query) {
  return request({
    url: '/monitor/api/statistics/list',
    method: 'get',
    params: query
  })
}

// 查询指定接口的统计信息
export function getApiStatistics(query) {
  return request({
    url: '/monitor/api/statistics/detail',
    method: 'get',
    params: query
  })
}

// 获取严重耗时接口列表
export function getSlowApiList(query) {
  return request({
    url: '/monitor/api/statistics/slow',
    method: 'get',
    params: query
  })
}

// 获取频繁调用接口列表
export function getFrequentApiList(query) {
  return request({
    url: '/monitor/api/statistics/frequent',
    method: 'get',
    params: query
  })
}

// 获取异常接口列表
export function getErrorApiList(query) {
  return request({
    url: '/monitor/api/statistics/error',
    method: 'get',
    params: query
  })
}

// 清理指定接口的旧记录
export function cleanOldRecords(apiKey) {
  return request({
    url: '/monitor/api/statistics/clean',
    method: 'delete',
    params: { apiKey }
  })
}

// 清理异常记录
export function cleanErrors() {
  return request({
    url: '/monitor/api/statistics/deleteErrors',
    method: 'delete'
  })
}
