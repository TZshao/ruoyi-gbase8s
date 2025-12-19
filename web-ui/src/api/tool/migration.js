import request from '@/utils/request'

// 获取所有实体类列表
export function getEntityClasses() {
  return request({
    url: '/dataMigration/entityClasses',
    method: 'get'
  })
}

// 获取指定实体类的字段列表
export function getEntityFields(className) {
  return request({
    url: '/dataMigration/entityFields/' + className,
    method: 'get'
  })
}

// 获取所有Mapper接口的insert方法列表
export function getMapperMethods() {
  return request({
    url: '/dataMigration/mapperMethods',
    method: 'get'
  })
}

// 验证数据转换
export function validateMigration(data) {
  return request({
    url: '/dataMigration/validate',
    method: 'post',
    data: data
  })
}

// 执行数据迁移
export function migrateData(data) {
  return request({
    url: '/dataMigration/migrate',
    method: 'post',
    data: data
  })
}
