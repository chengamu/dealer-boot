import { CheckCircle2, ChevronRight, Clock3 } from "lucide-react";
import type { ActivityItem } from "../types";
import { statusLabel } from "../utils/format";

export function WorkProcessCard({ items }: { items: ActivityItem[] }) {
  if (!items.length) return null;
  return (
    <section className="work-process">
      <div className="work-process-head">
        <span>Work process ({items.length} steps)</span>
        <ChevronRight size={15} />
      </div>
      <div className="work-process-list">
        {items.map((item) => (
          <div className={`work-process-row ${item.status}`} key={item.id}>
            <span className="work-process-icon">
              {item.status === "done" ? <CheckCircle2 size={14} /> : <Clock3 size={14} />}
            </span>
            <span className="work-process-label">{item.label}</span>
            <span className="work-process-status">{statusLabel(item.status)}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
