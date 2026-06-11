import { useEffect, useMemo, useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { PanelRightClose, PanelRightOpen } from "lucide-react";
import type {
  ActivityItem,
  ChatMessage,
  ChatStreamEvent,
  CuttingConfig,
  PreviewPayload,
  Usage,
  WorkbenchSelection,
} from "./types";
import { streamAgentChat } from "./api/chatStream";
import { getCuttingConfig, getReports, saveCuttingConfig, uploadExcelForCalculation } from "./api/fabricCutting";
import { welcomeText } from "./data/templates";
import { Sidebar } from "./components/Sidebar";
import { MessageView } from "./components/MessageView";
import { Composer } from "./components/Composer";
import { ActivityPanel } from "./components/ActivityPanel";
import { useSmartScroll } from "./hooks/useSmartScroll";

export function App() {
  const queryClient = useQueryClient();
  const configQuery = useQuery({ queryKey: ["fabric-config"], queryFn: getCuttingConfig });
  const reportsQuery = useQuery({ queryKey: ["reports"], queryFn: getReports });
  const saveConfigMutation = useMutation({
    mutationFn: saveCuttingConfig,
    onSuccess: (nextConfig) => {
      queryClient.setQueryData(["fabric-config"], nextConfig);
      setSelection((current) => hydrateSelection(current, nextConfig));
    },
  });

  const config = configQuery.data;
  const [messages, setMessages] = useState<ChatMessage[]>(() => [welcomeMessage()]);
  const [input, setInput] = useState("");
  const [contextText, setContextText] = useState("");
  const [isBusy, setIsBusy] = useState(false);
  const [preview, setPreview] = useState<PreviewPayload | null>(null);
  const [activities, setActivities] = useState<ActivityItem[]>([]);
  const [usage, setUsage] = useState<Usage>({ model: "-", input: 0, output: 0, cost: 0 });
  const [showActivity, setShowActivity] = useState(true);
  const [selection, setSelection] = useState<WorkbenchSelection>(() => emptySelection());
  const abortRef = useRef<AbortController | null>(null);
  const threadId = useMemo(() => crypto.randomUUID?.() || String(Date.now()), []);
  const scroll = useSmartScroll(messages);

  useEffect(() => {
    if (!config) return;
    setSelection((current) => hydrateSelection(current, config));
  }, [config]);

  async function sendMessage(text?: string) {
    const typed = (text ?? input).trim();
    if (!typed || isBusy || !config) return;
    const nextContext = contextText ? `${contextText}\n${typed}` : typed;
    setContextText(nextContext);
    setInput("");
    pushMessage({ role: "user", content: typed });
    const assistantId = pushMessage({ role: "assistant", content: "" });
    setIsBusy(true);
    abortRef.current = new AbortController();
    try {
      await streamAgentChat(
        {
          thread_id: threadId,
          action: "submit",
          message: nextContext,
          ...payloadFromSelection(selection),
        },
        (event) => handleStreamEvent(event, assistantId),
        abortRef.current.signal,
      );
    } catch (error) {
      if ((error as Error).name !== "AbortError") {
        markError(assistantId, `调用失败：${error instanceof Error ? error.message : String(error)}`);
      }
    } finally {
      setIsBusy(false);
      abortRef.current = null;
    }
  }

  async function confirmCalculation() {
    if (!preview || isBusy) return;
    pushMessage({ role: "user", content: "确认，开始计算" });
    const assistantId = pushMessage({ role: "assistant", content: "" });
    setIsBusy(true);
    abortRef.current = new AbortController();
    try {
      await streamAgentChat(
        {
          thread_id: threadId,
          action: "confirm",
          confirmed_request: preview.request,
          ...payloadFromSelection(selection),
        },
        (event) => handleStreamEvent(event, assistantId),
        abortRef.current.signal,
      );
    } catch (error) {
      if ((error as Error).name !== "AbortError") {
        markError(assistantId, `计算失败：${error instanceof Error ? error.message : String(error)}`);
      }
    } finally {
      setIsBusy(false);
      abortRef.current = null;
    }
  }

  async function uploadExcel(file: File) {
    if (isBusy || !config) return;
    pushMessage({ role: "user", content: `上传 Excel：${file.name}` });
    const assistantId = pushMessage({ role: "assistant", content: "收到 Excel，我先交给后台解析清单。\n" });
    setIsBusy(true);
    upsertActivity({ id: "excel", label: "上传并解析 Excel", status: "running" });
    try {
      const result = await uploadExcelForCalculation(file, selection, config);
      upsertActivity({ id: "excel", label: "上传并解析 Excel", status: "done" });
      patchMessage(assistantId, {
        kind: "result",
        content: `${result.message}\n`,
        result,
      });
      void queryClient.invalidateQueries({ queryKey: ["reports"] });
    } catch (error) {
      upsertActivity({ id: "excel", label: "上传并解析 Excel", status: "error" });
      markError(assistantId, `Excel 解析失败：${error instanceof Error ? error.message : String(error)}`);
    } finally {
      setIsBusy(false);
    }
  }

  function cancelRun() {
    abortRef.current?.abort();
    setIsBusy(false);
    pushMessage({ role: "assistant", kind: "error", content: "本次运行已停止。" });
  }

  function handleStreamEvent(event: ChatStreamEvent, assistantId: string) {
    if (event.type === "assistant_delta") {
      appendAssistantText(assistantId, event.data.text || "");
    } else if (event.type === "activity_step") {
      upsertActivity(event.data);
      upsertMessageProcess(assistantId, event.data);
    } else if (event.type === "clarification") {
      appendAssistantText(assistantId, `\n${event.data.message || "请补充信息。"}\n`);
      patchMessage(assistantId, { actions: event.data.actions || [] });
    } else if (event.type === "preview") {
      setPreview(event.data);
      patchMessage(assistantId, { kind: "preview", preview: event.data });
    } else if (event.type === "result") {
      patchMessage(assistantId, { kind: "result", result: event.data });
      void queryClient.invalidateQueries({ queryKey: ["reports"] });
    } else if (event.type === "usage") {
      addUsage(event.data);
    } else if (event.type === "error") {
      markError(assistantId, event.data.message || "处理失败");
    }
  }

  function pushMessage(inputMessage: Omit<ChatMessage, "id">) {
    const id = crypto.randomUUID?.() || `${Date.now()}-${Math.random()}`;
    setMessages((current) => [...current, { id, kind: "text", ...inputMessage }]);
    return id;
  }

  function appendAssistantText(id: string, text: string) {
    setMessages((current) =>
      current.map((item) => (item.id === id ? { ...item, content: `${item.content}${text}` } : item)),
    );
  }

  function patchMessage(id: string, patch: Partial<ChatMessage>) {
    setMessages((current) => current.map((item) => (item.id === id ? { ...item, ...patch } : item)));
  }

  function markError(id: string, text: string) {
    patchMessage(id, { kind: "error", content: text });
  }

  function upsertActivity(item: ActivityItem) {
    setActivities((current) => {
      const exists = current.some((step) => step.id === item.id);
      return exists ? current.map((step) => (step.id === item.id ? item : step)) : [...current, item];
    });
  }

  function upsertMessageProcess(messageId: string, item: ActivityItem) {
    setMessages((current) =>
      current.map((message) => {
        if (message.id !== messageId) return message;
        const process = message.process || [];
        const exists = process.some((step) => step.id === item.id);
        const nextProcess = exists ? process.map((step) => (step.id === item.id ? item : step)) : [...process, item];
        return { ...message, process: nextProcess };
      }),
    );
  }

  function addUsage(data: { model?: string; input_tokens?: number; output_tokens?: number; estimated_cost?: number }) {
    if (!Object.keys(data).length) return;
    setUsage((current) => ({
      model: String(data.model || current.model),
      input: current.input + Number(data.input_tokens || 0),
      output: current.output + Number(data.output_tokens || 0),
      cost: current.cost + Number(data.estimated_cost || 0),
    }));
  }

  function newChat() {
    if (isBusy) return;
    setMessages([welcomeMessage()]);
    setInput("");
    setContextText("");
    setPreview(null);
    setActivities([]);
    setUsage({ model: "-", input: 0, output: 0, cost: 0 });
  }

  if (!config) {
    return <div className="loading-screen">正在加载裁剪配置...</div>;
  }

  return (
    <div className={`app-shell ${showActivity ? "" : "activity-collapsed"}`}>
      <Sidebar
        config={config}
        reports={reportsQuery.data || []}
        selection={selection}
        isBusy={isBusy}
        isSavingConfig={saveConfigMutation.isPending}
        onSelectionChange={setSelection}
        onConfigSave={(nextConfig) => saveConfigMutation.mutateAsync(nextConfig)}
        onNewChat={newChat}
        onUseTemplate={setInput}
      />
      <main className="thread">
        <header className="thread-header">
          <div>
            <h2>布料裁剪 Agent 工作台</h2>
            <p>订单号、产品、订单类型和规则模板先选好；模型只负责整理清单，确认后再计算。</p>
          </div>
          <div className="header-actions">
            <span className={`model-chip ${isBusy ? "live" : ""}`}>{isBusy ? "Agent 正在工作" : "Ready"}</span>
            <button className="icon-button" type="button" onClick={() => setShowActivity((value) => !value)}>
              {showActivity ? <PanelRightClose size={18} /> : <PanelRightOpen size={18} />}
            </button>
          </div>
        </header>
        <section className="messages" ref={scroll.ref} aria-live="polite">
          {messages.map((message) => (
            <MessageView
              key={message.id}
              message={message}
              onQuickAction={sendMessage}
              onConfirm={confirmCalculation}
            />
          ))}
          {!scroll.isPinned && (
            <button className="scroll-bottom" type="button" onClick={() => scroll.scrollToBottom()}>
              回到底部
            </button>
          )}
        </section>
        <Composer
          value={input}
          isBusy={isBusy}
          orderNo={selection.orderNo}
          onChange={setInput}
          onSubmit={() => void sendMessage()}
          onCancel={cancelRun}
          onExcelUpload={(file) => void uploadExcel(file)}
        />
      </main>
      {showActivity && <ActivityPanel activities={activities} usage={usage} selection={selection} config={config} />}
    </div>
  );
}

function welcomeMessage(): ChatMessage {
  return {
    id: crypto.randomUUID?.() || String(Date.now()),
    role: "assistant",
    kind: "text",
    content: welcomeText(),
  };
}

function emptySelection(): WorkbenchSelection {
  return {
    orderNo: "",
    product: "",
    orderType: "",
    wrapMode: "",
    materials: [],
    rollTotalLength: 50,
    rollHeadWaste: 5,
    rollTailWaste: 5,
  };
}

function hydrateSelection(current: WorkbenchSelection, config: CuttingConfig): WorkbenchSelection {
  return {
    orderNo: current.orderNo,
    product: current.product || config.defaults.product || config.products[0]?.key || "",
    orderType: current.orderType || config.defaults.order_type || config.order_types[0]?.key || "",
    wrapMode: current.wrapMode || config.wrap_modes[0]?.key || "",
    materials: current.materials.length ? current.materials : config.defaults.materials || [],
    rollTotalLength: current.rollTotalLength || config.defaults.roll_total_length || 50,
    rollHeadWaste: current.rollHeadWaste ?? config.defaults.roll_head_waste ?? 5,
    rollTailWaste: current.rollTailWaste ?? config.defaults.roll_tail_waste ?? 5,
  };
}

function payloadFromSelection(selection: WorkbenchSelection) {
  return {
    order_no: selection.orderNo.trim() || undefined,
    product: selection.product,
    order_type: selection.orderType,
    wrap_mode: selection.wrapMode,
    materials: selection.materials,
    roll_total_length: selection.rollTotalLength,
    roll_head_waste: selection.rollHeadWaste,
    roll_tail_waste: selection.rollTailWaste,
  };
}
