 /**
 * v-hasPermi 操作权限处理
 * Copyright (c) 2019 ruoyi
 */
import useUserStore from '@/store/modules/user'

export default {
  mounted(el, binding, vnode) {
    const { value } = binding
    const all_permission = "*:*:*"
    const permissions = useUserStore().permissions

    if (value && value instanceof Array && value.length > 0) {
      const permissionFlag = value

      const hasPermissions = permissions.some(permission => {
        return all_permission === permission || permissionFlag.includes(permission)
      })

      if (!hasPermissions) {
        // 获取实际的 DOM 元素（处理组件多根节点的情况）
        const targetEl = vnode.el || el
        // 确保有父节点再移除
        if (targetEl && targetEl.parentNode) {
          targetEl.parentNode.removeChild(targetEl)
        } else if (el.parentNode) {
          el.parentNode.removeChild(el)
        } else {
          // 如果找不到父节点，隐藏元素
          el.style.display = 'none'
        }
      }
    } else {
      throw new Error(`请设置操作权限标签值`)
    }
  }
}
