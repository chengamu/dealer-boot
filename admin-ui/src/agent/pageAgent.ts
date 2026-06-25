import {
  getPageAgentApiKey,
  getPageAgentBaseUrl,
  getPageAgentModel,
  isPageAgentEnabled
} from '@/utils/config'

type PageAgentInstance = {
  panel?: {
    show: () => void
  }
}

let pageAgent: PageAgentInstance | null = null

function assertPageAgentConfig() {
  if (!isPageAgentEnabled()) {
    throw new Error('pageAgent.disabled')
  }
  if (!getPageAgentApiKey() || !getPageAgentBaseUrl() || !getPageAgentModel()) {
    throw new Error('pageAgent.missingConfig')
  }
}

async function createPageAgent() {
  const { PageAgent } = await import('page-agent')
  return new PageAgent({
    apiKey: getPageAgentApiKey(),
    baseURL: getPageAgentBaseUrl(),
    model: getPageAgentModel(),
    language: 'zh-CN'
  }) as PageAgentInstance
}

export async function openPageAgentPanel() {
  assertPageAgentConfig()
  if (!pageAgent) {
    pageAgent = await createPageAgent()
  }
  pageAgent.panel?.show()
}
