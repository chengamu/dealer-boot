import type { ChatStreamEvent } from "../types";

export async function streamAgentChat(
  payload: Record<string, unknown>,
  onEvent: (event: ChatStreamEvent) => void,
  signal?: AbortSignal,
) {
  const response = await fetch("/api/agents/fabric_cutting/chat/stream", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
    signal,
  });
  if (!response.ok || !response.body) {
    throw new Error(`HTTP ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";
  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const chunks = buffer.split("\n\n");
    buffer = chunks.pop() || "";
    chunks.map(parseSseEvent).filter(Boolean).forEach((event) => onEvent(event as ChatStreamEvent));
  }
}

function parseSseEvent(raw: string): ChatStreamEvent | null {
  const type = raw.match(/^event:\s*(.+)$/m)?.[1];
  const dataText = raw.match(/^data:\s*(.+)$/m)?.[1] || "{}";
  if (!type) return null;
  if (!["assistant_delta", "activity_step", "clarification", "preview", "result", "usage", "error"].includes(type)) {
    return null;
  }
  return { type, data: JSON.parse(dataText) } as ChatStreamEvent;
}
