import type { ChatMessage } from "../types";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { PreviewCard } from "./PreviewCard";
import { ResultCard } from "./ResultCard";
import { WorkProcessCard } from "./WorkProcessCard";

export function MessageView({
  message,
  onQuickAction,
  onConfirm,
}: {
  message: ChatMessage;
  onQuickAction: (text: string) => Promise<void>;
  onConfirm: () => Promise<void>;
}) {
  return (
    <article className={`message ${message.role} ${message.kind === "error" ? "error" : ""}`}>
      <div className="role">{message.role === "user" ? "你" : "Agent"}</div>
      <div className="bubble">
        {message.content && (
          <div className="message-text markdown-body">
            <ReactMarkdown remarkPlugins={[remarkGfm]}>{message.content}</ReactMarkdown>
          </div>
        )}
        {!message.content && message.role === "assistant" && !message.process?.length && (
          <div className="thinking-line">
            <span />
            正在连接 Agent
          </div>
        )}
        {!!message.process?.length && <WorkProcessCard items={message.process} />}
        {!!message.actions?.length && (
          <div className="quick-actions">
            {message.actions.map((action) => (
              <button key={action.label} type="button" onClick={() => void onQuickAction(action.message)}>
                {action.label}
              </button>
            ))}
          </div>
        )}
        {message.preview && <PreviewCard preview={message.preview} onConfirm={onConfirm} />}
        {message.result && <ResultCard result={message.result} />}
      </div>
    </article>
  );
}
