import type { PreviewPayload } from "../types";
import { fmt } from "../utils/format";
import { Metric } from "./Metric";

export function PreviewCard({
  preview,
  onConfirm,
}: {
  preview: PreviewPayload;
  onConfirm: () => Promise<void>;
}) {
  const summary = preview.summary;
  return (
    <section className="preview-card">
      <div className="card-head">
        <strong>本次要计算的内容</strong>
        <span>确认后开始计算</span>
      </div>
      <div className="metric-grid">
        <Metric label="订单号" value={summary.order_no || "自动生成"} />
        <Metric label="帘型" value={summary.fabric_type || "-"} />
        <Metric label="订单类型" value={summary.order_type || "-"} />
        <Metric label="门幅" value={summary.materials.join("、") || "-"} />
        <Metric label="裁片行数" value={`${summary.piece_rows || 0} 行`} />
        <Metric label="裁片数量" value={`${summary.piece_quantity || 0} 件`} />
        <Metric label="包布加高" value={`${fmt(summary.height_extra)}m`} />
        <Metric label="裁剪倍率" value={`${summary.height_multiplier || 1}×`} />
        <Metric label="每卷总长" value={`${fmt(summary.roll_total_length)}m`} />
        <Metric label="前端不可用" value={`${fmt(summary.roll_head_waste)}m`} />
        <Metric label="末端不可用" value={`${fmt(summary.roll_tail_waste)}m`} />
      </div>
      <div className="card-actions preview-actions">
        <span>需要改的话，直接在输入框继续补充。</span>
        <button className="primary" type="button" onClick={() => void onConfirm()}>
          确认并计算
        </button>
      </div>
    </section>
  );
}
