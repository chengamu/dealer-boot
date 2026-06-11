import type { CuttingConfig, ResultPayload, StoredReport, WorkbenchSelection } from "../types";

export async function getCuttingConfig(): Promise<CuttingConfig> {
  const response = await fetch("/api/fabric-cutting/config");
  if (!response.ok) throw new Error(`配置加载失败：HTTP ${response.status}`);
  return response.json();
}

export async function saveCuttingConfig(config: CuttingConfig): Promise<CuttingConfig> {
  const response = await fetch("/api/fabric-cutting/config", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(config),
  });
  if (!response.ok) throw new Error(`配置保存失败：HTTP ${response.status}`);
  return response.json();
}

export async function getReports(): Promise<StoredReport[]> {
  const response = await fetch("/api/reports?limit=80");
  if (!response.ok) throw new Error(`报告列表加载失败：HTTP ${response.status}`);
  const data = await response.json();
  return data.reports || [];
}

export async function uploadExcelForCalculation(
  file: File,
  selection: WorkbenchSelection,
  config: CuttingConfig,
): Promise<ResultPayload> {
  const form = new FormData();
  form.append("file", file);
  const wrap = config.wrap_modes.find((item) => item.key === selection.wrapMode);
  const product = config.products.find((item) => item.key === selection.product);
  if (selection.wrapMode) form.append("wrap_mode", selection.wrapMode);
  if (wrap) form.append("height_extra", String(wrap.height_extra));
  if (selection.product) form.append("fabric_type", selection.product);
  if (selection.orderType) form.append("order_type", selection.orderType);
  if (product?.height_multiplier) form.append("height_multiplier", String(product.height_multiplier));
  if (selection.materials.length) form.append("materials", selection.materials.join(","));
  form.append("roll_total_length", String(selection.rollTotalLength));
  form.append("roll_head_waste", String(selection.rollHeadWaste));
  form.append("roll_tail_waste", String(selection.rollTailWaste));

  const response = await fetch("/api/fabric-cutting/calculate-excel", {
    method: "POST",
    body: form,
  });
  if (!response.ok) {
    const detail = await response.text();
    throw new Error(detail || `Excel 解析失败：HTTP ${response.status}`);
  }
  const data = await response.json();
  return {
    message: "Excel 已解析并生成报告。",
    report_id: data.report_id,
    report_url: `/reports/${data.report_id}`,
    order_no: data.request?.order_no,
    product: data.request?.business_rules?.fabric_type,
    order_type: data.request?.business_rules?.order_type,
    summary: data.summary || "已根据 Excel 清单完成用料和损耗计算。",
    llm_cost: data.usage_summary?.estimated_cost || 0,
    recommended_material_width: data.recommended_material_width ?? null,
    plans: data.plans || [],
  };
}
