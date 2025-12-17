import request from '@/utils/request'

// 测试权限SQL构建接口
export function testAuthSql(dataScope, fieldGroup, deptCodes) {
  // 手动构建 URL 参数，确保数组参数正确传递
  let url = '/moduelTest/authSql?'
  url += 'dataScop=' + encodeURIComponent(dataScope || '')
  url += '&fieldGroup=' + encodeURIComponent(fieldGroup || '')
  
  // 将部门代码数组转换为多个同名参数（Spring可变参数支持）
  // 格式：deptCode=xxx&deptCode=yyy
  if (Array.isArray(deptCodes) && deptCodes.length > 0) {
    deptCodes.filter(code => code).forEach(code => {
      url += '&deptCode=' + encodeURIComponent(code)
    })
  }
  
  return request({
    url: url,
    method: 'get',
    params: {} // 空对象，因为参数已经在 URL 中
  })
}
