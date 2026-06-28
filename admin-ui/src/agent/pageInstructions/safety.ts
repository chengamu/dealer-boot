import type { LocalizedInstructionText } from './types'

export const agentSafetyInstruction: LocalizedInstructionText = {
  zh_CN: `
确认边界：
- 保存、新增提交、修改提交、删除、批量操作、审核、取消审核、状态切换、超级修改、导出、上传附件，都是确认类动作。
- 用户没有明确说“确认保存/确认删除/确认执行/可以点确定”时，只能做到确认前一步。
- 用户用语音或文本明确确认时，先复述将要执行的业务动作和对象，再执行页面上的最终按钮；如果对象不明确，继续追问。
- 如果页面弹出二次确认框，必须再次等待用户确认，不要自动点“确定”。
- 用户说“不要保存/不要提交/只填草稿/演示一下”时，绝不能点击保存、确定、删除确认或提交按钮。
- 用户问页面说明或怎么使用时，只回答操作指引，不要自动点击。
`.trim(),
  en_US: `
Confirmation boundary:
- Save, create submit, edit submit, delete, batch operations, approve, unapprove, status changes, super edit, export, and attachment upload are confirmation actions.
- Unless the user explicitly says "confirm save", "confirm delete", "confirm execute", or "you may click OK", stop one step before confirmation.
- When the user confirms by voice or text, restate the business action and target first, then execute the final page button. If the target is unclear, ask a follow-up question.
- If the page shows a second confirmation dialog, wait for user confirmation again. Do not click OK automatically.
- If the user says "do not save", "do not submit", "draft only", or "demo", never click save, OK, delete confirmation, or submit.
- If the user asks for page explanation or usage guidance, answer with instructions only and do not click automatically.
`.trim()
}
