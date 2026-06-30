export function sanitizePanelText(text: string) {
  return text
    .replace(/Page navigated to\s*→\s*https?:\/\/[^\s]+/gi, '已进入当前页面')
    .replace(/Page navigated to\s*->\s*https?:\/\/[^\s]+/gi, '已进入当前页面')
    .replace(/Navigated to\s*https?:\/\/[^\s]+/gi, '已进入当前页面')
    .replace(/https?:\/\/127\.0\.0\.1:\d+\/[^\s)）]*/g, '当前系统页面')
    .replace(/https?:\/\/localhost:\d+\/[^\s)）]*/g, '当前系统页面')
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

export function sanitizeTextNodes(root: HTMLElement) {
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT)
  const textNodes: Text[] = []
  while (walker.nextNode()) {
    textNodes.push(walker.currentNode as Text)
  }
  textNodes.forEach((node) => {
    const nextText = sanitizePanelText(node.nodeValue || '')
    if (nextText !== node.nodeValue) node.nodeValue = nextText
  })
}
