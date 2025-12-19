import request from '@/utils/request'

// 查询表列表
export function listTables() {
  return request({
    url: '/dataSource/tables',
    method: 'get'
  })
}

// 查询表结构
export function getTableColumns(tableName) {
  return request({
    url: '/dataSource/table/' + tableName,
    method: 'get'
  })
}
