import type { ResultPayload } from "../types";
import { fmt, pct } from "../utils/format";
import { Metric } from "./Metric";

export function ResultCard({ result }: { result: ResultPayload }) {
  const recommended =
    result.plans.find((plan) => plan.material_width === result.recommended_material_width) ||
    result.plans.find((plan) => plan.feasible);
  return (
    <section className="result-card">
      <div className="card-head">
        <strong>计算完成</strong>
        <span>{result.recommended_material_width ? `推荐 ${result.recommended_material_width}m` : "无可用方案"}</span>
      </div>
      <div className="metric-grid">
        <Metric label="订单号" value={result.order_no || "-"} />
        <Metric label="产品/订单" value={`${result.product || "-"} / ${result.order_type || "-"}`} />
        <Metric label="用料长度" value={recommended ? `${fmt(recommended.used_length)}m` : "-"} />
        <Metric label="采购总长" value={recommended ? `${fmt(recommended.total_material_length || recommended.used_length)}m` : "-"} />
        <Metric label="卷数" value={recommended ? `${recommended.roll_count || 1}卷` : "-"} />
        <Metric label="总用料面积" value={recommended ? `${fmt(recommended.material_area)}㎡` : "-"} />
        <Metric label="损耗面积" value={recommended ? `${fmt(recommended.waste_area)}㎡` : "-"} />
        <Metric label="损耗率" value={recommended ? pct(recommended.waste_rate) : "-"} />
        <Metric label="报告生成费用" value={fmt(result.llm_cost || 0, 4)} />
      </div>
      <div className="card-actions">
        <a className="report-link" href={result.report_url}>
          打开 H5 工艺指导卡
        </a>
      </div>
    </section>
  );
}
