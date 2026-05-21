import type { Router } from 'vue-router'
import { nextTick, watch } from 'vue'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'

const legacyTextKeys: Record<string, string> = {
  首页: 'dashboard.home',
  搜索: 'common.search',
  重置: 'common.reset',
  新增: 'common.add',
  修改: 'common.edit',
  删除: 'common.delete',
  清空: 'common.clear',
  保存: 'common.save',
  导入: 'common.import',
  导出: 'common.export',
  详细: 'common.detail',
  关闭: 'common.close',
  确定: 'common.confirm',
  '确 定': 'common.confirm',
  取消: 'common.cancel',
  '取 消': 'common.cancel',
  操作: 'common.operate',
  序号: 'common.index',
  创建时间: 'common.createTime',
  更新时间: 'common.updateTime',
  开始日期: 'common.startDate',
  结束日期: 'common.endDate',
  显示搜索: 'common.showSearch',
  隐藏搜索: 'common.hideSearch',
  刷新: 'common.refresh',
  显隐列: 'common.columnVisibility',
  请选择: 'common.selectPlaceholder',
  用户编号: 'user.userId',
  用户名称: 'user.userName',
  用户昵称: 'user.nickName',
  手机号码: 'user.phonenumber',
  部门: 'user.deptName',
  状态: 'user.status',
  用户状态: 'user.statusPlaceholder',
  归属部门: 'user.deptId',
  用户密码: 'user.password',
  用户性别: 'user.sex',
  性别: 'user.sex',
  岗位: 'user.post',
  角色: 'user.role',
  备注: 'user.remark',
  请输入部门名称: 'user.deptNamePlaceholder',
  请输入用户名称: 'user.userNamePlaceholder',
  请输入用户昵称: 'user.nickNamePlaceholder',
  请输入手机号码: 'user.phonenumberPlaceholder',
  请输入邮箱: 'user.emailPlaceholder',
  请输入用户密码: 'user.passwordPlaceholder',
  请选择归属部门: 'user.deptIdPlaceholder',
  请输入内容: 'user.remarkPlaceholder',
  修改用户: 'user.editUser',
  删除用户: 'user.deleteUser',
  修改角色: 'legacy.editRole',
  删除角色: 'legacy.deleteRole',
  设置角色数据权限: 'legacy.setRoleDataScope',
  分配角色用户: 'legacy.assignRoleUser',
  重置密码: 'user.resetPassword',
  修改密码: 'legacy.changePassword',
  重置用户密码: 'user.resetUserPassword',
  分配角色: 'user.assignRole',
  分配用户角色: 'user.assignUserRole',
  用户导入: 'user.importTitle',
  导入结果: 'user.importResult',
  下载模板: 'user.downloadTemplate',
  '是否更新已经存在的用户数据': 'user.updateExistingUser',
  '仅允许导入xls、xlsx格式文件。': 'user.importFileTip',
  角色编号: 'legacy.roleId',
  角色名称: 'legacy.roleName',
  请输入角色名称: 'legacy.roleNamePlaceholder',
  权限字符: 'legacy.roleKey',
  请输入权限字符: 'legacy.roleKeyPlaceholder',
  显示顺序: 'legacy.orderNum',
  菜单权限: 'legacy.menuPermission',
  数据权限: 'legacy.dataPermission',
  分配用户: 'legacy.assignUser',
  菜单名称: 'menu.menuName',
  菜单状态: 'menu.menuStatus',
  菜单类型: 'menu.menuType',
  菜单图标: 'menu.menuIcon',
  路由地址: 'menu.path',
  组件路径: 'menu.component',
  权限标识: 'menu.perms',
  部门名称: 'legacy.deptName',
  负责人: 'legacy.leader',
  联系电话: 'legacy.phone',
  邮箱: 'user.email',
  岗位编号: 'legacy.postId',
  岗位编码: 'legacy.postCode',
  岗位名称: 'legacy.postName',
  岗位排序: 'legacy.postSort',
  字典编号: 'legacy.dictId',
  字典名称: 'legacy.dictName',
  请输入字典名称: 'legacy.dictNamePlaceholder',
  字典类型: 'legacy.dictType',
  请输入字典类型: 'legacy.dictTypePlaceholder',
  字典标签: 'legacy.dictLabel',
  字典键值: 'legacy.dictValue',
  字典排序: 'legacy.dictSort',
  国际化键: 'menu.i18nKey',
  参数编号: 'legacy.configId',
  参数名称: 'legacy.configName',
  参数键名: 'legacy.configKey',
  参数键值: 'legacy.configValue',
  系统内置: 'legacy.configType',
  公告标题: 'legacy.noticeTitle',
  公告类型: 'legacy.noticeType',
  发布状态: 'legacy.noticeStatus',
  文件名: 'legacy.fileName',
  原名: 'legacy.originalName',
  文件后缀: 'legacy.fileSuffix',
  服务商: 'legacy.provider',
  上传人: 'legacy.createBy',
  登录地址: 'legacy.loginIp',
  请输入登录地址: 'legacy.loginIpPlaceholder',
  会话编号: 'legacy.sessionId',
  登录名称: 'legacy.loginName',
  所属部门: 'legacy.dept',
  主机: 'legacy.host',
  登录地点: 'legacy.loginLocation',
  操作系统: 'legacy.os',
  浏览器: 'legacy.browser',
  登录时间: 'legacy.loginTime',
  系统模块: 'legacy.systemModule',
  操作人员: 'legacy.operator',
  类型: 'legacy.type',
  操作时间: 'legacy.operTime',
  操作地址: 'legacy.operIp',
  表名称: 'gen.tableName',
  表描述: 'gen.tableComment',
  实体: 'gen.className',
  数据源: 'gen.dataName',
  生成: 'gen.generate',
  生成代码: 'gen.generateCode',
  预览: 'common.preview',
  同步: 'common.sync',
  上传文件: 'legacy.uploadFile',
  上传图片: 'legacy.uploadImage',
  刷新缓存: 'legacy.refreshCache',
  展开: 'legacy.expand',
  折叠: 'legacy.collapse',
  强退: 'legacy.forceLogout',
  解锁: 'legacy.unlock',
  旧密码: 'legacy.oldPassword',
  请输入旧密码: 'legacy.oldPasswordPlaceholder',
  新密码: 'legacy.newPassword',
  请输入新密码: 'legacy.newPasswordPlaceholder',
  确认密码: 'legacy.confirmPassword',
  请确认新密码: 'legacy.confirmPasswordPlaceholder'
}

