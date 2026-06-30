import {
  getPageAgentApiKey,
  getPageAgentBaseUrl,
  getPageAgentModel,
  isPageAgentEnabled
} from '@/utils/config'
import useLocaleStore from '@/stores/locale'
import { getAiBootstrap, type AiBootstrap } from '@/api/ai'
import {
  getAgentSafetyInstructions,
  getBaseInfoPageInstructions,
  getGlobalCapabilityInstructions,
  normalizeInstructionLocale,
  preloadBaseInfoPageInstructions
} from './pageInstructions'
import { bindPanelActivity, installPageAgentPanelEnhancer } from './panelEnhancer'

type PageAgentInstance = {
  panel?: {
    show: () => void
    expand?: () => void
  }
  addEventListener?: EventTarget['addEventListener']
  stop?: () => Promise<void>
  dispose?: () => void
}

let pageAgent: PageAgentInstance | null = null
let pageAgentLocale = ''
let pageInstructionWatcherInstalled = false
let pageAgentEmergencyStopInstalled = false
let aiBootstrapCache: AiBootstrap | null = null
let aiBootstrapLoadedAt = 0
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
你是 Dealer Admin Portal 的系统助手。
你可以帮助用户从任意页面定位功能、查询页面数据、解释当前页面、引导用户完成操作、演示操作流程、打开表单并填写草稿。
当前页面有知识包时，优先按知识包理解业务边界；没有知识包时，基于当前页面 DOM 和可见菜单操作，并在不确定时追问。
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
You are the system assistant for Dealer Admin Portal.
You can help users locate functions from any page, inspect page data, explain the current page, guide workflows, demonstrate operations, open forms, and fill drafts.
When the current page has an instruction pack, follow that business context first. If no instruction pack is available, use the current page DOM and visible menus, and ask a follow-up question when uncertain.
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

function installPageInstructionPreloadWatcher() {
  if (pageInstructionWatcherInstalled) return
  pageInstructionWatcherInstalled = true
  let lastHref = window.location.href

  const preloadCurrentPage = () => {
    const href = window.location.href
    if (href === lastHref) return
    lastHref = href
    void preloadBaseInfoPageInstructions(href)
  }

  const wrapHistoryMethod = (method: 'pushState' | 'replaceState') => {
    const original = window.history[method]
    window.history[method] = function patchedHistoryMethod(...args) {
      const result = original.apply(this, args)
      window.setTimeout(preloadCurrentPage, 0)
      return result
    }
  }

  wrapHistoryMethod('pushState')
  wrapHistoryMethod('replaceState')
  window.addEventListener('popstate', preloadCurrentPage)
}

function installPageAgentEmergencyStop() {
  if (pageAgentEmergencyStopInstalled) return
  pageAgentEmergencyStopInstalled = true
  window.addEventListener(
    'keydown',
    (event) => {
      if (event.key !== 'Escape') return
      const hasAgentUi =
        Boolean(document.getElementById('page-agent-runtime_agent-panel')) ||
        Boolean(document.getElementById('page-agent-runtime_simulator-mask'))
      if (!hasAgentUi || !pageAgent) return

      event.preventDefault()
      event.stopPropagation()
      event.stopImmediatePropagation()
      void pageAgent.stop?.()
    },
    true
  )
}

async function loadAiBootstrap() {
  const now = Date.now()
  if (aiBootstrapCache && now - aiBootstrapLoadedAt < 30000) {
    return aiBootstrapCache
  }
  try {
    aiBootstrapCache = await getAiBootstrap()
    aiBootstrapLoadedAt = now
  } catch {
    aiBootstrapCache = {
      enabled: isPageAgentEnabled(),
      apiKey: getPageAgentApiKey(),
      pageAgentBaseUrl: getPageAgentBaseUrl(),
      model: getPageAgentModel()
    }
  }
  return aiBootstrapCache
}

function assertPageAgentConfig(bootstrap: AiBootstrap) {
  if (!bootstrap.enabled) {
    throw new Error('pageAgent.disabled')
  }
  if (!pageAgentApiKey(bootstrap) || !pageAgentBaseUrl(bootstrap) || !pageAgentModel(bootstrap)) {
    throw new Error('pageAgent.missingConfig')
  }
}

function pageAgentApiKey(bootstrap: AiBootstrap) {
  return bootstrap.apiKey || getPageAgentApiKey()
}

function pageAgentBaseUrl(bootstrap: AiBootstrap) {
  return bootstrap.pageAgentBaseUrl || getPageAgentBaseUrl()
}

function pageAgentModel(bootstrap: AiBootstrap) {
  return bootstrap.model || getPageAgentModel()
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
    getAgentSafetyInstructions(locale),
    getGlobalCapabilityInstructions(locale)
  ].join('\n\n')
}

async function createPageAgent(bootstrap: AiBootstrap) {
  const { PageAgent } = await import('page-agent')
  const locale = currentInstructionLocale()
  await preloadBaseInfoPageInstructions(window.location.href)
  return new PageAgent({
    apiKey: pageAgentApiKey(bootstrap),
    baseURL: pageAgentBaseUrl(bootstrap),
    model: pageAgentModel(bootstrap),
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
  const bootstrap = await loadAiBootstrap()
  assertPageAgentConfig(bootstrap)
  installPageInstructionPreloadWatcher()
  installPageAgentEmergencyStop()
  await preloadBaseInfoPageInstructions(window.location.href)
  const language = currentPageAgentLanguage()
  if (pageAgent && pageAgentLocale !== language) {
    pageAgent.dispose?.()
    pageAgent = null
  }
  if (pageAgent && !document.getElementById('page-agent-runtime_agent-panel')) {
    pageAgent = null
  }
  if (!pageAgent) {
    pageAgent = await createPageAgent(bootstrap)
    pageAgentLocale = language
  }
  if (pageAgent.addEventListener) {
    bindPanelActivity(pageAgent as PageAgentInstance & EventTarget)
  }
  pageAgent.panel?.show()
  pageAgent.panel?.expand?.()
  installPageAgentPanelEnhancer()
}
