import { CheckCircle2, Clock3 } from "lucide-react";
import type { ActivityItem, CuttingConfig, Usage, WorkbenchSelection } from "../types";
import { statusLabel } from "../utils/format";

export function ActivityPanel({
  activities,
  usage,
  selection,
  config,
}: {
  activities: ActivityItem[];
  usage: Usage;
  selection: WorkbenchSelection;
  config: CuttingConfig;
}) {
  const product = config.products.find((item) => item.key === selection.product);
  const orderType = config.order_types.find((item) => item.key === selection.orderType);
  return (
    <aside className="activity-panel">
      <h3>Agent Activity</h3>
      <div className="runtime-card">
        <strong>{selection.orderNo || "AUTO 临时单号"}</strong>
        <UsageRow label="产品" value={product?.label || selection.product || "-"} />
        <UsageRow label="订单类型" value={orderType?.label || selection.orderType || "-"} />
        <UsageRow label="倍率" value={`${product?.height_multiplier || 1}×`} />
        <UsageRow label="门幅" value={selection.materials.map((item) => `${item}m`).join(" / ") || "-"} />
      </div>
      <div className="steps">
        {activities.length ? (
          activities.map((step) => <ActivityStep key={step.id} step={step} />)
        ) : (
          <p className="empty-copy">发送清单后，这里会显示模型整理、校验、计算和报告生成过程。</p>
        )}
      </div>
      <div className="usage-card">
        <strong>模型用量</strong>
        <UsageRow label="模型" value={usage.model} />
        <UsageRow label="输入 token" value={String(usage.input)} />
        <UsageRow label="输出 token" value={String(usage.output)} />
        <UsageRow label="报告生成费用" value={usage.cost.toFixed(4)} />
      </div>
    </aside>
  );
}

function ActivityStep({ step }: { step: ActivityItem }) {
  return (
    <div className={`step ${step.status}`}>
      <div className="step-icon">{step.status === "done" ? <CheckCircle2 size={15} /> : <Clock3 size={15} />}</div>
      <div>
        <strong>{step.label}</strong>
        <span>{statusLabel(step.status)}</span>
      </div>
    </div>
  );
}

function UsageRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="usage-row">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