const normalizedKeys = new Map<string, string>()
Object.entries(legacyTextKeys).forEach(([text, key]) => {
  normalizedKeys.set(normalize(text), key)
})

const textSelectors = [
  '.el-button',
  '.el-form-item__label',
  '.el-table__header-wrapper .cell',
  '.el-dialog__title',
  '.el-message-box__title',
  '.el-message-box__message',
  '.el-link',
  '.el-tabs__item',
  '.el-upload__text',
  '.el-upload__tip',
  '.el-empty__description',
  '.el-pagination__total',
  '.el-pagination__goto',
  '.el-pagination__classifier',
  '.el-dropdown-menu__item'
].join(',')

const attrNames = ['placeholder', 'aria-label', 'title'] as const
const originalTextNodes = new WeakMap<Text, string>()

export function installLegacyUiI18n(router: Router) {
  const localeStore = useLocaleStore()
  let observer: MutationObserver | undefined
  let scheduled = false

  function schedule() {
    if (scheduled) return
    scheduled = true
    window.requestAnimationFrame(() => {
      scheduled = false
      patchDocument(localeStore.language)
    })
  }

  router.afterEach(() => nextTick(schedule))
  watch(() => localeStore.language, () => nextTick(schedule))

  nextTick(() => {
    schedule()
    observer = new MutationObserver(schedule)
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      characterData: true,
      attributes: true,
      attributeFilter: [...attrNames]
    })
  })

  window.addEventListener('beforeunload', () => observer?.disconnect(), { once: true })
}

function patchDocument(locale: string) {
  document.querySelectorAll<HTMLElement>(textSelectors).forEach((element) => {
    patchElementText(element, locale)
  })
  document.querySelectorAll<HTMLElement>('input, textarea, button, [title], [aria-label]').forEach((element) => {
    patchElementAttrs(element, locale)
  })
}

function patchElementText(element: HTMLElement, locale: string) {
  if (element.closest('.el-table__body-wrapper') && !element.matches('.el-button, .el-link')) return
  element.childNodes.forEach((node) => {
    if (node.nodeType === Node.TEXT_NODE) {
      patchTextNode(node as Text, locale)
      return
    }
    if (node.nodeType === Node.ELEMENT_NODE) {
      const child = node as HTMLElement
      if (!['svg', 'path'].includes(child.tagName.toLowerCase())) {
        child.childNodes.forEach((childNode) => {
          if (childNode.nodeType === Node.TEXT_NODE) {
            patchTextNode(childNode as Text, locale)
          }
        })
      }
    }
  })
}

function patchElementAttrs(element: HTMLElement, locale: string) {
  attrNames.forEach((attr) => {
    const dataKey = `legacyI18n${toDatasetSuffix(attr)}`
    const dataset = element.dataset as Record<string, string | undefined>
    const original = dataset[dataKey] || element.getAttribute(attr)?.trim()
    const key = findKey(original)
    if (!original || !key) return
    dataset[dataKey] = original
    const translated = getMessage(key, locale)
    if (element.getAttribute(attr) !== translated) {
      element.setAttribute(attr, translated)
    }
  })
}

function patchTextNode(node: Text, locale: string) {
  const original = originalTextNodes.get(node) || node.nodeValue?.trim()
  const key = findKey(original)
  if (!original || !key) return
  originalTextNodes.set(node, original)
  const translated = getMessage(key, locale)
  if (node.nodeValue?.trim() === translated) return
  const prefix = node.nodeValue?.match(/^\s*/)?.[0] || ''
  const suffix = node.nodeValue?.match(/\s*$/)?.[0] || ''
  node.nodeValue = `${prefix}${translated}${suffix}`
}

function findKey(value?: string | null) {
  if (!value) return undefined
  return legacyTextKeys[value] || normalizedKeys.get(normalize(value))
}

function normalize(value: string) {
  return value.replace(/\s+/g, '')
}

function toDatasetSuffix(value: string) {
  return value.replace(/(^|-)([a-z])/g, (_, __, letter: string) => letter.toUpperCase())
}
