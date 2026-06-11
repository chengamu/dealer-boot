import { useEffect, useMemo, useState, type ReactNode } from "react";
import {
  Bot,
  FileText,
  History,
  MessageSquarePlus,
  Settings,
  Sparkles,
  Trash2,
} from "lucide-react";
import type { CuttingConfig, StoredReport, WorkbenchSelection } from "../types";
import { templatesForConfig } from "../data/templates";
import { fmt, pct } from "../utils/format";

export function Sidebar({
  config,
  reports,
  selection,
  isBusy,
  isSavingConfig,
  onSelectionChange,
  onConfigSave,
  onNewChat,
  onUseTemplate,
}: {
  config: CuttingConfig;
  reports: StoredReport[];
  selection: WorkbenchSelection;
  isBusy: boolean;
  isSavingConfig: boolean;
  onSelectionChange: (selection: WorkbenchSelection) => void;
  onConfigSave: (config: CuttingConfig) => Promise<CuttingConfig>;
  onNewChat: () => void;
  onUseTemplate: (value: string) => void;
}) {
  const templates = useMemo(() => templatesForConfig(config, selection), [config, selection]);
  const currentProduct = config.products.find((item) => item.key === selection.product);
  const currentWrap = config.wrap_modes.find((item) => item.key === selection.wrapMode);
  const [settingsType, setSettingsType] = useState<"product" | "wrap" | null>(null);
  const [draftConfig, setDraftConfig] = useState(config);

  useEffect(() => {
    setDraftConfig(config);
  }, [config]);

  function patchSelection(patch: Partial<WorkbenchSelection>) {
    onSelectionChange({ ...selection, ...patch });
  }

  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">
          <Bot size={20} />
        </div>
        <div>
          <h1>HK Agent</h1>
          <p>工业裁剪工作台</p>
        </div>
      </div>
      <button className="primary full" onClick={onNewChat} disabled={isBusy}>
        <MessageSquarePlus size={17} /> 新会话
      </button>

      <PanelTitle icon={<Sparkles size={15} />} text="订单参数" />
      <div className="order-panel">
        <label>
          <span>订单号</span>
          <input
            value={selection.orderNo}
            onChange={(event) => patchSelection({ orderNo: event.target.value })}
            placeholder="必填，空则自动生成"
          />
        </label>
        <Segmented
          label="订单类型"
          value={selection.orderType}
          items={config.order_types}
          onChange={(value) => patchSelection({ orderType: value })}
        />
        <Segmented
          label="产品"
          value={selection.product}
          items={config.products}
          onChange={(value) => patchSelection({ product: value })}
          onSettings={() => setSettingsType("product")}
        />
        <Segmented
          label="包布"
          value={selection.wrapMode}
          items={config.wrap_modes}
          onChange={(value) => patchSelection({ wrapMode: value })}
          onSettings={() => setSettingsType("wrap")}
        />
        <div className="rule-strip">
          <span>{currentProduct?.label || "产品"}倍率 {currentProduct?.height_multiplier || 1}×</span>
          <span>{currentWrap?.label || "包布"} +{fmt(currentWrap?.height_extra || 0)}m</span>
        </div>
        <div className="inline-fields">
          <label>
            <span>门幅</span>
            <input
              value={selection.materials.join(",")}
              onChange={(event) =>
                patchSelection({
                  materials: event.target.value
                    .split(/[,\s/，]+/)
                    .map((item) => Number(item))
                    .filter((item) => Number.isFinite(item) && item > 0),
                })
              }
              placeholder="2,3"
            />
          </label>
          <label>
            <span>卷长</span>
            <input
              type="number"
              value={selection.rollTotalLength}
              onChange={(event) => patchSelection({ rollTotalLength: Number(event.target.value) })}
            />
          </label>
        </div>
        <div className="inline-fields">
          <label>
            <span>前端损耗</span>
            <input
              type="number"
              value={selection.rollHeadWaste}
              onChange={(event) => patchSelection({ rollHeadWaste: Number(event.target.value) })}
            />
          </label>
          <label>
            <span>末端损耗</span>
            <input
              type="number"
              value={selection.rollTailWaste}
              onChange={(event) => patchSelection({ rollTailWaste: Number(event.target.value) })}
            />
          </label>
        </div>
      </div>

      <PanelTitle icon={<FileText size={15} />} text="快捷模板" />
      <div className="template-grid">
        {templates.map((template) => (
          <button
            key={template.label}
            className="template-button"
            onClick={() => onUseTemplate(template.value)}
            disabled={isBusy}
          >
            {template.label}
          </button>
        ))}
      </div>

      <PanelTitle icon={<History size={15} />} text="最近报告" />
      <div className="history-list">
        {reports.length ? (
          reports.map((report) => <ReportItem key={report.id} report={report} />)
        ) : (
          <p className="empty-copy">暂无报告</p>
        )}
      </div>
      {settingsType && (
        <ConfigModal
          type={settingsType}
          config={draftConfig}
          isSaving={isSavingConfig}
          onChange={setDraftConfig}
          onClose={() => {
            setDraftConfig(config);
            setSettingsType(null);
          }}
          onSave={async () => {
            const saved = await onConfigSave(draftConfig);
            setDraftConfig(saved);
            setSettingsType(null);
          }}
        />
      )}
    </aside>
  );
}

function Segmented({
  label,
  value,
  items,
  onChange,
  onSettings,
}: {
  label: string;
  value: string;
  items: { key: string; label: string }[];
  onChange: (value: string) => void;
  onSettings?: () => void;
}) {
  return (
    <div className="segmented-field">
      <div className="field-head">
        <span>{label}</span>
        {onSettings && (
          <button type="button" className="mini-icon-button" onClick={onSettings} title={`设置${label}`}>
            <Settings size={13} />
          </button>
        )}
      </div>
      <div className="segmented">
        {items.map((item) => (
          <button
            key={item.key}
            type="button"
            className={item.key === value ? "active" : ""}
            onClick={() => onChange(item.key)}
          >
            {item.label}
          </button>
        ))}
      </div>
    </div>
  );
}

