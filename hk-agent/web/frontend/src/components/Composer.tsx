import { useRef, type ChangeEvent, type FormEvent, type KeyboardEvent } from "react";
import { Loader2, Send, Square, Upload } from "lucide-react";

export function Composer({
  value,
  isBusy,
  orderNo,
  onChange,
  onSubmit,
  onCancel,
  onExcelUpload,
}: {
  value: string;
  isBusy: boolean;
  orderNo: string;
  onChange: (value: string) => void;
  onSubmit: () => void;
  onCancel: () => void;
  onExcelUpload: (file: File) => void;
}) {
  const fileRef = useRef<HTMLInputElement | null>(null);

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (isBusy) {
      onCancel();
      return;
    }
    onSubmit();
  }

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key !== "Enter" || event.nativeEvent.isComposing) return;
    if (event.shiftKey || event.ctrlKey || event.metaKey) return;
    event.preventDefault();
    if (!isBusy) onSubmit();
  }

  function handleExcelChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (file && !isBusy) onExcelUpload(file);
  }

  return (
    <form className="composer" onSubmit={handleSubmit}>
      <div className="composer-card">
        <div className="composer-status">
          <span>{orderNo.trim() ? `订单号 ${orderNo}` : "未填订单号，系统将自动生成临时单号"}</span>
          <span>Enter 发送 · Shift/Ctrl/⌘ + Enter 换行</span>
        </div>
        <textarea
          value={value}
          disabled={isBusy}
          onChange={(event) => onChange(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="粘贴订单清单：门幅、每卷长度、包布方式、宽度/高度/数量。"
        />
        <div className="composer-actions">
          <span>模型会先整理预览，确认后才计算。</span>
          <div className="composer-buttons">
            <input
              ref={fileRef}
              type="file"
              accept=".xlsx,.xls"
              hidden
              onChange={handleExcelChange}
            />
            <button
              type="button"
              className="ghost-button upload-button"
              onClick={() => fileRef.current?.click()}
              disabled={isBusy}
            >
              <Upload size={16} />
              上传 Excel
            </button>
            <button className={isBusy ? "stop-button" : "primary send-button"} disabled={!isBusy && !value.trim()}>
              {isBusy ? <Square size={15} /> : <Send size={17} />}
              {isBusy ? "停止" : "发送给 Agent"}
            </button>
          </div>
        </div>
        {isBusy && (
          <div className="composer-running">
            <Loader2 className="spin" size={15} />
            正在流式处理
          </div>
        )}
      </div>
    </form>
  );
}
