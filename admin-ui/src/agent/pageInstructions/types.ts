export interface PageAgentPageInstruction {
  routes: string[]
  title: LocalizedInstructionText
  content: LocalizedInstructionText
}

export type PageAgentInstructionLocale = 'zh_CN' | 'en_US'

export type LocalizedInstructionText = Record<PageAgentInstructionLocale, string>