function ConfigModal({
  type,
  config,
  isSaving,
  onChange,
  onClose,
  onSave,
}: {
  type: "product" | "wrap";
  config: CuttingConfig;
  isSaving: boolean;
  onChange: (config: CuttingConfig) => void;
  onClose: () => void;
  onSave: () => Promise<void>;
}) {
  const isProduct = type === "product";
  const title = isProduct ? "产品设置" : "包布设置";

  function addItem() {
    if (isProduct) {
      onChange({
        ...config,
        products: [
          ...config.products,
          {
            key: `product_${Date.now()}`,
            label: "新产品",
            height_multiplier: 1,
          },
        ],
      });
      return;
    }
    onChange({
      ...config,
      wrap_modes: [
        ...config.wrap_modes,
        {
          key: `wrap_${Date.now()}`,
          label: "新包布",
          height_extra: 0,
        },
      ],
    });
  }

  function deleteProduct(index: number) {
    if (config.products.length <= 1) return;
    onChange({ ...config, products: config.products.filter((_, itemIndex) => itemIndex !== index) });
  }

  function deleteWrap(index: number) {
    if (config.wrap_modes.length <= 1) return;
    onChange({ ...config, wrap_modes: config.wrap_modes.filter((_, itemIndex) => itemIndex !== index) });
  }

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section className="config-modal" role="dialog" aria-modal="true" onMouseDown={(event) => event.stopPropagation()}>
        <div className="modal-head">
          <div>
            <strong>{title}</strong>
            <span>{isProduct ? "维护产品名称和裁剪倍率" : "维护包布名称和加高参数"}</span>
          </div>
          <button type="button" className="ghost-button" onClick={addItem}>
            {isProduct ? "新增产品" : "新增包布"}
          </button>
        </div>
        <div className="config-list">
          {isProduct
            ? config.products.map((product, index) => (
                <div className="config-row" key={product.key}>
                  <label>
                    <span>名称</span>
                    <input
                      value={product.label}
                      onChange={(event) =>
                        onChange({
                          ...config,
                          products: config.products.map((item, itemIndex) =>
                            itemIndex === index ? { ...item, label: event.target.value } : item,
                          ),
                        })
                      }
                    />
                  </label>
                  <label>
                    <span>倍率</span>
                    <input
                      type="number"
                      min="0.1"
                      step="0.1"
                      value={product.height_multiplier || 1}
                      onChange={(event) =>
                        onChange({
                          ...config,
                          products: config.products.map((item, itemIndex) =>
                            itemIndex === index
                              ? { ...item, height_multiplier: Number(event.target.value) || 1 }
                              : item,
                          ),
                        })
                      }
                    />
                  </label>
                  <button
                    type="button"
                    className="icon-danger"
                    onClick={() => deleteProduct(index)}
                    disabled={config.products.length <= 1}
                    title="删除"
                  >
                    <Trash2 size={15} />
                  </button>
                </div>
              ))
            : config.wrap_modes.map((wrap, index) => (
                <div className="config-row" key={wrap.key}>
                  <label>
                    <span>名称</span>
                    <input
                      value={wrap.label}
                      onChange={(event) =>
                        onChange({
                          ...config,
                          wrap_modes: config.wrap_modes.map((item, itemIndex) =>
                            itemIndex === index ? { ...item, label: event.target.value } : item,
                          ),
                        })
                      }
                    />
                  </label>
                  <label>
                    <span>加高(m)</span>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={wrap.height_extra}
                      onChange={(event) =>
                        onChange({
                          ...config,
                          wrap_modes: config.wrap_modes.map((item, itemIndex) =>
                            itemIndex === index ? { ...item, height_extra: Number(event.target.value) || 0 } : item,
                          ),
                        })
                      }
                    />
                  </label>
                  <button
                    type="button"
                    className="icon-danger"
                    onClick={() => deleteWrap(index)}
                    disabled={config.wrap_modes.length <= 1}
                    title="删除"
                  >
                    <Trash2 size={15} />
                  </button>
                </div>
              ))}
        </div>
        <div className="modal-actions">
          <button type="button" className="ghost-button" onClick={onClose} disabled={isSaving}>
            取消
          </button>
          <button type="button" className="primary" onClick={() => void onSave()} disabled={isSaving}>
            {isSaving ? "保存中..." : "保存"}
          </button>
        </div>
      </section>
    </div>
  );
}

function ReportItem({ report }: { report: StoredReport }) {
  return (
    <a className="history-item" href={report.url}>
      <div className="history-top">
        <strong>{report.order_no || "-"}</strong>
        <span>{report.created_at}</span>
      </div>
      <div className="history-meta">
        <span>{report.product || "-"}</span>
        <span>{report.order_type || "-"}</span>
        <span>{report.recommended_material_width ? `${report.recommended_material_width}m` : "-"}</span>
      </div>
      <div className="history-metrics">
        <span>用料 {report.used_length ? `${fmt(report.used_length)}m` : "-"}</span>
        <span>损耗 {report.waste_rate ? pct(report.waste_rate) : "-"}</span>
        <span>报告生成费 {fmt(report.llm_cost || 0, 4)}</span>
      </div>
    </a>
  );
}

function PanelTitle({ icon, text }: { icon: ReactNode; text: string }) {
  return (
    <div className="panel-title">
      {icon}
      <span>{text}</span>
    </div>
  );
}
