import {
  getPageAgentApiKey,
  getPageAgentBaseUrl,
  getPageAgentModel,
  isPageAgentEnabled
} from '@/utils/config'
import useLocaleStore from '@/stores/locale'
import {
  getBaseInfoPageInstructions,
  getGlobalCapabilityInstructions,
  normalizeInstructionLocale,
  preloadBaseInfoPageInstructions
} from './pageInstructions'

type PageAgentInstance = {
  panel?: {
    show: () => void
  }
  dispose?: () => void
}

let pageAgent: PageAgentInstance | null = null
let pageAgentLocale = ''
const pageAgentAttributes = [
  'aria-label',
  'title',
  'placeholder',
  'role',
  'data-agent-label',
  'data-agent-field',
  'data-agent-scope',
  'data-agent-entity',
  'data-agent-row',
  'data-agent-action',
  'data-agent-status',
  'data-agent-risk',
  'data-agent-confirm-required',
  'data-agent-confirm-message',
  'data-agent-danger'
]
const baseAssistantInstructions = {
  zh_CN: `
你是 Dealer Admin Portal 的系统助手，当前阶段优先服务基础信息模块。
你可以帮助用户从任意页面定位功能、查询页面数据、解释当前页面、引导用户完成操作、演示操作流程、打开表单并填写草稿。
回答要短，优先给结论和下一步；除非用户要求详细说明，不要输出长篇背景。
不要向业务用户提及元素索引、按钮索引、DOM 节点编号、内部 ID、工具参数、原始 HTML 或调试日志。
执行页面操作前先观察当前页面状态；如果弹窗已打开，优先复用当前弹窗或先征得用户同意关闭。
不要声称已经保存、删除、审核、导出或上传，除非页面明确显示该操作已经由用户完成。
遇到带有 data-agent-confirm-required="true"、data-agent-risk="confirm-required" 或 data-agent-danger 的元素时，必须先询问用户确认；最终保存、最终删除、状态切换确认、超级修改确认、上传附件确认、导出确认、批量操作确认必须停下，交给用户手动点击或等待用户明确授权。
允许做高风险操作前的安全准备动作，例如打开弹窗、填写草稿、引用检查、展示确认摘要，但不能点击最终确定/保存/删除确认。
填写数字输入框时，优先定位带 aria-label 或 data-agent-field 的业务输入框，清空旧值后输入新值，并在继续前检查可见值是否已经变更。
如果用户只是问“怎么做”，优先用中文说明步骤；如果用户要求演示，可以操作页面但在高风险确认前停下。
`.trim(),
  en_US: `
You are the system assistant for Dealer Admin Portal. In this phase, focus on the Basic Information module.
You can help users locate functions from any page, inspect page data, explain the current page, guide workflows, demonstrate operations, open forms, and fill drafts.
Keep replies short. Lead with the conclusion and next step. Do not produce long background explanations unless the user asks for detail.
Do not mention element indexes, button indexes, DOM node numbers, internal IDs, tool arguments, raw HTML, or debug logs to business users.
Observe the current page state before operating. If a dialog is already open, reuse it or ask before closing it.
Do not claim that you saved, deleted, approved, exported, or uploaded anything unless the page clearly shows that the action was completed by the user.
When an element has data-agent-confirm-required="true", data-agent-risk="confirm-required", or data-agent-danger, ask the user for confirmation first. Final save, final delete, status-change confirmation, super-edit confirmation, upload confirmation, export confirmation, and batch-operation confirmation must stop for the user to click manually or explicitly authorize.
You may perform safe preparation before high-risk actions, such as opening dialogs, filling drafts, checking references, and showing a confirmation summary, but do not click final OK/save/delete confirmation.
When filling numeric inputs, target the business input with aria-label or data-agent-field, clear the old value before entering the new value, and verify the visible value before continuing.
If the user asks how to do something, explain the steps in English. If the user asks for a demo, operate the page but stop before high-risk confirmation.
`.trim()
}

