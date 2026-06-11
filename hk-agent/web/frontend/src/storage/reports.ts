import type { ResultPayload, StoredReport } from "../types";

const REPORT_KEY = "hk-agent-reports";

export function loadReports(): StoredReport[] {
  try {
    return JSON.parse(localStorage.getItem(REPORT_KEY) || "[]");
  } catch {
    return [];
  }
}

export function rememberReport(result: ResultPayload): StoredReport[] {
  const reports = loadReports();
  const next = [
    {
      id: result.report_id,
      url: result.report_url,
      order_no: result.order_no || "-",
      title: result.summary || "布料裁剪工艺指导卡",
      product: result.product,
      order_type: result.order_type,
      recommended_material_width: result.recommended_material_width || undefined,
      llm_cost: result.llm_cost || 0,
      created_at: new Date().toLocaleString(),
    },
    ...reports,
  ].slice(0, 8);
  localStorage.setItem(REPORT_KEY, JSON.stringify(next));
  return next;
}
