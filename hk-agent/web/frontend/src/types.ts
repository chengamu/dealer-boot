export type Role = "user" | "assistant";
export type MessageKind = "text" | "preview" | "result" | "error";

export type ActivityStatus = "running" | "done" | "blocked" | "error";

export type ActivityItem = {
  id: string;
  label: string;
  status: ActivityStatus;
};

export type QuickAction = {
  label: string;
  message: string;
};

export type ProductConfig = {
  key: string;
  label: string;
  description?: string;
  height_multiplier?: number;
  default_height_extra?: number;
  allow_rotate?: boolean;
};

export type OrderTypeConfig = {
  key: string;
  label: string;
  description?: string;
};

export type WrapModeConfig = {
  key: string;
  label: string;
  height_extra: number;
};

export type CuttingConfig = {
  order_types: OrderTypeConfig[];
  products: ProductConfig[];
  wrap_modes: WrapModeConfig[];
  defaults: {
    order_type: string;
    product: string;
    materials: number[];
    roll_total_length: number;
    roll_head_waste: number;
    roll_tail_waste: number;
    currency: string;
    llm_input_price_per_1m: number;
    llm_output_price_per_1m: number;
  };
  drawing: {
    default_collapsed: boolean;
    bulk_group_by_spec: boolean;
    screen_segment_length: number;
    print_segment_length: number;
  };
};

export type WorkbenchSelection = {
  orderNo: string;
  product: string;
  orderType: string;
  wrapMode: string;
  materials: number[];
  rollTotalLength: number;
  rollHeadWaste: number;
  rollTailWaste: number;
};

export type CuttingRequest = {
  order_no?: string | null;
  materials: { width: number }[];
  pieces: { id: string; width: number; height: number; quantity: number }[];
  business_rules: Record<string, unknown>;
  order_title: string;
};

export type PreviewPayload = {
  request: CuttingRequest;
  summary: {
    order_no?: string;
    fabric_type: string;
    order_type?: string;
    materials: string[];
    piece_rows: number;
    piece_quantity: number;
    height_extra: number;
    height_multiplier: number;
    roll_total_length: number;
    roll_head_waste: number;
    roll_tail_waste: number;
  };
};

export type CuttingPlan = {
  material_width: number;
  feasible: boolean;
  used_length: number;
  total_material_length: number;
  roll_count: number;
  material_area: number;
  piece_area: number;
  waste_area: number;
  waste_rate: number;
};

export type ResultPayload = {
  message: string;
  report_id: string;
  report_url: string;
  order_no?: string;
  product?: string;
  order_type?: string;
  summary: string;
  llm_cost?: number;
  recommended_material_width: number | null;
  plans: CuttingPlan[];
};

export type ChatMessage = {
  id: string;
  role: Role;
  content: string;
  kind?: MessageKind;
  process?: ActivityItem[];
  preview?: PreviewPayload;
  result?: ResultPayload;
  actions?: QuickAction[];
};

export type Usage = {
  model: string;
  input: number;
  output: number;
  cost: number;
};

export type UsageDelta = {
  model?: string;
  input_tokens?: number;
  output_tokens?: number;
  estimated_cost?: number;
};

export type StoredReport = {
  id: string;
  url: string;
  order_no: string;
  title: string;
  product?: string;
  order_type?: string;
  recommended_material_width?: number;
  used_length?: number;
  total_material_length?: number;
  roll_count?: number;
  waste_rate?: number;
  llm_cost?: number;
  created_at: string;
};

export type ChatStreamEvent =
  | { type: "assistant_delta"; data: { text?: string } }
  | { type: "activity_step"; data: ActivityItem }
  | { type: "clarification"; data: { message?: string; actions?: QuickAction[] } }
  | { type: "preview"; data: PreviewPayload }
  | { type: "result"; data: ResultPayload }
  | { type: "usage"; data: UsageDelta }
  | { type: "error"; data: { message?: string } };