function sanitizePageAgentPanelText(text: string) {
  return text
    .replace(/TypeError:\s*Cannot create property 'action' on string .*/g, '工具响应格式异常，已自动重试。')
    .replace(/InvokeError:\s*Tool arguments validation failed/g, '工具参数校验失败，已自动重试')
    .replace(/LLM retry attempt \d+ of \d+ \(\d+\/\d+\)/g, '正在自动重试')
    .replace(/正在点击元素\s*\[\d+\]\.\.\./g, '正在点击页面控件...')
    .replace(/正在输入文本到元素\s*\[\d+\]\.\.\./g, '正在填写页面字段...')
    .replace(/✅\s*Clicked element\s*\(\[\d+\][^)]+\)\.?/g, '✅ 已完成页面点击。')
    .replace(/✅\s*Clicked element\s*\(\d+\)\.?/g, '✅ 已完成页面点击。')
    .replace(/✅\s*Input text\s*\((.*?)\)\s*into element\s*\(\[\d+\][^)]+\)\.?/g, '✅ 已填写 $1。')
    .replace(/✅\s*Input text\s*\((.*?)\)\s*into element\s*\(\d+\)\.?/g, '✅ 已填写 $1。')
    .replace(/✅\s*已点击元素\s*\[\d+\]/g, '✅ 已完成页面点击')
    .replace(/element\s*\(\d+\)/g, 'field')
    .replace(/步骤\s*\d+/g, '')
    .replace(/Step\s*\d+/g, '')
}

function cleanPageAgentPanel(root: HTMLElement) {
  root
    .querySelectorAll<HTMLElement>('button[title="复制测速日志"], button[title="清空测速日志"], button[title="Copy perf logs"], button[title="Clear perf logs"]')
    .forEach((button) => {
      button.style.display = 'none'
    })

  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT)
  const textNodes: Text[] = []
  while (walker.nextNode()) {
    textNodes.push(walker.currentNode as Text)
  }
  textNodes.forEach((node) => {
    const nextText = sanitizePageAgentPanelText(node.nodeValue || '')
    if (nextText !== node.nodeValue) node.nodeValue = nextText
  })
}

function installPageAgentPanelCleanup() {
  const root = document.getElementById('page-agent-runtime_agent-panel')
  if (!root || root.dataset.bocooCleanupInstalled === 'true') return

  root.dataset.bocooCleanupInstalled = 'true'
  cleanPageAgentPanel(root)
  const observer = new MutationObserver(() => cleanPageAgentPanel(root))
  observer.observe(root, {
    childList: true,
    subtree: true,
    characterData: true
  })
}

function assertPageAgentConfig() {
  if (!isPageAgentEnabled()) {
    throw new Error('pageAgent.disabled')
  }
  if (!getPageAgentApiKey() || !getPageAgentBaseUrl() || !getPageAgentModel()) {
    throw new Error('pageAgent.missingConfig')
  }
}

function currentInstructionLocale() {
  return normalizeInstructionLocale(useLocaleStore().language)
}

function currentPageAgentLanguage() {
  return currentInstructionLocale() === 'en_US' ? 'en-US' : 'zh-CN'
}

function currentSystemInstructions() {
  const locale = currentInstructionLocale()
  return [
    baseAssistantInstructions[locale],
    getGlobalCapabilityInstructions(locale)
  ].join('\n\n')
}

async function createPageAgent() {
  const { PageAgent } = await import('page-agent')
  const locale = currentInstructionLocale()
  await preloadBaseInfoPageInstructions(window.location.href)
  return new PageAgent({
    apiKey: getPageAgentApiKey(),
    baseURL: getPageAgentBaseUrl(),
    model: getPageAgentModel(),
    language: locale === 'en_US' ? 'en-US' : 'zh-CN',
    viewportExpansion: 0,
    includeAttributes: pageAgentAttributes,
    keepSemanticTags: true,
    promptForNextTask: true,
    instructions: {
      system: currentSystemInstructions(),
      getPageInstructions: (url: string) => {
        void preloadBaseInfoPageInstructions(url)
        return getBaseInfoPageInstructions(url, useLocaleStore().language)
      }
    }
  }) as PageAgentInstance
}

export async function openPageAgentPanel() {
  assertPageAgentConfig()
  await preloadBaseInfoPageInstructions(window.location.href)
  const language = currentPageAgentLanguage()
  if (pageAgent && pageAgentLocale !== language) {
    pageAgent.dispose?.()
    pageAgent = null
  }
  if (!pageAgent) {
    pageAgent = await createPageAgent()
    pageAgentLocale = language
  }
  pageAgent.panel?.show()
  installPageAgentPanelCleanup()
}
