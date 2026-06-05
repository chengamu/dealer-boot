const DATA = window.CONFIG_CENTER_PROTOTYPE_DATA || { products: [] };
const STORAGE_KEY = "skyspf-config-center-entry-drafts";

const FEATURED_PRODUCT_NAMES = [
  "L Cassette Motor Zebra Shades",
  "Flat rail Motor Screen View Shades",
  "L Cassette Chain Blackout shades",
  "Standard Chain Roman Shades",
  "Solar Panel"
];

const state = {
  productId: DATA.products.find(item => item.name === FEATURED_PRODUCT_NAMES[0])?.id || DATA.products[0]?.id || "",
  questionId: "",
  search: "",
  navCollapsed: localStorage.getItem("skyspf-config-center-nav-collapsed") === "1",
  motorRuleInput: null,
  activeSharedModule: "",
  drafts: loadDrafts()
};

const MOTOR_CAPACITY_PROFILES = [
  {
    code: "BATTERY_25MM_1_1NM",
    label: "25mm 电池电机 / 1.1Nm",
    minTubeMm: 38,
    maxWidthCm: 220,
    maxHeightCm: 260,
    maxLoadKg: 4.5,
    maxFabricGsm: 220
  },
  {
    code: "TUBULAR_35MM_3NM",
    label: "35mm 管状电机 / 3Nm",
    minTubeMm: 40,
    maxWidthCm: 280,
    maxHeightCm: 320,
    maxLoadKg: 12,
    maxFabricGsm: 450
  },
  {
    code: "TUBULAR_45MM_6NM",
    label: "45mm 高扭矩电机 / 6Nm",
    minTubeMm: 50,
    maxWidthCm: 380,
    maxHeightCm: 420,
    maxLoadKg: 24,
    maxFabricGsm: 650
  }
];

const SLOT_CN = dict([
  ["Blackout Shades Fabric Type", "遮光卷帘面料"],
  ["Bottom Rail", "底杆"],
  ["Bottom Rail Color", "底杆颜色"],
  ["Cassette Color", "罩壳颜色"],
  ["Cassette Fabric Covering", "罩壳是否包布"],
  ["Control/Motor Direction", "控制位置/操作侧"],
  ["Fabric Roll Type", "卷布方向"],
  ["Model Configuration(Chain Operated)", "拉珠控制方案"],
  ["Model Configuration(Lithium Battery Motor)", "锂电池电机方案"],
  ["Mount Position", "安装方式"],
  ["Roman Shades Fabric Type", "罗马帘面料"],
  ["Screen View Shades Fabric Type", "景观帘面料"],
  ["Shape Side Channels", "侧轨形状"],
  ["Solar Panel", "太阳能板"],
  ["Telescoping pole", "伸缩杆"],
  ["Zebra Shades Fabric Type", "柔纱帘面料"]
]);

const TERM_CN = dict([
  ["APP+Remote(Bluetooth Motor)", "APP + 遥控器（蓝牙电机）"],
  ["APP+Voice+Remote(Zigbee)", "APP + 语音 + 遥控器（Zigbee）"],
  ["APP+Voice+Remote(Matter Motor:Special Speaker Mode)", "APP + 语音 + 遥控器（Matter 电机：特殊音箱模式）"],
  ["Chain Operated(Beige plastic)", "拉珠控制（米色塑料）"],
  ["Chain Operated(Black plastic)", "拉珠控制（黑色塑料）"],
  ["Chain Operated(Brown plastic)", "拉珠控制（棕色塑料）"],
  ["Chain Operated(Grey plastic)", "拉珠控制（灰色塑料）"],
  ["Chain Operated(Stainless steel)", "拉珠控制（不锈钢）"],
  ["Chain Operated(White plastic)", "拉珠控制（白色塑料）"],
  ["Fabric wrap", "包布"],
  ["Farbric wrap", "包布"],
  ["No Fabric wrap", "不包布"],
  ["No fabric warp", "不包布"],
  ["Inside", "内装"],
  ["Outside", "外装"],
  ["Left", "左侧"],
  ["Right", "右侧"],
  ["L shape side channel", "L 型侧轨"],
  ["U shape side channel", "U 型侧轨"],
  ["Night Black", "夜黑"],
  ["Pearl White", "珍珠白"],
  ["Iron Gray", "铁灰"],
  ["Sand Beige", "沙米色"],
  ["Remote Control Only(Remote Motor)", "仅遥控器（遥控电机）"],
  ["Reverse", "反卷"],
  ["Solar Panel", "加太阳能板"],
  ["No Solar Panel", "不加太阳能板"],
  ["Standard", "标准卷"],
  ["Telescoping pole", "加伸缩杆"],
  ["No Telescoping pole", "不加伸缩杆"],
  ["White", "白色"],
  ["Beige", "米色"],
  ["Light Grey", "浅灰色"],
  ["Dark Gray", "深灰色"],
  ["Brown", "棕色"]
]);

const COMPONENT_CN = dict([
  ["Bluetooth Motor", "蓝牙电机"],
  ["Remote Motor", "遥控电机"],
  ["Matter Motor:Special Speaker Mode", "Matter 电机：特殊音箱模式"],
  ["Zigbee", "Zigbee 电机"],
  ["Solar Panel", "太阳能板"],
  ["Telescoping pole", "伸缩杆"],
  ["Cassette Fabric Covering", "罩壳包布"],
  ["Farbric wrap", "包布"]
]);

const QUESTION_GROUP_OPTIONS = ["公共问题组", "销售变体专属问题组", "产品/结构专属问题组", "组件/配件问题组", "生产/包装问题组", "提示/备注问题组"];
const APPLY_LEVEL_OPTIONS = [["PRODUCT_MODEL", "产品模型"], ["SALES_VARIANT", "销售变体"], ["CONFIG_TEMPLATE", "配置模板"], ["COMPONENT_OPTION", "答案/组件绑定"]];

function $(id) {
  return document.getElementById(id);
}

function esc(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function clean(value) {
  return String(value ?? "").replace(/\s+/g, " ").trim();
}

function key(value) {
  return clean(value)
    .replaceAll("（", "(")
    .replaceAll("）", ")")
    .replaceAll("：", ":")
    .replace(/\s*\(\s*/g, "(")
    .replace(/\s*\)\s*/g, ")")
    .replace(/\s*--\s*/g, "--")
    .toLowerCase();
}

function dict(items) {
  return Object.fromEntries(items.map(([en, cn]) => [key(en), cn]));
}

function slug(value, fallback = "ITEM") {
  const code = clean(value).toUpperCase().replace(/[^A-Z0-9]+/g, "_").replace(/^_|_$/g, "");
  return code || fallback;
}

function translate(value, dictionary = {}) {
  const text = clean(value);
  if (!text) return "";
  const direct = dictionary[key(text)] || TERM_CN[key(text)];
  if (direct) return direct;
  const coded = text.match(/^(.+?)--\s*(.+)$/);
  if (coded) {
    const code = clean(coded[1]);
    const name = clean(coded[2]);
    const translated = TERM_CN[key(name)];
    return translated ? `${code} - ${translated}` : "";
  }
  return "";
}

function label(value, dictionary = {}) {
  const en = clean(value);
  const cn = translate(en, dictionary);
  return {
    cn: cn && cn !== en ? cn : "",
    en
  };
}

function bilingual(value, dictionary = {}) {
  const parts = label(value, dictionary);
  if (!parts.en) return "";
  return parts.cn ? `${parts.cn} / ${parts.en}` : parts.en;
}

function optionLabel(option) {
  const name = clean(option?.name || "");
  const description = clean(option?.description || "");
  if (description && description !== name && description.includes("--")) {
    return label(description, TERM_CN);
  }
  return label(name || description, TERM_CN);
}

function loadDrafts() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {};
  } catch {
    return {};
  }
}

function saveDrafts() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state.drafts));
  updateSavedState();
}

function product() {
  return DATA.products.find(item => item.id === state.productId) || DATA.products[0];
}

function productOptionsCount(p) {
  return p.slots.reduce((sum, slot) => sum + slot.options.length, 0);
}

function slotsForProduct(p = product()) {
  const slots = [...p.slots];
  const savedQuestions = state.drafts[p.id]?.questions || {};
  Object.entries(savedQuestions).forEach(([questionId, draft]) => {
    if (!questionId.startsWith("NEW_Q_") || slots.some(slot => slot.itemId === questionId)) return;
    slots.push({
      itemId: questionId,
      name: draft.enName || draft.cnName || "New Config Question",
      type: draft.inputType === "multi_select" ? "MULTI" : "SINGLE",
      mandatory: draft.required ? "Y" : "N",
      sequence: String(draft.sortOrder || slots.length * 10 + 10),
      options: []
    });
  });
  return slots;
}

function draftOptionsCount(p = product()) {
  return slotsForProduct(p).reduce((sum, slot, index) => sum + questionDraft(slot, index, p).options.length, 0);
}

function productScore(p) {
  return productOptionsCount(p) + p.slots.length * 10 + p.componentMappings.length + (p.priceSamples.length ? 20 : 0);
}

function productsForList() {
  const ranked = [...DATA.products].sort((a, b) => productScore(b) - productScore(a));
  const list = [...FEATURED_PRODUCT_NAMES.map(name => DATA.products.find(item => item.name === name)), ...ranked];
  const seen = new Set();
  const term = state.search.toLowerCase();
  return list.filter(item => {
    if (!item || seen.has(item.id)) return false;
    seen.add(item.id);
    const text = [item.id, item.name, item.kind, item.control].join(" ").toLowerCase();
    return !term || text.includes(term);
  });
}

function controlMethodCode(p = product()) {
  const text = `${p.control || ""} ${p.name || ""} ${p.controlCategoryName || ""}`.toLowerCase();
  if (/chain|手拉|拉珠/.test(text)) return "chain";
  if (/motor|电动|battery|remote|zigbee|matter/.test(text)) return "motor";
  if (/standard|标准|solar|panel/.test(text)) return "standard";
  return "custom";
}

function currentProductDraft(p = product()) {
  const saved = state.drafts[p.id] || {};
  const method = controlMethodCode(p);
  return {
    seriesCode: saved.seriesCode || slug(p.kind, `SERIES_${p.id}`),
    seriesName: saved.seriesName || p.kind,
    modelCode: saved.modelCode || `MODEL_${slug(p.kind)}_${slug(p.valanceCategoryName || "BASE")}`,
    modelName: saved.modelName || `${p.kind} / ${p.valanceCategoryName || "通用结构"}`,
    productLine: saved.productLine || p.priceCategoryName || p.kind,
    valanceType: saved.valanceType || p.valanceCategoryName || "按产品资料",
    variantCode: saved.variantCode || `VAR_${p.id}_${method.toUpperCase()}`,
    variantName: saved.variantName || `${p.control || "标准"}销售变体`,
    variantDimension: saved.variantDimension || `control_method = ${method}`,
    controlSystem: saved.controlSystem || p.controlCategoryName || p.control,
    productCode: saved.productCode || `P_${p.id}`,
    productName: saved.productName || p.name,
    saleMode: saved.saleMode || (p.kind === "标准配件" ? "STANDARD_SKU" : "CUSTOM_CONFIGURED"),
    productNature: saved.productNature || (p.kind === "标准配件" ? "可售成品" : "定制成品/按单生产"),
    stockPolicy: saved.stockPolicy || (p.kind === "标准配件" ? "成品库存" : "不预生产成品，按订单生成 BOM"),
    templateSource: saved.templateSource || "GENERATED_FROM_PRODUCT",
    templateCode: saved.templateCode || `TPL_${p.id}_2026_06_DRAFT`,
    status: saved.status || "DRAFT",
    displayUnit: saved.displayUnit || "inch",
    engineUnit: saved.engineUnit || "cm",
    owner: saved.owner || "产品运营"
  };
}

function questionDraft(slot, index, p = product()) {
  const savedQuestion = state.drafts[p.id]?.questions?.[slot.itemId] || {};
  return {
    sourceId: slot.itemId,
    ordinal: savedQuestion.ordinal || index + 1,
    sortOrder: savedQuestion.sortOrder ?? Number(slot.sequence || (index + 1) * 10),
    questionCode: savedQuestion.questionCode || `Q_${slot.itemId}`,
    cnName: savedQuestion.cnName ?? label(slot.name, SLOT_CN).cn,
    enName: savedQuestion.enName ?? clean(slot.name),
    inputType: savedQuestion.inputType || (slot.type === "SINGLE" ? "single_select" : clean(slot.type || "single_select").toLowerCase()),
    required: savedQuestion.required ?? slot.mandatory === "Y",
    group: savedQuestion.group || groupForSlot(slot),
    questionCategory: savedQuestion.questionCategory || questionCategoryForSlot(slot),
    applyLevel: savedQuestion.applyLevel || applyLevelForSlot(slot),
    applyDimension: savedQuestion.applyDimension || applyDimensionForSlot(slot, p),
    purpose: savedQuestion.purpose || purposeForSlot(slot),
    options: optionDrafts(slot, savedQuestion)
  };
}

function optionDrafts(slot, savedQuestion = {}) {
  const savedOptions = savedQuestion.options || {};
  const base = slot.options.map((option, index) => {
    const saved = savedOptions[option.optionId] || {};
    const parts = optionLabel(option);
    return {
      sourceId: option.optionId,
      ordinal: saved.ordinal || index + 1,
      sortOrder: saved.sortOrder ?? Number(option.sequence || (index + 1) * 10),
      optionCode: saved.optionCode || `A_${slot.itemId}_${option.optionId}`,
      cnName: saved.cnName ?? parts.cn,
      enName: saved.enName ?? parts.en,
      status: saved.status || "ACTIVE",
      componentName: saved.componentName ?? componentNameForOption(slot.itemId, option.optionId),
      isNew: false
    };
  });
  const extra = (savedQuestion.extraOptions || []).map((option, index) => ({
    sourceId: option.sourceId,
    ordinal: option.ordinal || base.length + index + 1,
    sortOrder: option.sortOrder ?? (base.length + index + 1) * 10,
    optionCode: option.optionCode || `A_${slot.itemId}_NEW_${index + 1}`,
    cnName: option.cnName || "新答案",
    enName: option.enName || "New Option",
    status: option.status || "DRAFT",
    componentName: option.componentName || "",
    isNew: true
  }));
  return [...base, ...extra];
}

function componentNameForOption(itemId, optionId) {
  const match = product().componentMappings.find(item => item.itemId === itemId && item.optionId === optionId);
  return match ? bilingual(match.componentName, COMPONENT_CN) : "";
}

function componentType(name, sourceQuestion = "") {
  const text = `${clean(name)} ${clean(sourceQuestion)}`.toLowerCase();
  if (/motor|zigbee|matter|remote/.test(text)) return "电机/控制件";
  if (/fabric|yb|yzb|xl|cream|linen|white|grey|gray|beige|brown/.test(text)) return "面料/颜色";
  if (/rail|cassette|channel|valance|aluminum|profile/.test(text)) return "铝杆/型材/外壳";
  if (/solar|pole|chain|cord/.test(text)) return "配件/辅料";
  return "组件/物料";
}

function inventoryClass(type) {
  if (type === "面料/颜色") return "原材料：布匹/面料";
  if (type === "铝杆/型材/外壳") return "原材料：铝杆/型材";
  if (type === "电机/控制件") return "采购组件：电机/控制件";
  if (type === "配件/辅料") return "采购组件/辅料";
  return "组件物料";
}

function materialRole(type) {
  if (type === "面料/颜色") return "参与库存、采购、用量、成本";
  if (type === "铝杆/型材/外壳") return "参与库存、切割、用量、成本";
  if (type === "电机/控制件") return "参与采购、库存、BOM、成本";
  if (type === "配件/辅料") return "参与采购、库存或随单领料";
  return "参与 BOM 或资料映射";
}

function componentPriceMethod(type) {
  if (type === "面料/颜色") return "用量价：面料面积 x 面料单价";
  if (type === "铝杆/型材/外壳") return "用量价：长度 x 型材单价";
  if (type === "电机/控制件") return "固定件价：按数量";
  if (type === "配件/辅料") return "固定件价或长度价";
  return "按组件规则";
}

function componentUsageModel(type) {
  if (type === "面料/颜色") return "FABRIC_AREA";
  if (type === "铝杆/型材/外壳") return "PROFILE_LENGTH";
  if (type === "电机/控制件") return "MOTOR_QTY";
  if (type === "配件/辅料") return "FIXED / BY_HEIGHT";
  return "FIXED";
}

function baseComponentCatalog(p = product()) {
  const map = new Map();
  for (const item of p.componentMappings) {
    const rawName = clean(item.componentName);
    if (!rawName) continue;
    const code = `C_${item.componentProductId || slug(rawName)}`;
    const existing = map.get(code);
    const type = componentType(rawName, item.itemName);
    if (existing) {
      existing.bindCount += 1;
      if (!existing.optionSamples.includes(item.optionName) && existing.optionSamples.length < 3) {
        existing.optionSamples.push(item.optionName);
      }
      continue;
    }
    const parts = label(rawName, COMPONENT_CN);
    map.set(code, {
      sourceId: code,
      componentCode: code,
      cnName: parts.cn,
      enName: parts.en,
      componentType: type,
      source: "从真实答案绑定抽取",
      priceMethod: componentPriceMethod(type),
      usageModel: componentUsageModel(type),
      status: "ACTIVE",
      inventoryClass: inventoryClass(type),
      materialRole: materialRole(type),
      bindCount: 1,
      optionSamples: [item.optionName].filter(Boolean)
    });
  }
  return [...map.values()].sort((a, b) => b.bindCount - a.bindCount);
}

function componentCatalog(p = product()) {
  const saved = state.drafts[p.id]?.components || {};
  return baseComponentCatalog(p).map(item => ({ ...item, ...(saved[item.sourceId] || {}) }));
}

const COLOR_SWATCHES = {
  white: "#f7f8f8",
  beige: "#d9c7aa",
  cream: "#efe2cc",
  linen: "#d8c6a7",
  grey: "#9ca3af",
  gray: "#8b949e",
  black: "#1f2933",
  brown: "#7a4a2a",
  kaki: "#a39174",
  golden: "#9a6a2f",
  pearl: "#f3f0e8",
  iron: "#6f7478",
  sand: "#c8b28d"
};

function swatchForText(value) {
  const text = clean(value).toLowerCase();
  const hit = Object.keys(COLOR_SWATCHES).find(name => text.includes(name));
  return COLOR_SWATCHES[hit] || "#dce8e4";
}

function fileSafe(value) {
  return slug(value, "ASSET").replace(/_+/g, "_");
}

function mediaKindForAsset(asset) {
  if (asset.assetType.includes("颜色") || asset.assetType.includes("面料")) return "swatch";
  if (asset.assetType.includes("组件")) return "component";
  if (asset.assetType.includes("主图")) return "product";
  return "guide";
}

function mediaCoverage(p = product()) {
  const assets = mediaAssets(p);
  const matched = assets.filter(item => item.status === "已匹配").length;
  const pending = assets.filter(item => item.status !== "已匹配").length;
  const byType = assets.reduce((acc, item) => {
    acc[item.assetType] = (acc[item.assetType] || 0) + 1;
    return acc;
  }, {});
  return { assets, matched, pending, byType };
}

function mediaAssets(p = product()) {
  const saved = state.drafts[p.id]?.mediaAssets || {};
  const rows = [];
  const add = asset => rows.push({ ...asset, ...(saved[asset.assetId] || {}) });
  const current = currentProductDraft(p);

  add({
    assetId: `MEDIA_PRODUCT_${p.id}`,
    assetType: "产品主图",
    bindTarget: current.productName,
    bindCode: current.productCode,
    fileName: `${current.productCode}_MAIN.jpg`,
    previewName: "产品主图",
    status: "待上传",
    usage: "报价选品、销售只读总览、客户确认"
  });

  const colorOptions = slotsForProduct(p)
    .filter(slot => /fabric|color|material|rail|cassette|bottom/i.test(slot.name))
    .flatMap(slot => slot.options.map(option => ({ slot, option })));
  colorOptions.slice(0, 8).forEach(({ slot, option }, index) => {
    const parts = optionLabel(option);
    const en = parts.en || option.name || option.description || `Color ${index + 1}`;
    const cn = parts.cn || "";
    const code = en.match(/^([A-Z]{1,5}\d{2,8})/i)?.[1] || `CLR_${p.id}_${index + 1}`;
    add({
      assetId: `MEDIA_COLOR_${slot.itemId}_${option.optionId}`,
      assetType: /fabric|material/i.test(slot.name) ? "面料/颜色图" : "结构颜色图",
      bindTarget: cn ? `${cn} / ${en}` : en,
      bindCode: code,
      fileName: index < 4 ? `${code}_${fileSafe(en)}.jpg` : "",
      previewName: en,
      status: index < 4 ? "已匹配" : "缺图片",
      usage: "客户选择颜色/面料时展示，报价快照留档"
    });
  });

  componentCatalog(p).slice(0, 6).forEach((component, index) => {
    add({
      assetId: `MEDIA_COMPONENT_${component.sourceId}`,
      assetType: "组件图片",
      bindTarget: component.cnName ? `${component.cnName} / ${component.enName}` : component.enName,
      bindCode: component.componentCode,
      fileName: index % 3 === 1 ? "" : `${component.componentCode}_PHOTO.jpg`,
      previewName: component.enName || component.cnName,
      status: index % 3 === 1 ? "缺图片" : "已匹配",
      usage: "组件库、BOM 预览、销售解释配件用途"
    });
  });

  [
    ["MEDIA_GUIDE_MEASURE", "测量说明", "MEASURE_INSIDE_OUTSIDE.pdf", "宽高问题帮助和销售培训"],
    ["MEDIA_GUIDE_INSTALL", "安装说明", `${current.productCode}_INSTALL.pdf`, "报价确认、交付、售后复用"],
    ["MEDIA_GUIDE_PACKAGE", "包装/运输图", `${current.productCode}_PACKAGE.jpg`, "运费复核和生产包装"]
  ].forEach(([assetId, assetType, fileName, usage]) => add({
    assetId,
    assetType,
    bindTarget: current.productName,
    bindCode: current.productCode,
    fileName,
    previewName: assetType,
    status: assetType === "包装/运输图" ? "待上传" : "已匹配",
    usage
  }));

  return rows;
}

function customerFeedbackItems() {
  return [
    {
      category: "Hub 说明提示",
      feedback: "电动产品缺少 Hub 提示和说明",
      route: "公共资料",
      target: "组件资料 / 下单帮助",
      action: "补 Hub 用途、场景、图片和客户提示"
    },
    {
      category: "遥控器系统优化",
      feedback: "遥控器型号、频道、价格、是否包含等信息不清晰",
      route: "组合处理",
      target: "组件资料 + 价格中心 + 下单选项",
      action: "组件页说明用途，价格中心维护价格，下单页选择是否随单"
    },
    {
      category: "太阳能板方案",
      feedback: "蜂巢帘充电口位置与卷帘不同，需要明确连接和走线",
      route: "公共资料",
      target: "安装学习资料 / 组件资料",
      action: "补安装照片、视频、接线示意图"
    },
    {
      category: "Room Name 管理",
      feedback: "仅靠尺寸识别产品，容易混淆",
      route: "订单流程",
      target: "报价/订单行字段",
      action: "Room Name 必填，不进入产品配置资料"
    },
    {
      category: "Line Item 编号",
      feedback: "缺少统一固定编号系统",
      route: "订单流程",
      target: "订单/生产/售后追踪",
      action: "订单行固定编号，不作为配置问题"
    },
    {
      category: "遥控器分组及编程",
      feedback: "多个窗帘需要分组控制和预编程",
      route: "订单组合",
      target: "订单备注 / 编程选项",
      action: "按订单行组合处理，不改变配置中心结构"
    }
  ];
}

function mediaRequirementRows(p = product()) {
  const questionCount = slotsForProduct(p).length;
  const optionCount = draftOptionsCount(p);
  const componentCount = componentCatalog(p).length;
  return [
    {
      objectType: "产品 / 系列",
      target: p.name,
      required: "产品主图、产品说明、适用场景、客户说明、内部备注",
      visible: "销售 / 客户 / 内部",
      check: "缺主图或客户说明时发布前提醒"
    },
    {
      objectType: "配置问题",
      target: `${questionCount} 个问题`,
      required: "问题解释、填写提示、测量/安装帮助、必填原因",
      visible: "销售 / 客户",
      check: "客户可见问题缺帮助说明时 WARNING"
    },
    {
      objectType: "配置答案",
      target: `${optionCount} 个答案`,
      required: "答案说明、选中效果、色卡/面料图、限制原因",
      visible: "销售 / 客户 / 快照",
      check: "颜色/面料答案缺图时 WARNING 或 BLOCKER"
    },
    {
      objectType: "组件 / 物料",
      target: `${componentCount} 个组件`,
      required: "图片、规格图、用途说明、安装方式、是否包含、是否收费",
      visible: "销售 / 内部 / 生产",
      check: "被答案绑定的组件必须有用途说明"
    },
    {
      objectType: "动态规则",
      target: `${dynamicRules(p).length} 条规则`,
      required: "触发原因、客户提示、内部处理说明、生产注意事项",
      visible: "销售 / 内部 / 生产",
      check: "BLOCKER 规则必须有客户提示和内部原因"
    },
    {
      objectType: "标准品",
      target: "遥控器 / 太阳能板 / 色卡 / 样品",
      required: "主图、规格、适用产品、安装/使用说明",
      visible: "销售 / 客户",
      check: "可售标准品缺图片说明时不可发布"
    }
  ];
}

function componentOptions(p = product()) {
  return componentCatalog(p).map(item => bilingual(item.cnName || item.enName, COMPONENT_CN) || item.cnName || item.enName);
}

function defaultDynamicRules(p = product()) {
  const hasMotor = p.control === "电动" || p.componentMappings.some(item => /motor|zigbee|matter/i.test(item.componentName));
  const maxWidth = p.priceSummary?.maxWidth ? `${p.priceSummary.maxWidth} inch` : "按产品资料";
  return [
    {
      id: "AUTO_DUAL_MOTOR_200CM",
      ruleType: "自动计算",
      condition: "width_cm > 200",
      action: hasMotor ? "MOTOR_QTY = 2，并在动态物料清单里输出两个电机" : "不适用：当前不是电动产品",
      target: "电机数量 / MOTOR_QTY",
      severity: hasMotor ? "WARNING" : "INFO",
      enabled: hasMotor ? "Y" : "N",
      note: "2m = 200cm，约 78.74 inch；阈值可以按产品资料调整。"
    },
    {
      id: "VISIBILITY_BY_CONTROL",
      ruleType: "显示/隐藏",
      condition: "variant.dimension.control_method = motor / chain / standard",
      action: "只显示当前销售变体维度相关的问题；隐藏无关的电机、链条、太阳能板问题",
      target: "ConfigQuestion.visible",
      severity: "INFO",
      enabled: "Y",
      note: "例如手拉产品不显示电机方案，标准品只显示轻量规格。"
    },
    {
      id: "OPTION_SCOPE_BY_PRODUCT",
      ruleType: "选项域",
      condition: "series + product_model + variant_dimension + fabric group",
      action: "筛出当前产品可用答案，过滤其他系列/变体维度的答案",
      target: "ConfigOption.scope",
      severity: "INFO",
      enabled: "Y",
      note: "当前抽样里同样是面料问题，不同产品答案数从几十到 80 个不等。"
    },
    {
      id: "DEFAULT_VALUE_RULE",
      ruleType: "默认值",
      condition: "question first shown and no user value",
      action: "按产品资料设置默认答案，例如默认卷布方向、默认安装方式或默认不加配件",
      target: "ConfigSelection.defaultValue",
      severity: "INFO",
      enabled: "Y",
      note: "默认值可以加快录入，但必须允许运营维护，不写死在代码里。"
    },
    {
      id: "DISABLE_OPTIONS_OVER_WIDTH",
      ruleType: "禁用选项",
      condition: "width_cm > 200",
      action: "禁用不支持超宽的选配答案，例如部分侧轨、安装方式、特殊配件",
      target: "ConfigOption.enabled = false",
      severity: "BLOCKER",
      enabled: "Y",
      note: "用于“超宽不支持选配”。不是删除答案，而是在当前宽度下禁用。"
    },
    {
      id: "FORBID_INCOMPATIBLE_OPTIONS",
      ruleType: "禁止组合",
      condition: "selected options conflict by product rule",
      action: "提示冲突原因，并阻止两个答案同时发布/提交",
      target: "CompatibilityRule",
      severity: "BLOCKER",
      enabled: "Y",
      note: "比如某些面料不能配某些罩壳/侧轨/安装方式，规则要说明原因。"
    },
    {
      id: "DIMENSION_LIMIT",
      ruleType: "尺寸限制",
      condition: `width/height 超过 ${maxWidth}`,
      action: "禁止提交，或改成仅提醒由负责人复核",
      target: "ValidationMessage",
      severity: "BLOCKER",
      enabled: "Y",
      note: "尺寸限制单独维护，不能只藏在价格矩阵里。"
    },
    {
      id: "AUTO_ADD_SOLAR_PANEL",
      ruleType: "自动带组件",
      condition: "Solar Panel = Yes",
      action: "动态物料清单加入 Solar Panel x 1",
      target: "ComponentBinding",
      severity: p.slots.some(slot => /solar panel/i.test(slot.name)) ? "INFO" : "INFO",
      enabled: p.slots.some(slot => /solar panel/i.test(slot.name)) ? "Y" : "N",
      note: "示例：客户选太阳能板，生产/采购自动看到组件。"
    },
    {
      id: "MEASURE_PRECISION",
      ruleType: "提醒",
      condition: "dimension input",
      action: "宽高按 1/8 inch 精度录入，并换算为 cm 给内部引擎",
      target: "MeasurementRule",
      severity: "WARNING",
      enabled: "Y",
      note: "避免宽高单位和精度混乱。"
    },
    {
      id: "MOUNT_DEDUCTION",
      ruleType: "自动计算",
      condition: "Mount Position = Inside / Outside",
      action: "计算成品整体宽、布料宽、型材长度的扣减/加量",
      target: "DeductionRule",
      severity: "INFO",
      enabled: p.slots.some(slot => /mount position/i.test(slot.name)) ? "Y" : "N",
      note: "Inside/Outside mount 会影响生产尺寸，不等于客户输入宽度。"
    },
    {
      id: "DERIVE_PRODUCTION_DIMENSIONS",
      ruleType: "派生值",
      condition: "width_cm + height_cm + mount + cassette/channel selected",
      action: "输出 ORDER_WIDTH、FINISHED_WIDTH、FABRIC_WIDTH、PROFILE_LENGTH",
      target: "DerivedDimension",
      severity: "INFO",
      enabled: "Y",
      note: "后面报价、BOM、裁剪和生产不能各算各的尺寸。"
    },
    {
      id: "FABRIC_WIDTH_OUTPUT",
      ruleType: "自动计算",
      condition: "fabric option selected + dimensions entered",
      action: "输出 FABRIC_WIDTH / FABRIC_AREA，给面料用量和成本",
      target: "UsageModel.FABRIC_AREA",
      severity: "INFO",
      enabled: p.slots.some(slot => /fabric|material/i.test(slot.name)) ? "Y" : "N",
      note: "订单宽、成品宽、布宽可能不同。"
    },
    {
      id: "MOTOR_CAPACITY_BY_FABRIC",
      ruleType: "禁用选项",
      condition: "fabric_weight + width_cm + height_cm exceeds motor capacity",
      action: "禁用当前电机方案，或要求更高扭矩电机/双电机",
      target: "MotorCapacityRule",
      severity: "BLOCKER",
      enabled: hasMotor ? "Y" : "N",
      note: "电机能力不只看宽度，还要看面料重量、管径和高度。"
    },
    {
      id: "STANDARD_SKU_AVAILABILITY",
      ruleType: "提醒",
      condition: "standard SKU selected or auto-added",
      action: "检查标准品库存/适用范围；缺货时提醒或禁止自动带出",
      target: "StandardSkuRule",
      severity: "WARNING",
      enabled: p.kind === "标准配件" || p.componentMappings.some(item => /solar panel|remote|motor/i.test(item.componentName)) ? "Y" : "N",
      note: "标准品既能单独卖，也能作为组件被引用。"
    },
    {
      id: "OPTION_PRICE_ADJUSTMENT",
      ruleType: "价格调整",
      condition: "option selected and option has surcharge/fixed component price",
      action: "在矩阵基础价之外追加选配价、固定件价或用量件价",
      target: "PriceResult.adjustments",
      severity: "INFO",
      enabled: p.priceSamples.length ? "Y" : "N",
      note: "当前数据有矩阵价，后期还要支持电机、太阳能板、服务项等加价。"
    },
    {
      id: "PUBLISH_COMPLETENESS_CHECK",
      ruleType: "发布检查",
      condition: "template status -> PUBLISHED",
      action: "检查必填问题、答案中英名、组件编码、价格/尺寸/规则测试是否完整",
      target: "PublishTestResult",
      severity: "BLOCKER",
      enabled: "Y",
      note: "上线前拦截漏配置，避免下单后才发现不能报价或不能生产。"
    }
  ];
}

function dynamicRules(p = product()) {
  const saved = state.drafts[p.id]?.dynamicRules || {};
  return defaultDynamicRules(p).map(rule => ({ ...rule, ...(saved[rule.id] || {}) }));
}

function ruleAuditRows(p = product()) {
  return [
    ["必填规则", "数据库抽样", `${p.slots.filter(slot => slot.mandatory === "Y").length} 个必填问题`, "字段显示时必须填写，不填不能发布/提交"],
    ["显示/隐藏规则", "HD/HB 配置器范式 + 销售变体维度", "电动、手拉、标准品不应看到同一套问题", "按产品模型/销售变体/已选答案控制问题可见性"],
    ["选项域规则", "数据库抽样", "面料类问题最多 80 个答案；不同产品选项数量不同", "按产品、系列、销售变体维度、面料组筛选答案"],
    ["默认值规则", "配置中心要求", "常见默认不加配件、默认卷布方向、默认安装方式", "自动填默认值，但允许运营修改"],
    ["禁止组合规则", "综合规则", "某些面料、结构、安装、配件不能同时出现", "命中时提示冲突原因并 BLOCKER"],
    ["答案到组件绑定", "数据库抽样", `${p.componentMappings.length} 条绑定，含自动配件和选项映射`, "选择答案后生成组件或物料引用"],
    ["价格矩阵规则", "数据库抽样", p.priceSamples.length ? `${p.priceSamples.length} 个价格样本` : "当前样本价格待补", "按产品线/系列/销售变体维度/宽高计算价格"],
    ["选配加价规则", "数据库抽样 + 后期报价", "矩阵价之外还会有电机、太阳能板、服务、辅料加价", "输出价格明细，不把所有价格都塞进矩阵"],
    ["尺寸限制规则", "数据库抽样", `${p.dimensionLimits.length} 条尺寸限制`, "超过最大宽高时提醒或拦截"],
    ["安装方式扣减", "外部行业资料", "Inside/Outside mount 会影响成品宽、布宽、型材长度", "由 Mount Position 触发 Deduction/Addition 规则"],
    ["测量精度规则", "外部行业资料", "常见按 1/8 inch 记录，不能混淆宽高", "输入校验、四舍/向下取整、单位换算"],
    ["成品尺寸 vs 布宽", "外部行业资料", "订购宽度、整体宽度、布料宽度可能不同", "输出生产尺寸、布料尺寸、成品尺寸三个结果"],
    ["派生尺寸规则", "配置中心要求", "订单宽高不是唯一生产口径", "统一输出 ORDER_WIDTH / FINISHED_WIDTH / FABRIC_WIDTH / PROFILE_LENGTH"],
    ["电机/管径能力", "外部行业资料", "电机型号、管径、面料重量会影响最大宽高和数量", "超阈值自动换电机/双电机/禁用电机方案"],
    ["超宽禁用选配", "综合规则", "侧轨、罩壳、安装方式、太阳能板等可能受宽高限制", "命中时 disable option 或 BLOCKER"],
    ["标准品复用", "数据库抽样", "Solar Panel 可单独卖，也可被定制品自动带出", "同一物料可作为销售 SKU 和组件被引用"],
    ["下单备注提示", "订单组合场景", "多通道遥控器分组属于订单备注/订单扩展信息", "配置中心只提示客户填写，不在产品模板里维护分组"],
    ["发布测试规则", "配置中心要求", "规则变更后需要回归典型配置", "上线前重跑价格、组件、禁选、快照测试"]
  ];
}

function componentTypeOptions(value) {
  return ["面料/颜色", "电机/控制件", "铝杆/型材/外壳", "配件/辅料", "组件/物料"]
    .map(item => `<option value="${esc(item)}" ${item === value ? "selected" : ""}>${esc(item)}</option>`)
    .join("");
}

function inventoryClassOptions(value) {
  return ["可售成品", "定制成品/按单生产", "原材料：布匹/面料", "原材料：铝杆/型材", "采购组件：电机/控制件", "采购组件/辅料", "包装材料", "服务项"]
    .map(item => `<option value="${esc(item)}" ${item === value ? "selected" : ""}>${esc(item)}</option>`)
    .join("");
}

function yesNoOptions(value) {
  return [["Y", "启用"], ["N", "停用"]]
    .map(([code, text]) => `<option value="${code}" ${code === value ? "selected" : ""}>${text}</option>`)
    .join("");
}

function ruleTypeOptions(value) {
  return ["显示/隐藏", "选项域", "默认值", "自动计算", "派生值", "禁用选项", "尺寸限制", "自动带组件", "禁止组合", "价格调整", "发布检查", "提醒"]
    .map(item => `<option value="${esc(item)}" ${item === value ? "selected" : ""}>${esc(item)}</option>`)
    .join("");
}

function severityOptions(value) {
  return ["INFO", "WARNING", "ERROR", "BLOCKER"]
    .map(item => `<option value="${esc(item)}" ${item === value ? "selected" : ""}>${esc(item)}</option>`)
    .join("");
}

function explainCard(title, body) {
  return `<div class="explain-card"><h3>${esc(title)}</h3><p>${esc(body)}</p></div>`;
}

function motorRuleDefaults(p = product()) {
  const isZebra = /zebra|柔纱/i.test(`${p.name} ${p.kind}`);
  const isBlackout = /blackout|遮光/i.test(`${p.name} ${p.kind}`);
  const isSolar = /solar|阳光/i.test(`${p.name} ${p.kind}`);
  return {
    widthCm: p.control === "电动" ? 240 : 160,
    heightCm: 180,
    fabricWeightGsm: isBlackout ? 380 : isZebra ? 180 : isSolar ? 260 : 240,
    tubeSizeMm: p.control === "电动" ? 40 : 38,
    motorModel: p.control === "电动" ? "BATTERY_25MM_1_1NM" : "TUBULAR_35MM_3NM"
  };
}

function motorRuleInput(p = product()) {
  return { ...motorRuleDefaults(p), ...(state.motorRuleInput || {}) };
}

function motorProfile(code) {
  return MOTOR_CAPACITY_PROFILES.find(item => item.code === code) || MOTOR_CAPACITY_PROFILES[0];
}

function estimateShadeLoadKg(input) {
  const areaSqm = Math.max(0, Number(input.widthCm || 0) * Number(input.heightCm || 0) / 10000);
  const fabricKg = areaSqm * Number(input.fabricWeightGsm || 0) / 1000;
  return {
    areaSqm,
    fabricKg,
    loadKg: fabricKg * 1.18 + 0.4
  };
}

function canProfileCarry(profile, input, loadKg) {
  return Number(input.tubeSizeMm || 0) >= profile.minTubeMm
    && Number(input.widthCm || 0) <= profile.maxWidthCm
    && Number(input.heightCm || 0) <= profile.maxHeightCm
    && Number(input.fabricWeightGsm || 0) <= profile.maxFabricGsm
    && loadKg <= profile.maxLoadKg;
}

function evaluateMotorCapacity(input) {
  const profile = motorProfile(input.motorModel);
  const load = estimateShadeLoadKg(input);
  const widthCm = Number(input.widthCm || 0);
  const heightCm = Number(input.heightCm || 0);
  const fabricWeightGsm = Number(input.fabricWeightGsm || 0);
  const tubeSizeMm = Number(input.tubeSizeMm || 0);
  const reasons = [];

  if (tubeSizeMm < profile.minTubeMm) {
    reasons.push(`管径 ${tubeSizeMm}mm 小于 ${profile.label} 要求的 ${profile.minTubeMm}mm`);
    return {
      status: "禁用选项",
      severity: "BLOCKER",
      action: "禁用当前电机答案，要求选择更大管径或匹配电机皇冠/转轮",
      target: "ConfigOption.enabled = false",
      reasons,
      load,
      profile,
      disabledOptions: [profile.label, `${tubeSizeMm}mm 管径`, "当前电机组件绑定"]
    };
  }

  if (canProfileCarry(profile, input, load.loadKg)) {
    return {
      status: "通过",
      severity: "INFO",
      action: "MOTOR_QTY = 1，当前电机方案可用",
      target: "MotorCapacityRule",
      reasons: ["宽度、高度、面料克重、管径和估算负载都在当前电机能力范围内"],
      load,
      profile,
      disabledOptions: []
    };
  }

  if (widthCm > profile.maxWidthCm) reasons.push(`宽度 ${widthCm}cm 超过 ${profile.label} 的 ${profile.maxWidthCm}cm`);
  if (heightCm > profile.maxHeightCm) reasons.push(`高度 ${heightCm}cm 超过 ${profile.label} 的 ${profile.maxHeightCm}cm`);
  if (fabricWeightGsm > profile.maxFabricGsm) reasons.push(`面料 ${fabricWeightGsm}g/m2 超过 ${profile.label} 建议 ${profile.maxFabricGsm}g/m2`);
  if (load.loadKg > profile.maxLoadKg) reasons.push(`估算负载 ${load.loadKg.toFixed(2)}kg 超过 ${profile.label} 的 ${profile.maxLoadKg}kg`);

  const dualInput = { ...input, widthCm: widthCm / 2 };
  const dualLoad = estimateShadeLoadKg(dualInput);
  if (widthCm > 200 && canProfileCarry(profile, dualInput, dualLoad.loadKg)) {
    return {
      status: "自动双电机",
      severity: "WARNING",
      action: `MOTOR_QTY = 2；每侧约 ${Math.ceil(widthCm / 2)}cm，动态 BOM 输出两个 ${profile.label}`,
      target: "UsageModel.MOTOR_QTY",
      reasons,
      load,
      profile,
      disabledOptions: ["单电机方案"]
    };
  }

  const nextProfile = MOTOR_CAPACITY_PROFILES.find(item => item.code !== profile.code && canProfileCarry(item, input, load.loadKg));
  if (nextProfile) {
    return {
      status: "换高扭矩",
      severity: "ERROR",
      action: `禁用 ${profile.label}，要求改选 ${nextProfile.label}`,
      target: "MotorCapacityRule",
      reasons,
      load,
      profile,
      disabledOptions: [profile.label]
    };
  }

  return {
    status: "BLOCKER",
    severity: "BLOCKER",
    action: "当前尺寸/面料/管径没有可用电机方案，禁止发布或下单，转人工工程复核",
    target: "ValidationMessage",
    reasons,
    load,
    profile,
    disabledOptions: ["当前产品的全部电机方案"]
  };
}

function groupForSlot(slot) {
  const name = clean(slot.name).toLowerCase();
  if (/fabric|material|color/.test(name)) return "面料/颜色";
  if (/control|motor|chain|direction/.test(name)) return "控制/配件方案";
  if (/mount|roll|rail|cassette|channel/.test(name)) return "结构安装";
  if (/solar|pole/.test(name)) return "配件";
  return "基础信息";
}

function questionCategoryForSlot(slot) {
  const name = clean(slot.name).toLowerCase();
  if (/motor|chain|control/.test(name)) return "销售变体专属问题组";
  if (/fabric|material|color|mount|roll|rail|cassette|channel/.test(name)) return "产品/结构专属问题组";
  if (/solar|pole/.test(name)) return "组件/配件问题组";
  return "公共问题组";
}

function applyLevelForSlot(slot) {
  return questionCategoryForSlot(slot) === "销售变体专属问题组" ? "SALES_VARIANT" : "CONFIG_TEMPLATE";
}

function applyDimensionForSlot(slot, p = product()) {
  const name = clean(slot.name).toLowerCase();
  if (/motor|chain|control/.test(name)) return `control_method = ${controlMethodCode(p)}`;
  if (/fabric|material|color/.test(name)) return "material_group = current_product";
  if (/mount|roll|rail|cassette|channel/.test(name)) return "structure = current_model";
  if (/solar|pole/.test(name)) return "component_scope = optional_accessory";
  return "all_variants";
}

function purposeForSlot(slot) {
  const name = clean(slot.name).toLowerCase();
  if (/fabric|material|color/.test(name)) return "决定面料、颜色、系列范围，并影响价格和生产物料。";
  if (/direction/.test(name)) return "决定操作侧或电机出线侧，不代表控制方式；控制方式由销售变体维度决定。";
  if (/control|motor|chain/.test(name)) return "决定当前销售变体下的控制方案，并自动带出电机、链条或遥控等组件。";
  if (/mount|roll|rail|cassette|channel/.test(name)) return "决定安装结构、外观件和生产加工方式。";
  if (/solar|pole/.test(name)) return "决定是否额外带出配件或安装辅助件。";
  return "作为报价、订单和生产快照里的结构化配置字段。";
}

function currentQuestion() {
  const p = product();
  const slots = slotsForProduct(p);
  if (!state.questionId) state.questionId = slots[0]?.itemId || "";
  return slots.find(slot => slot.itemId === state.questionId) || slots[0];
}

function render() {
  const p = product();
  if (!p) return;
  const slots = slotsForProduct(p);
  if (!slots.some(slot => slot.itemId === state.questionId)) {
    state.questionId = slots[0]?.itemId || "";
  }
  renderProductList();
  renderProductEditor(p);
  renderQuickStartPanel(p);
  renderSharedModuleLinks(p);
  renderSharedModuleDetail(p);
  renderQuestionList(p);
  renderQuestionEditor(p);
  renderOutput(p);
  updateSavedState();
}

function renderProductList() {
  const items = productsForList();
  $("productList").innerHTML = items.map((item, index) => `
    <button class="product-card ${item.id === state.productId ? "active" : ""}" type="button" data-product-id="${esc(item.id)}">
      <span class="product-seq">${String(index + 1).padStart(2, "0")}</span>
      <span>
        <h3>${esc(item.name)}</h3>
        <small>${esc(item.kind)} / ${esc(item.control)}</small>
        <span class="meta-row">
          <span class="tag accent">${item.slots.length} 问题</span>
          <span class="tag">${productOptionsCount(item)} 选项</span>
          <span class="tag">${item.componentMappings.length} 绑定</span>
        </span>
      </span>
    </button>
  `).join("") || `<div class="empty">没有匹配产品。</div>`;
}

function renderProductEditor(p) {
  const draft = currentProductDraft(p);
  $("productTitle").textContent = draft.productName;
  const slots = slotsForProduct(p);
  $("productMeta").textContent = `${p.kind} / ${p.control}。先维护产品系列和可售产品，再维护 ${slots.length} 个问题、${draftOptionsCount(p)} 个答案。`;
  $("metricRow").innerHTML = [
    ["问题", slots.length],
    ["选项", draftOptionsCount(p)],
    ["组件库", componentCatalog(p).length],
    ["组件绑定", p.componentMappings.length],
    ["价格样本", p.priceSamples.length]
  ].map(([labelText, value]) => `<span class="tag accent">${labelText} ${value}</span>`).join("");
  $("productForm").innerHTML = `
    ${field("seriesCode", "产品系列编码", draft.seriesCode)}
    ${field("seriesName", "产品系列名称", draft.seriesName)}
    ${field("modelCode", "产品模型编码", draft.modelCode)}
    ${field("modelName", "产品模型名称", draft.modelName)}
    ${field("productLine", "产品线/品类", draft.productLine)}
    ${field("valanceType", "结构/外壳", draft.valanceType)}
    ${field("variantCode", "销售变体编码", draft.variantCode)}
    ${field("variantName", "销售变体名称", draft.variantName)}
    ${field("variantDimension", "变体适用维度", draft.variantDimension)}
    ${field("controlSystem", "默认控制系统", draft.controlSystem)}
    ${field("productCode", "可售产品编码", draft.productCode)}
    ${field("productName", "可售产品名称", draft.productName)}
    ${selectField("saleMode", "销售方式", draft.saleMode, [["CUSTOM_CONFIGURED", "定制品"], ["STANDARD_SKU", "标准品"], ["SEMI_STANDARD", "半标准品"], ["ACCESSORY", "配件/服务"]])}
    ${selectField("productNature", "物料/产品性质", draft.productNature, ["可售成品", "定制成品/按单生产", "原材料：布匹/面料", "原材料：铝杆/型材", "采购组件：电机/控制件", "采购组件/辅料", "包装材料", "服务项"])}
    ${selectField("stockPolicy", "库存口径", draft.stockPolicy, ["不预生产成品，按订单生成 BOM", "成品库存", "原材料库存", "采购组件库存", "不管库存"])}
    ${selectField("templateSource", "模板来源", draft.templateSource, [["GENERATED_FROM_PRODUCT", "按当前产品生成草稿"], ["COPY_PUBLISHED", "复制已发布模板"], ["CLONE_SIMILAR_PRODUCT", "从相似产品克隆"], ["IMPORT_PACKAGE", "后期导入资料包/Excel"]])}
    ${field("templateCode", "配置模板版本", draft.templateCode)}
    ${selectField("status", "配置状态", draft.status, [["DRAFT", "草稿"], ["TESTING", "测试中"], ["PUBLISHED", "已发布"], ["ARCHIVED", "已归档"]])}
    ${selectField("displayUnit", "销售显示单位", draft.displayUnit, ["inch", "cm"])}
    ${selectField("engineUnit", "内部计算单位", draft.engineUnit, ["cm"])}
    ${field("owner", "维护部门/负责人", draft.owner)}
    <div class="field">
      <label>操作</label>
      <button class="entry-btn" type="button" data-action="save-product">保存产品基础</button>
    </div>
  `;
  $("productSourceNote").innerHTML = `
    <div class="source-note">
      ${explainCard("模板哪里来", "模板不是凭空输入编号。第一期按当前产品生成草稿、复制已发布模板或从相似产品克隆；资料包导入先作为后期入口预留。")}
      ${explainCard("销售变体是什么", "同一个产品模型可以有多个可售入口。拉珠、电机、标准、未来的市场/等级/包装方式都可以作为变体维度，不需要改配置中心架构。")}
      ${explainCard("问题怎么挂上来", "配置模板不是手写一整张表，而是挂载公共问题组、结构问题组、变体专属问题组。当前产品只维护它需要的组合。")}
      ${explainCard("组件哪里来", "组件先进入组件库：从物料主数据、产品资料包、供应商清单或历史绑定导入；答案只能选择组件库里的组件。")}
      ${explainCard("成品和原材料", "可售产品是前台卖的入口；布匹、型材、电机、辅料是后期库存/采购/生产会用的物料，不能混成一个概念。")}
      ${explainCard("问题不是全部", "问题/答案只是配置表单。完整配置还包括物料基础、组件库、用量模型、价格规则、规则引擎和发布测试。")}
    </div>
  `;
}

function blueprintForProduct(p) {
  const method = controlMethodCode(p);
  if (p.kind === "标准配件" || method === "standard") return "标准配件蓝图";
  if (method === "motor") return `${p.kind || "窗帘"}-电动蓝图`;
  if (method === "chain") return `${p.kind || "窗帘"}-拉珠蓝图`;
  return `${p.kind || "窗帘"}-自定义蓝图`;
}

function similarProducts(p) {
  return DATA.products
    .filter(item => item.id !== p.id)
    .map(item => ({
      product: item,
      score: (item.kind === p.kind ? 40 : 0)
        + (item.control === p.control ? 25 : 0)
        + (item.valanceCategoryName === p.valanceCategoryName ? 20 : 0)
        + Math.min(item.slots.length, p.slots.length)
    }))
    .sort((a, b) => b.score - a.score)
    .slice(0, 3)
    .map(item => item.product);
}

function questionGroupTemplates(p) {
  const method = controlMethodCode(p);
  const groups = [
    {
      name: "公共尺寸与安装问题组",
      version: "v1",
      scope: "所有定制产品",
      includes: "宽度、高度、安装方式、控制位置",
      action: "默认挂载"
    },
    {
      name: `${p.valanceCategoryName || "当前结构"}结构问题组`,
      version: "v1",
      scope: "同结构产品",
      includes: "罩壳、底杆、卷布方向、侧轨",
      action: "按产品模型挂载"
    },
    {
      name: method === "motor" ? "Motor 电机控制问题组" : method === "chain" ? "Chain 拉珠控制问题组" : "标准品轻量问题组",
      version: "v1",
      scope: `system = ${method}`,
      includes: method === "motor" ? "电机方案、遥控、电源、太阳能板" : method === "chain" ? "链条颜色、拉珠尺寸、定位器" : "规格、适用范围",
      action: "按销售变体挂载"
    },
    {
      name: `${p.kind || "产品"}面料/颜色问题组`,
      version: "v1",
      scope: "当前分类和面料系列",
      includes: "面料、颜色、面料方向、面料属性",
      action: "按分类和选项域挂载"
    }
  ];
  return groups;
}

function releaseGaps(p) {
  const gaps = [];
  if (!p.priceSamples.length) gaps.push(["价格来源", "未抽到价格样本，需要选择固定价、矩阵价或价格适配器"]);
  if (!p.componentMappings.length && p.kind !== "标准配件") gaps.push(["组件绑定", "没有答案到组件绑定，BOM 无法自动生成"]);
  const missingCn = p.slots.filter(slot => !translate(slot.name, SLOT_CN)).length;
  if (missingCn) gaps.push(["中英文标称", `${missingCn} 个问题缺少中文业务名，需要补齐给销售和客户看`]);
  const largeOptionSlots = p.slots.filter(slot => slot.options.length > 30).length;
  if (largeOptionSlots) gaps.push(["批量适用范围", `${largeOptionSlots} 个问题答案很多，应批量设置适用分类/变体/组件`]);
  if (!gaps.length) gaps.push(["发布检查", "当前样本的关键配置完整，可进入测试用例"]);
  return gaps;
}

function renderQuickStartPanel(p) {
  const similar = similarProducts(p);
  const groups = questionGroupTemplates(p);
  const gaps = releaseGaps(p);
  $("quickStartPanel").innerHTML = `
    <div class="quick-grid">
      ${explainCard("推荐创建方式", `从 ${similar[0]?.name || "相似产品"} 克隆，再套用 ${blueprintForProduct(p)}，只补差异。`)}
      ${explainCard("10 分钟目标", "80% 从相似产品、蓝图、问题组模板和批量粘贴小工具生成；录入员只处理 20% 差异。")}
      ${explainCard("问题组要独立", "问题组是可复用资产，不是产品里的字段。产品模板通过挂载问题组快速生成。")}
      ${explainCard("安全边界", "克隆生成独立草稿；问题组引用走版本，已发布产品不被偷偷改动。")}
    </div>
    <div class="quick-workflow">
      ${[
        ["1", "选分类和创建方式", "从相似产品克隆 / 从蓝图创建 / 手工录入"],
        ["2", "确认模型和变体", `当前建议：${blueprintForProduct(p)}，system = ${controlMethodCode(p)}`],
        ["3", "一键挂载问题组", groups.map(item => item.name).join(" / ")],
        ["4", "批量维护答案和组件", "粘贴颜色/面料/组件列表，按编码、英文名、中文名、供应商编号匹配"],
        ["5", "差异补录并测试", "只处理缺口清单，跑发布测试后提交审核"]
      ].map(item => `
        <div class="quick-step">
          <span>${esc(item[0])}</span>
          <strong>${esc(item[1])}</strong>
          <p>${esc(item[2])}</p>
        </div>
      `).join("")}
    </div>
    <div class="quick-columns">
      <div>
        <h3>相似产品克隆候选</h3>
        <div class="quick-list">
          ${similar.map(item => `
            <div>
              <strong>${esc(item.name)}</strong>
              <span>${esc(item.kind)} / ${esc(item.control)} / ${item.slots.length} 问题 / ${productOptionsCount(item)} 答案</span>
            </div>
          `).join("") || `<div class="empty">没有找到相似产品。</div>`}
        </div>
      </div>
      <div>
        <h3>本产品建议挂载的问题组</h3>
        <div class="quick-list">
          ${groups.map(item => `
            <div>
              <strong>${esc(item.name)} ${esc(item.version)}</strong>
              <span>${esc(item.scope)}；${esc(item.includes)}；${esc(item.action)}</span>
            </div>
          `).join("")}
        </div>
      </div>
      <div>
        <h3>发布缺口清单</h3>
        <div class="quick-list">
          ${gaps.map(item => `
            <div>
              <strong>${esc(item[0])}</strong>
              <span>${esc(item[1])}</span>
            </div>
          `).join("")}
        </div>
      </div>
    </div>
    <div class="quick-actions">
      <button class="entry-btn secondary" type="button">从相似产品克隆草稿</button>
      <button class="entry-btn secondary" type="button">套用蓝图</button>
      <button class="entry-btn secondary" type="button">批量粘贴答案/颜色</button>
      <button class="entry-btn secondary" type="button">查看差异对比</button>
    </div>
  `;
}

function sharedModules(p = product()) {
  const standard = DATA.products.find(item => item.name === "Solar Panel") || DATA.products.find(item => item.kind === "标准配件");
  const media = mediaCoverage(p);
  return [
    {
      key: "questionGroups",
      label: "问题组模板",
      title: "公共问题组和蓝图模板",
      scope: "公共配置资产",
      relation: `当前建议挂载 ${questionGroupTemplates(p).length} 个问题组`,
      count: `${questionGroupTemplates(p).length} 组`
    },
    {
      key: "standard",
      label: "标准品基础",
      title: "标准品如何进入配置中心",
      scope: "公共主数据",
      relation: standard ? `${standard.name} 可单独卖，也可被当前定制品规则引用` : "当前抽样没有标准品",
      count: standard ? `${standard.slots.length} 问题 / ${productOptionsCount(standard)} 答案` : "0"
    },
    {
      key: "components",
      label: "组件库",
      title: "组件和物料基础信息",
      scope: "公共物料库",
      relation: `当前产品引用 ${componentCatalog(p).length} 个组件，${p.componentMappings.length} 条答案绑定`,
      count: `${componentCatalog(p).length} 组件`
    },
    {
      key: "pricing",
      label: "价格/用量模型",
      title: "价格和用量基础",
      scope: "公共算法和价格表",
      relation: p.priceSamples.length ? `当前产品有 ${p.priceSamples.length} 个价格样本` : "当前产品价格待补",
      count: `${p.priceSamples.length} 价格样本`
    },
    {
      key: "media",
      label: "资料资产库",
      title: "颜色图片、组件图片和说明文件",
      scope: "公共资料中心",
      relation: `当前产品 ${media.matched} 个资料已匹配，${media.pending} 个待补`,
      count: `${media.assets.length} 资料`
    },
    {
      key: "rules",
      label: "规则库",
      title: "动态规则设置",
      scope: "公共规则引擎",
      relation: "当前产品会读取显示、禁选、尺寸、电机能力、发布检查等规则",
      count: `${dynamicRules(p).length} 规则`
    },
    {
      key: "governance",
      label: "发布闭环",
      title: "联合发布检查、缺口待办和影响分析",
      scope: "公共运营闸门",
      relation: "配置、价格、资料、组件和测试全部通过后才允许可销售",
      count: "3 闭环"
    }
  ];
}

function renderSharedModuleLinks(p) {
  $("sharedModuleLinks").innerHTML = `
    <div class="module-link-grid">
      ${sharedModules(p).map(item => `
        <button class="module-link ${state.activeSharedModule === item.key ? "active" : ""}" type="button" data-module-open="${esc(item.key)}">
          <span class="module-label">${esc(item.label)}</span>
          <strong>${esc(item.title)}</strong>
          <small>${esc(item.scope)}</small>
          <span>${esc(item.relation)}</span>
          <em>${esc(item.count)}</em>
        </button>
      `).join("")}
    </div>
    <div class="extension-guardrail">
      ${explainCard("配置中心可以扩展什么", "新增配置问题、答案、组件绑定、尺寸/能力阈值、价格/用量算法参数、客户提示文案、发布测试用例。")}
      ${explainCard("配置中心不扩展什么", "订单行分组、客户备注解析、安装排班、采购审批、库存扣减、财务核算，这些属于下游业务系统。")}
      ${explainCard("未来未知维度怎么扩", "不要新增硬编码层。把市场、等级、包装、控制方式都作为销售变体维度，再挂载对应问题组和规则。")}
      ${explainCard("怎么保证以后少改架构", "新需求先判断是产品能力、公共基础、规则参数、销售变体维度，还是订单组合。除订单组合外才进入配置中心。")}
    </div>
  `;
}

function renderSharedModuleDetail(p) {
  const card = $("sharedModuleDetailCard");
  const detail = $("sharedModuleDetail");
  const module = sharedModules(p).find(item => item.key === state.activeSharedModule);
  card.classList.toggle("is-hidden", !module);
  if (!module) {
    detail.innerHTML = "";
    return;
  }

  const actionButton = module.key === "components"
    ? `<button class="entry-btn secondary" type="button" data-action="save-components">保存组件库</button>`
    : module.key === "rules"
      ? `<button class="entry-btn secondary" type="button" data-action="save-dynamic-rules">保存动态规则</button>`
      : module.key === "media"
        ? `<button class="entry-btn secondary" type="button" data-action="save-media-assets">保存资料绑定</button>`
        : "";

  const targetId = {
    questionGroups: "questionGroupPanel",
    standard: "standardProductPanel",
    components: "componentLibrary",
    pricing: "priceRulePanel",
    media: "mediaAssetPanel",
    rules: "dynamicRulePanel",
    governance: "governancePanel"
  }[module.key];

  detail.innerHTML = `
    <div class="section-head">
      <div>
        <span class="eyebrow">公共模块详情 / ${esc(module.label)}</span>
        <h2>${esc(module.title)}</h2>
        <p>${esc(module.scope)}。这里只是从当前产品入口跳转查看，数据本身是公共基础能力，不属于某一个产品表单。</p>
      </div>
      <div class="entry-actions">
        ${actionButton}
        <button class="entry-btn secondary" type="button" data-module-close>收起详情</button>
      </div>
    </div>
    <div class="module-scope-note">
      ${explainCard("为什么不放进产品表单", "问题组、组件、颜色图、标准品、价格模型和规则引擎会被多个产品复用。产品只保存引用关系，不复制一份公共基础数据。")}
      ${explainCard("当前产品关系", module.relation)}
      ${explainCard("开发边界", "Step 1 和 Step 5 是产品配置主流程；这里属于公共模块的查看/维护入口。修改公共资产时先看影响范围，再保存草稿走审批。")}
    </div>
    <div id="${esc(targetId)}"></div>
  `;

  if (module.key === "questionGroups") renderQuestionGroupPanel(p);
  if (module.key === "standard") renderStandardProductPanel();
  if (module.key === "components") renderComponentLibrary(p);
  if (module.key === "pricing") renderPriceRulePanel(p);
  if (module.key === "media") renderMediaAssetPanel(p);
  if (module.key === "rules") renderDynamicRulePanel(p);
  if (module.key === "governance") renderGovernancePanel(p);
}

function renderGovernancePanel(p) {
  const coverage = mediaCoverage(p);
  const gates = [
    ["配置模板", "已保存草稿，待发布", "检查产品基础、销售变体、问题组、问题/答案是否完整"],
    ["价格方案", p.priceSamples.length ? "有价格样本，待价格中心发布" : "缺价格方案", "必须引用已发布价格方案，不能只发布配置"],
    ["客户资料", coverage.pending ? `${coverage.pending} 项待补` : "资料齐全", "客户可见图片、说明、中英文、安装/测量帮助要齐"],
    ["组件绑定", p.componentMappings.length ? `${p.componentMappings.length} 条绑定` : "缺组件绑定", "答案选中后能带出组件或物料引用"],
    ["测试用例", "待回归", "典型尺寸、典型答案、超宽/禁选/BLOCKER 要通过"],
    ["BLOCKER", releaseGaps(p).length > 1 ? "有缺口待处理" : "未发现阻塞", "存在阻塞项时不能变成可销售"]
  ];
  const impactRows = [
    ["问题组", `${questionGroupTemplates(p).length} 个建议组`, "影响挂载该组的产品、销售变体和问题顺序", "另存新版本 / 选择同步产品 / 草稿审批"],
    ["组件", `${componentCatalog(p).length} 个当前引用`, "影响自动带出的电机、遥控、型材、辅料和 BOM", "选择同步范围后保存组件变更草稿"],
    ["颜色图/面料图", `${coverage.byType["面料/颜色图"] || 0} 个资产`, "影响答案预览、客户确认、售后复原", "更新资料版本并选择同步产品"],
    ["价格方案", `${p.priceSamples.length} 个样本`, "影响报价结果和订单价格快照", "进入价格中心审批，不直接改配置"],
    ["动态规则", `${dynamicRules(p).length} 条规则`, "影响显示、禁选、自动组件、用量和发布测试", "生成规则草稿并跑回归用例"]
  ];
  const gaps = releaseGaps(p).map((item, index) => [
    `GAP-${String(index + 1).padStart(2, "0")}`,
    item[0],
    item[1],
    index % 3 === 0 ? "产品运营" : index % 3 === 1 ? "资料负责人" : "价格/组件负责人",
    "待处理"
  ]);
  $("governancePanel").innerHTML = `
    <div class="library-summary">
      ${explainCard("联合发布检查", "配置发布、价格发布、资料齐全、组件绑定、测试通过，全部满足后商品才允许进入可销售状态。")}
      ${explainCard("缺口待办闭环", "缺图片、缺价格、缺组件、缺中英文说明不能只提示，要能指派负责人、跟状态、复核完成。")}
      ${explainCard("变更影响分析", "修改问题组、组件、颜色图、价格方案或规则前，先知道影响哪些产品，再选择同步范围。")}
      ${explainCard("导入先后置", "第一期保留导入入口和批次设计预留，主线先做克隆、问题组复用、批量粘贴和人工补齐。")}
    </div>
    <div class="data-table-wrap">
      <table class="data-table governance-table">
        <thead>
          <tr>
            <th>发布检查项</th>
            <th>当前样本状态</th>
            <th>为什么必须检查</th>
          </tr>
        </thead>
        <tbody>
          ${gates.map(row => `<tr>${row.map(cell => `<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}
        </tbody>
      </table>
    </div>
    <div class="data-table-wrap">
      <table class="data-table governance-table">
        <thead>
          <tr>
            <th>变更对象</th>
            <th>当前引用</th>
            <th>可能影响</th>
            <th>操作闭环</th>
          </tr>
        </thead>
        <tbody>
          ${impactRows.map(row => `<tr>${row.map(cell => `<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}
        </tbody>
      </table>
    </div>
    <div class="data-table-wrap">
      <table class="data-table governance-table">
        <thead>
          <tr>
            <th>待办号</th>
            <th>缺口类型</th>
            <th>缺口说明</th>
            <th>负责人</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          ${gaps.map(row => `<tr>${row.map(cell => `<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function renderQuestionGroupPanel(p) {
  const groups = questionGroupTemplates(p);
  $("questionGroupPanel").innerHTML = `
    <div class="library-summary">
      ${explainCard("问题组是什么", "问题组是可复用的配置资产。产品模板不是从 0 手写问题，而是挂载公共问题组、结构问题组、销售变体问题组。")}
      ${explainCard("为什么要版本", "问题组升级不能偷偷影响已发布产品。v1 已发布产品继续使用；v2 经测试后再升级。")}
      ${explainCard("怎么快速录入", "新产品优先从相似产品克隆，再选择蓝图和问题组，最后只补差异。")}
      ${explainCard("怎么反向沉淀", "成熟产品里的多个问题可以另存为问题组，后续产品直接引用。")}
    </div>
    <div class="data-table-wrap">
      <table class="data-table price-table">
        <thead>
          <tr>
            <th>问题组模板</th>
            <th>版本</th>
            <th>适用范围</th>
            <th>包含问题</th>
            <th>挂载方式</th>
            <th>后期操作</th>
          </tr>
        </thead>
        <tbody>
          ${groups.map(item => `
            <tr>
              <td>${esc(item.name)}</td>
              <td>${esc(item.version)}</td>
              <td>${esc(item.scope)}</td>
              <td>${esc(item.includes)}</td>
              <td>${esc(item.action)}</td>
              <td>引用 / 复制为草稿 / 查看差异 / 升级版本</td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
    <div class="quick-columns">
      <div>
        <h3>蓝图示例</h3>
        <div class="quick-list">
          ${["卷帘-电动蓝图", "卷帘-拉珠蓝图", "蜂巢帘-无拉蓝图", "罗马帘-拉珠蓝图", "标准配件蓝图"].map(item => `
            <div><strong>${esc(item)}</strong><span>默认问题组 + 默认规则组 + 默认价格/用量模型 + 发布测试用例</span></div>
          `).join("")}
        </div>
      </div>
      <div>
        <h3>批量操作</h3>
        <div class="quick-list">
          ${["批量设置适用分类/产品形态", "批量设置适用销售变体", "批量启用/停用答案", "批量绑定组件类型", "批量粘贴中英文名"].map(item => `
            <div><strong>${esc(item)}</strong><span>用于 80+ 面料答案、颜色答案和组件绑定的快速处理。</span></div>
          `).join("")}
        </div>
      </div>
      <div>
        <h3>差异对比</h3>
        <div class="quick-list">
          ${["来源产品 vs 当前草稿", "问题组 v1 vs v2", "批量粘贴结果 vs 已发布模板", "组件自动匹配成功/失败"].map(item => `
            <div><strong>${esc(item)}</strong><span>发布前必须知道改了什么，避免复制后误影响报价或生产。</span></div>
          `).join("")}
        </div>
      </div>
    </div>
  `;
}

function renderComponentLibrary(p) {
  const components = componentCatalog(p);
  const counts = components.reduce((acc, item) => {
    acc[item.componentType] = (acc[item.componentType] || 0) + 1;
    return acc;
  }, {});
  $("componentLibrary").innerHTML = `
    <div class="library-summary">
      ${explainCard("组件库来源", "第一版可从真实答案绑定、物料主数据、供应商清单和历史绑定抽取；缺的组件允许手工新建草稿。资料包导入后置。")}
      ${explainCard("成品 vs 原材料", "成品是可售入口或按单生产结果；原材料主要是布匹/面料、铝杆/型材，采购组件是电机、遥控、太阳能板、辅料。")}
      ${explainCard("面料/颜色", `${counts["面料/颜色"] || 0} 个。通常对应布匹/面料库存，影响面料用量、面料单价或矩阵价。`)}
      ${explainCard("电机/型材/辅料", `${(counts["电机/控制件"] || 0) + (counts["铝杆/型材/外壳"] || 0) + (counts["配件/辅料"] || 0)} 个。后期进入采购、库存、领料、BOM 和成本。`)}
    </div>
    <div class="data-table-wrap">
      <table class="data-table component-table">
        <thead>
          <tr>
            <th>组件编码</th>
            <th>组件类型</th>
            <th>后期物料口径</th>
            <th>中文名</th>
            <th>英文名/供应商名</th>
            <th>来源</th>
            <th>价格口径</th>
            <th>用量模型</th>
            <th>后期用途</th>
            <th>绑定次数</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          ${components.slice(0, 80).map(item => `
            <tr data-component-id="${esc(item.sourceId)}">
              <td><input data-component-field="componentCode" value="${esc(item.componentCode)}"></td>
              <td>
                <select data-component-field="componentType">
                  ${componentTypeOptions(item.componentType)}
                </select>
              </td>
              <td>
                <select data-component-field="inventoryClass">
                  ${inventoryClassOptions(item.inventoryClass)}
                </select>
              </td>
              <td><input data-component-field="cnName" value="${esc(item.cnName)}"></td>
              <td><input data-component-field="enName" value="${esc(item.enName)}"></td>
              <td><input data-component-field="source" value="${esc(item.source)}"></td>
              <td><input data-component-field="priceMethod" value="${esc(item.priceMethod)}"></td>
              <td><input data-component-field="usageModel" value="${esc(item.usageModel)}"></td>
              <td><input data-component-field="materialRole" value="${esc(item.materialRole)}"></td>
              <td>${esc(item.bindCount)}</td>
              <td>
                <select data-component-field="status">
                  ${optionStatusOptions(item.status)}
                </select>
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function renderMediaAssetPanel(p) {
  const coverage = mediaCoverage(p);
  const assets = coverage.assets;
  const feedback = customerFeedbackItems();
  const requirements = mediaRequirementRows(p);
  const colorAssets = assets.filter(item => item.assetType.includes("颜色") || item.assetType.includes("面料")).slice(0, 6);
  const componentAssets = assets.filter(item => item.assetType.includes("组件")).slice(0, 5);
  const guideAssets = assets.filter(item => !item.assetType.includes("颜色") && !item.assetType.includes("面料") && !item.assetType.includes("组件")).slice(0, 4);
  $("mediaAssetPanel").innerHTML = `
    <div class="library-summary">
      ${explainCard("资料资产覆盖什么", "产品、问题、答案、组件、动态规则和标准品都可以绑定说明、图片、安装/测量帮助，不只是颜色图。")}
      ${explainCard("资料资产来源", "供应商图片包、历史订单图片、客户反馈和人工上传都先进入资料资产库。产品资料包和 Excel 附件解析后期再做。")}
      ${explainCard("发布前检查", `${coverage.pending} 个资料待补。客户会看到的问题、答案、组件和规则提示缺说明时，应阻止发布或转人工复核。`)}
    </div>
    <div class="feedback-panel">
      <div class="section-head compact">
        <div>
          <span class="eyebrow">资料覆盖矩阵</span>
          <h3>所有配置对象都要能解释清楚</h3>
          <p>操作员不只维护图片，还要维护客户说明、内部说明、安装帮助、测量帮助和规则提示。</p>
        </div>
      </div>
      <div class="data-table-wrap">
        <table class="data-table media-requirement-table">
          <thead>
            <tr>
              <th>对象</th>
              <th>当前范围</th>
              <th>必须维护的资料</th>
              <th>给谁看</th>
              <th>发布检查</th>
            </tr>
          </thead>
          <tbody>
            ${requirements.map(item => `
              <tr>
                <td>${esc(item.objectType)}</td>
                <td>${esc(item.target)}</td>
                <td>${esc(item.required)}</td>
                <td>${esc(item.visible)}</td>
                <td>${esc(item.check)}</td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    </div>
    <div class="asset-summary">
      ${assetMetric("已匹配资料", coverage.matched)}
      ${assetMetric("待补资料", coverage.pending)}
      ${assetMetric("颜色/面料图", coverage.byType["面料/颜色图"] || 0)}
      ${assetMetric("组件图片", coverage.byType["组件图片"] || 0)}
    </div>
    <div class="asset-gallery">
      <div>
        <h3>颜色/面料图预览</h3>
        <div class="asset-card-grid">
          ${colorAssets.map(assetPreviewCard).join("")}
        </div>
      </div>
      <div>
        <h3>组件图片预览</h3>
        <div class="asset-card-grid">
          ${componentAssets.map(assetPreviewCard).join("")}
        </div>
      </div>
      <div>
        <h3>说明文件预览</h3>
        <div class="asset-card-grid">
          ${guideAssets.map(assetPreviewCard).join("")}
        </div>
      </div>
    </div>
    <div class="source-note">
      ${explainCard("后期资料包建议", "等资料格式标准化后，再把 product-info、options、components、images/colors、images/components、install-guides 做成统一资料包。")}
      ${explainCard("命名规则", "颜色图建议 COLOR_N001_PureWhite.jpg；面料图 FABRIC_YS180002_White.jpg；组件图 COMPONENT_BLUETOOTH_MOTOR.jpg。")}
      ${explainCard("下游使用", "销售下单、客户确认、生产查看、售后说明都读取同一份资料资产，订单快照保存当时文件版本。")}
    </div>
    <div class="feedback-panel">
      <div class="section-head compact">
        <div>
          <span class="eyebrow">客户反馈转说明资料</span>
          <h3>提示和文字说明要沉淀下来</h3>
          <p>这里只处理和系统提示、帮助文档、配件说明、安装走线说明有关的反馈。</p>
        </div>
      </div>
      <div class="data-table-wrap">
        <table class="data-table feedback-table">
          <thead>
            <tr>
              <th>反馈分类</th>
              <th>客户问题</th>
              <th>归属</th>
              <th>关联对象</th>
              <th>处理方式</th>
            </tr>
          </thead>
          <tbody>
            ${feedback.map(item => `
              <tr>
                <td>${esc(item.category)}</td>
                <td>${esc(item.feedback)}</td>
                <td>${feedbackRouteBadge(item.route)}</td>
                <td>${esc(item.target)}</td>
                <td>${esc(item.action)}</td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
    </div>
    <div class="data-table-wrap">
      <table class="data-table media-table">
        <thead>
          <tr>
            <th>资料类型</th>
            <th>绑定对象</th>
            <th>对象编码</th>
            <th>文件名/路径</th>
            <th>状态</th>
            <th>用途</th>
          </tr>
        </thead>
        <tbody>
          ${assets.map(item => `
            <tr data-media-id="${esc(item.assetId)}">
              <td>
                <select data-media-field="assetType">
                  ${mediaTypeOptions(item.assetType)}
                </select>
              </td>
              <td><input data-media-field="bindTarget" value="${esc(item.bindTarget)}"></td>
              <td><input data-media-field="bindCode" value="${esc(item.bindCode)}"></td>
              <td><input data-media-field="fileName" value="${esc(item.fileName)}" placeholder="例如：COLOR_N001_PureWhite.jpg"></td>
              <td>
                <select data-media-field="status">
                  ${mediaStatusOptions(item.status)}
                </select>
              </td>
              <td><input data-media-field="usage" value="${esc(item.usage)}"></td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function assetMetric(labelText, value) {
  return `
    <div class="asset-metric">
      <strong>${esc(value)}</strong>
      <span>${esc(labelText)}</span>
    </div>
  `;
}

function assetPreviewCard(asset) {
  const kind = mediaKindForAsset(asset);
  return `
    <div class="asset-card ${esc(kind)}">
      <div class="asset-thumb" style="--swatch:${esc(swatchForText(asset.previewName || asset.bindTarget))}">
        ${kind === "swatch" ? `<span></span>` : kind === "component" ? `<b>${esc((asset.previewName || "C").slice(0, 2).toUpperCase())}</b>` : `<i>${kind === "guide" ? "PDF" : "IMG"}</i>`}
      </div>
      <div>
        <strong>${esc(asset.bindTarget)}</strong>
        <small>${esc(asset.bindCode)}</small>
        <em class="${asset.status === "已匹配" ? "ok" : "warn"}">${esc(asset.status)}</em>
      </div>
    </div>
  `;
}

function mediaTypeOptions(value) {
  return ["产品主图", "面料/颜色图", "结构颜色图", "组件图片", "测量说明", "安装说明", "包装/运输图", "客户说明文件"].map(item =>
    `<option value="${esc(item)}" ${item === value ? "selected" : ""}>${esc(item)}</option>`
  ).join("");
}

function mediaStatusOptions(value) {
  return ["已匹配", "待上传", "缺图片", "需复核", "停用"].map(item =>
    `<option value="${esc(item)}" ${item === value ? "selected" : ""}>${esc(item)}</option>`
  ).join("");
}

function feedbackRouteBadge(route) {
  const kind = route === "公共资料" ? "ok" : route === "订单流程" || route === "订单组合" ? "warn" : "neutral";
  return `<span class="route-badge ${kind}">${esc(route)}</span>`;
}

function renderStandardProductPanel() {
  const standard = DATA.products.find(item => item.name === "Solar Panel") || DATA.products.find(item => item.kind === "标准配件");
  if (!standard) {
    $("standardProductPanel").innerHTML = `<div class="empty">当前抽样里没有标准产品。</div>`;
    return;
  }
  const usedBy = DATA.products
    .filter(item => item.id !== standard.id && item.componentMappings.some(mapping => clean(mapping.componentName).toLowerCase().includes("solar panel")))
    .map(item => item.name);
  $("standardProductPanel").innerHTML = `
    <div class="source-note">
      ${explainCard("标准品身份", `${standard.name} 是标准配件：可以作为固定 SKU 单独销售，也可以被定制品规则自动带出。`)}
      ${explainCard("怎么维护", "不需要完整的 12 个问题模板；主要维护 SKU、固定价、库存口径、图片说明、适用范围。")}
      ${explainCard("怎么被引用", `当前抽样中 ${usedBy.length} 个定制产品会在选择 Solar Panel 时自动引用它。`)}
      ${explainCard("和定制品差异", "标准品走固定价和成品库存；定制品按问题/答案/规则生成动态物料清单。")}
    </div>
    <div class="data-table-wrap">
      <table class="data-table price-table">
        <thead>
          <tr>
            <th>配置项</th>
            <th>标准品例子</th>
            <th>后期业务用途</th>
          </tr>
        </thead>
        <tbody>
          ${[
            ["产品性质", "可售成品 / 采购组件", "可单独卖，也可作为 BOM 组件被带出"],
            ["库存口径", "成品库存 / 采购组件库存", "销售扣库存，生产领料也能扣同一组件"],
            ["价格规则", standard.priceSamples.length ? `${standard.priceSamples.length} 个价格样本或固定价` : "固定价待维护", "标准品通常不按宽高算价"],
            ["适用范围", usedBy.slice(0, 3).join(" / ") || "待绑定", "哪些定制产品可以选择或自动带出该标准品"],
            ["配置问题", `${standard.slots.length} 个轻量问题 / ${productOptionsCount(standard)} 个答案`, "如需选择适用面料或规格，可保留简单选项"]
          ].map(row => `<tr>${row.map(cell => `<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function renderPriceRulePanel(p) {
  const price = p.priceSummary || {};
  $("priceRulePanel").innerHTML = `
    <div class="price-rule-grid">
      ${explainCard("面料价格", "面料本身是组件，价格可以走面料单价 x FABRIC_AREA，也可以走当前抽样里的尺寸矩阵价。")}
      ${explainCard("电机价格", "电机是组件。客户选电机方案后绑定电机组件，价格按固定件单价或包含在矩阵价里。")}
      ${explainCard("铝杆/型材价格", "外壳、底杆、侧轨等属于型材组件，可按 PROFILE_LENGTH 计算长度和价格。")}
      ${explainCard("用量模型", "FABRIC_AREA、PROFILE_LENGTH、MOTOR_QTY 等结果同时给价格和生产 BOM 使用，避免两边各算一遍。")}
      ${explainCard("规则来源", "必填、筛选答案、尺寸限制、禁止组合、自动带配件都读取产品基础、组件库和问题答案。")}
      ${explainCard("模板发布", "模板草稿通过测试后发布；报价/订单只读发布版本，旧订单保存当时快照。")}
    </div>
    <div class="data-table-wrap">
      <table class="data-table price-table">
        <thead>
          <tr>
            <th>配置项</th>
            <th>当前样本</th>
            <th>正式系统怎么维护</th>
          </tr>
        </thead>
        <tbody>
          ${[
            ["价格模式", price.mode || "待维护", "PriceRule 选择 MATRIX / AREA / USAGE_UNIT / FIXED"],
            ["价格档", price.rows || p.priceSamples.length || 0, "PriceMatrix 按产品线、系列、销售变体维度、宽高维护"],
            ["宽高范围", `${price.minWidth || "-"}-${price.maxWidth || "-"} x ${price.minHeight || "-"}-${price.maxHeight || "-"} inch`, "DimensionRule 单独维护，不能只藏在价格表里"],
            ["面料用量", "FABRIC_AREA = width_cm * height_cm / 10000 * 损耗率", "UsageModel 维护算法类型和参数"],
            ["型材长度", "PROFILE_LENGTH = width_cm - 扣减量", "UsageModel 给底杆、外壳、侧轨等组件使用"],
            ["电机数量", "MOTOR_QTY = 1，超宽时可变 2", "Rule + UsageModel 共同决定"]
          ].map(row => `<tr>${row.map(cell => `<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function renderMotorCapacitySimulator(p) {
  const input = motorRuleInput(p);
  const result = evaluateMotorCapacity(input);
  return `
    <div class="motor-simulator" id="motorRuleSimulator">
      <div class="simulator-head">
        <div>
          <h3>电机能力规则模拟</h3>
          <p>这里不是建议文本。输入当前配置后，规则引擎按宽、高、面料克重、管径、电机型号输出动作。</p>
        </div>
        <span class="result-pill ${esc(result.severity.toLowerCase())}">${esc(result.status)}</span>
      </div>
      <div class="motor-form">
        ${motorField("widthCm", "宽度 cm", input.widthCm, "number")}
        ${motorField("heightCm", "高度 cm", input.heightCm, "number")}
        ${motorField("fabricWeightGsm", "面料克重 g/m2", input.fabricWeightGsm, "number")}
        ${motorField("tubeSizeMm", "管径 mm", input.tubeSizeMm, "number")}
        <div class="field">
          <label>电机型号</label>
          <select data-motor-field="motorModel">
            ${MOTOR_CAPACITY_PROFILES.map(item => `<option value="${esc(item.code)}" ${item.code === input.motorModel ? "selected" : ""}>${esc(item.label)}</option>`).join("")}
          </select>
        </div>
      </div>
      <div class="simulator-output">
        <div class="result-card">
          <h4>系统动作</h4>
          <strong>${esc(result.action)}</strong>
          <p>影响对象：${esc(result.target)} / 严重程度：${esc(result.severity)}</p>
        </div>
        <div class="result-card">
          <h4>估算负载</h4>
          <strong>${result.load.loadKg.toFixed(2)} kg</strong>
          <p>面积 ${result.load.areaSqm.toFixed(2)} m2，面料约 ${result.load.fabricKg.toFixed(2)} kg，已加安全余量。</p>
        </div>
        <div class="result-card">
          <h4>当前电机能力</h4>
          <strong>${esc(result.profile.label)}</strong>
          <p>管径 >= ${result.profile.minTubeMm}mm，宽 <= ${result.profile.maxWidthCm}cm，高 <= ${result.profile.maxHeightCm}cm，负载 <= ${result.profile.maxLoadKg}kg。</p>
        </div>
      </div>
      <div class="data-table-wrap">
        <table class="data-table simulator-table">
          <thead>
            <tr>
              <th>判断项</th>
              <th>当前值</th>
              <th>规则阈值</th>
              <th>命中说明</th>
            </tr>
          </thead>
          <tbody>
            ${[
              ["宽度", `${input.widthCm}cm`, `<= ${result.profile.maxWidthCm}cm`, Number(input.widthCm) > result.profile.maxWidthCm ? "超出当前电机宽度能力" : "通过"],
              ["高度", `${input.heightCm}cm`, `<= ${result.profile.maxHeightCm}cm`, Number(input.heightCm) > result.profile.maxHeightCm ? "超出当前电机高度能力" : "通过"],
              ["面料克重", `${input.fabricWeightGsm}g/m2`, `<= ${result.profile.maxFabricGsm}g/m2`, Number(input.fabricWeightGsm) > result.profile.maxFabricGsm ? "面料过重" : "通过"],
              ["管径", `${input.tubeSizeMm}mm`, `>= ${result.profile.minTubeMm}mm`, Number(input.tubeSizeMm) < result.profile.minTubeMm ? "管径不匹配，禁用当前电机" : "通过"],
              ["估算负载", `${result.load.loadKg.toFixed(2)}kg`, `<= ${result.profile.maxLoadKg}kg`, result.load.loadKg > result.profile.maxLoadKg ? "负载超限" : "通过"],
              ["禁用选项", result.disabledOptions.join(" / ") || "无", "按命中结果动态输出", result.disabledOptions.length ? "当前配置下禁用，不从库里删除" : "无"]
            ].map(row => `<tr>${row.map(cell => `<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}
          </tbody>
        </table>
      </div>
      <div class="rule-expression">
        <strong>规则表达</strong>
        <code>if width_cm / height_cm / fabric_weight_gsm / tube_size_mm / motor_model exceed profile then action</code>
      </div>
    </div>
  `;
}

function motorField(keyName, labelText, value, type = "text") {
  return `
    <div class="field">
      <label>${esc(labelText)}</label>
      <input data-motor-field="${esc(keyName)}" type="${esc(type)}" value="${esc(value)}">
    </div>
  `;
}

function renderOrderRemarkGuidance() {
  return `
    <div class="motor-simulator">
      <div class="simulator-head">
        <div>
          <h3>遥控器分组不放进配置中心</h3>
          <p>配置中心只说明电机方案支持遥控器。多樘窗帘怎么分组控制，让客户在下单备注或订单扩展字段里填写。</p>
        </div>
        <span class="result-pill info">下单处理</span>
      </div>
      <div class="simulator-output">
        <div class="result-card">
          <h4>配置中心管什么</h4>
          <strong>电机支持遥控 / 遥控器可选</strong>
          <p>是否默认带出遥控器、遥控器型号、协议兼容、是否显示下单提示。</p>
        </div>
        <div class="result-card">
          <h4>下单页管什么</h4>
          <strong>客户备注：1-5 用遥控器 A，6-10 用遥控器 B</strong>
          <p>这是订单级说明，不改变单个产品配置模板。</p>
        </div>
        <div class="result-card">
          <h4>为什么不加操作</h4>
          <strong>避免配置中心变成订单系统</strong>
          <p>配置中心保持产品能力定义；跨多行订单的分配关系由订单流程处理。</p>
        </div>
      </div>
      <div class="rule-expression">
        <strong>下单提示文案</strong>
        <code>如需多个窗帘共用多通道遥控器，请备注控制分组，例如：1-5 一组，6-10 一组。</code>
      </div>
    </div>
  `;
}

function renderDynamicRulePanel(p) {
  const rules = dynamicRules(p);
  $("dynamicRulePanel").innerHTML = `
    <div class="data-table-wrap">
      <table class="data-table rule-table">
        <thead>
          <tr>
            <th>规则能力</th>
            <th>来源</th>
            <th>依据/现象</th>
            <th>配置中心应该怎么处理</th>
          </tr>
        </thead>
        <tbody>
          ${ruleAuditRows(p).map(row => `<tr>${row.map(cell => `<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}
        </tbody>
      </table>
    </div>
    <div class="price-rule-grid">
      ${explainCard("超 2m 双电机", "用自动计算规则表达：当 width_cm > 200 时，把 MOTOR_QTY 输出为 2。后续动态物料清单读取这个数量。")}
      ${explainCard("超宽不支持选配", "用禁用选项或禁止组合表达：宽度超过阈值后，某些答案仍在库里，但当前配置下不能选。")}
      ${explainCard("禁止下单 / 仅提醒", "同一个尺寸限制可以设置严重程度：INFO、WARNING、ERROR、BLOCKER。BLOCKER 就不能提交。")}
      ${explainCard("规则执行顺序", "建议固定为：可见性 -> 选项域 -> 默认值 -> 必填 -> 兼容/尺寸 -> 派生用量 -> 价格 -> 动态 BOM -> 发布测试。")}
    </div>
    ${renderMotorCapacitySimulator(p)}
    ${renderOrderRemarkGuidance()}
    <div class="data-table-wrap">
      <table class="data-table rule-table">
        <thead>
          <tr>
            <th>启用</th>
            <th>规则类型</th>
            <th>触发条件</th>
            <th>系统动作</th>
            <th>影响对象</th>
            <th>严重程度</th>
            <th>业务说明</th>
          </tr>
        </thead>
        <tbody>
          ${rules.map(rule => `
            <tr data-rule-id="${esc(rule.id)}">
              <td>
                <select data-rule-field="enabled">
                  ${yesNoOptions(rule.enabled)}
                </select>
              </td>
              <td>
                <select data-rule-field="ruleType">
                  ${ruleTypeOptions(rule.ruleType)}
                </select>
              </td>
              <td><input data-rule-field="condition" value="${esc(rule.condition)}"></td>
              <td><input data-rule-field="action" value="${esc(rule.action)}"></td>
              <td><input data-rule-field="target" value="${esc(rule.target)}"></td>
              <td>
                <select data-rule-field="severity">
                  ${severityOptions(rule.severity)}
                </select>
              </td>
              <td><input data-rule-field="note" value="${esc(rule.note)}"></td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function renderQuestionList(p) {
  $("questionList").innerHTML = slotsForProduct(p).map((slot, index) => {
    const draft = questionDraft(slot, index, p);
    return `
      <button class="question-row ${slot.itemId === state.questionId ? "active" : ""}" type="button" data-question-id="${esc(slot.itemId)}">
        <span class="question-seq">${draft.ordinal}</span>
        <span>
          <h3>${esc(draft.cnName || draft.enName)}</h3>
          <small>${esc(draft.enName)} · 排序 ${esc(draft.sortOrder)} · ${esc(draft.questionCode)}</small>
          <small>${esc(draft.questionCategory)} · ${esc(draft.applyDimension)}</small>
        </span>
        <span class="question-badges">
          <span class="tag ${draft.required ? "danger" : ""}">${draft.required ? "必填" : "可选"}</span>
          <span class="tag accent">${draft.options.length} 答案</span>
        </span>
      </button>
    `;
  }).join("");
}

function renderQuestionEditor(p) {
  const slot = currentQuestion();
  if (!slot) {
    $("questionEditor").innerHTML = `<div class="empty">这个产品还没有配置问题。</div>`;
    return;
  }
  const slots = slotsForProduct(p);
  const index = slots.findIndex(item => item.itemId === slot.itemId);
  const draft = questionDraft(slot, index, p);
  const bindings = p.componentMappings.filter(item => item.itemId === slot.itemId);
  $("questionEditor").innerHTML = `
    <div class="editor-block">
      <h3>问题录入</h3>
      <p class="editor-note">序号是页面里的第几个配置问题；排序值决定发布后显示顺序。两者都显示，ERP 操作员不会只看到 20、30、40。</p>
      <div class="question-form">
        ${field("ordinal", "序号", draft.ordinal, "number")}
        ${field("sortOrder", "排序值", draft.sortOrder, "number")}
        ${field("questionCode", "问题编码", draft.questionCode)}
        ${field("cnName", "中文名称", draft.cnName)}
        ${field("enName", "英文名称", draft.enName)}
        ${selectField("inputType", "填写方式", draft.inputType, [["single_select", "单选"], ["multi_select", "多选"], ["number", "数字"], ["text", "文本"], ["boolean", "是/否"]])}
        ${selectField("required", "是否必填", draft.required ? "Y" : "N", [["Y", "是"], ["N", "否"]])}
        ${selectField("group", "业务分组", draft.group, ["基础信息", "控制/配件方案", "面料/颜色", "结构安装", "配件", "生产细节"])}
        ${selectField("questionCategory", "问题分类", draft.questionCategory, QUESTION_GROUP_OPTIONS)}
        ${selectField("applyLevel", "适用层级", draft.applyLevel, APPLY_LEVEL_OPTIONS)}
        ${field("applyDimension", "适用维度", draft.applyDimension)}
        ${field("optionCount", "当前答案数", draft.options.length, "number", true)}
        ${textarea("purpose", "这个问题做什么 / 后面业务怎么用", draft.purpose)}
      </div>
      <div class="option-toolbar">
        <button class="entry-btn" type="button" data-action="save-question">保存问题</button>
        <button class="entry-btn secondary" type="button" data-action="add-option">新增答案</button>
      </div>
    </div>
    <div class="editor-block">
      <h3>答案录入</h3>
      <p class="editor-note">这里不是只看列表。每个答案都可以维护序号、排序、中文名、英文名、状态，以及选中后自动绑定的组件。</p>
      <div class="option-table-wrap">
        <table class="option-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>排序</th>
              <th>答案编码</th>
              <th>中文名</th>
              <th>英文名</th>
              <th>状态</th>
              <th>选中后绑定组件</th>
            </tr>
          </thead>
          <tbody>
            ${draft.options.map(option => `
              <tr data-option-id="${esc(option.sourceId)}">
                <td><input data-option-field="ordinal" type="number" value="${esc(option.ordinal)}"></td>
                <td><input data-option-field="sortOrder" type="number" value="${esc(option.sortOrder)}"></td>
                <td><input data-option-field="optionCode" value="${esc(option.optionCode)}"></td>
                <td><input data-option-field="cnName" value="${esc(option.cnName)}"></td>
                <td><input data-option-field="enName" value="${esc(option.enName)}"></td>
                <td>
                  <select data-option-field="status">
                    ${optionStatusOptions(option.status)}
                  </select>
                </td>
                <td><input data-option-field="componentName" value="${esc(option.componentName)}" placeholder="例如：蓝牙电机 / Bluetooth Motor"></td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      </div>
      <div class="option-toolbar">
        <button class="entry-btn" type="button" data-action="save-options">保存答案</button>
        <button class="entry-btn secondary" type="button" data-action="add-option">新增答案</button>
      </div>
    </div>
    <div class="editor-block">
      <h3>组件绑定预览</h3>
      <p class="editor-note">这就是“客户选了某个答案，生产自动带什么组件”。没有绑定的答案可以在上面的答案表里补。</p>
      <div class="binding-list">
        ${bindings.length ? bindings.slice(0, 18).map(item => `
          <div class="binding-item">
            <strong>${esc(bilingual(item.optionName, TERM_CN))} -> ${esc(bilingual(item.componentName, COMPONENT_CN))}</strong>
            <small>数量 ${esc(item.quantity || "按规则")} · ${esc(item.kind || "组件绑定")} · 组件编码 C_${esc(item.componentProductId || "待生成")}</small>
          </div>
        `).join("") : `<div class="empty">当前问题还没有组件绑定。可以先保存答案，后续在组件绑定页补规则。</div>`}
      </div>
    </div>
  `;
}

function optionStatusOptions(value) {
  return [["ACTIVE", "启用"], ["DRAFT", "草稿"], ["DISABLED", "停用"]]
    .map(([code, labelText]) => `<option value="${code}" ${code === value ? "selected" : ""}>${labelText}</option>`)
    .join("");
}

function renderOutput(p) {
  const draft = currentProductDraft(p);
  const question = currentQuestion();
  const slots = slotsForProduct(p);
  const qIndex = slots.findIndex(slot => slot.itemId === question?.itemId);
  const qDraft = question ? questionDraft(question, qIndex, p) : null;
  const enabledRules = dynamicRules(p).filter(rule => rule.enabled === "Y");
  $("outputGrid").innerHTML = [
    ["发布模板", `${draft.templateCode} / ${draft.status}`, "报价和订单只能读取已发布模板；草稿保存不影响旧订单。"],
    ["基础资料", `${draft.productNature} / ${draft.stockPolicy}`, "给后期库存、采购、生产和成本留口径，但界面不直接做完整 ERP。"],
    ["问题输出", `${slots.length} 个问题，当前编辑第 ${qDraft?.ordinal || "-"} 项`, "下游页面按序号和排序值展示问题。"],
    ["答案输出", `${draftOptionsCount(p)} 个答案，本地可新增草稿答案`, "答案进入 ConfigOption，可绑定图片、组件、价格影响。"],
    ["组件输出", `${componentCatalog(p).length} 个组件，${p.componentMappings.length} 条答案绑定`, "生产读取组件绑定生成 BOM；库存/采购读取组件物料口径。"],
    ["规则输出", `${enabledRules.length} 条动态规则：超宽、禁选、自动组件、尺寸限制`, "规则命中后输出禁用选项、提醒/blocker、动态物料数量。"],
    ["价格/用量", p.priceSamples.length ? `${p.priceSamples.length} 个价格样本` : "价格待补", "发布前要能通过尺寸、系列、销售变体维度算价。"],
    ["测试上线", releaseState(p), "用真实配置跑问题、答案、价格、组件和快照。"],
    ["保存结构", "ProductSeries / ProductModel / SalesVariant / ConfigQuestionGroup / ConfigQuestion / ConfigOption", "开发可以按这些对象拆表或合表，但业务含义要保留。"]
  ].map(([title, value, desc]) => `
    <div class="output-card">
      <h3>${esc(title)}</h3>
      <strong>${esc(value)}</strong>
      <p>${esc(desc)}</p>
    </div>
  `).join("");
}

function releaseState(p) {
  if (!p.slots.length) return "缺少问题";
  if (!productOptionsCount(p)) return "缺少答案";
  if (!p.priceSamples.length && p.kind !== "标准配件") return "价格待补";
  return "可进入测试";
}

function field(keyName, labelText, value, type = "text", readonly = false) {
  return `
    <div class="field">
      <label>${esc(labelText)}</label>
      <input data-field="${esc(keyName)}" type="${esc(type)}" value="${esc(value)}" ${readonly ? "readonly" : ""}>
    </div>
  `;
}

function textarea(keyName, labelText, value) {
  return `
    <div class="field full">
      <label>${esc(labelText)}</label>
      <textarea data-field="${esc(keyName)}">${esc(value)}</textarea>
    </div>
  `;
}

function selectField(keyName, labelText, value, options) {
  const opts = options.map(option => {
    const pair = Array.isArray(option) ? option : [option, option];
    return `<option value="${esc(pair[0])}" ${String(pair[0]) === String(value) ? "selected" : ""}>${esc(pair[1])}</option>`;
  }).join("");
  return `
    <div class="field">
      <label>${esc(labelText)}</label>
      <select data-field="${esc(keyName)}">${opts}</select>
    </div>
  `;
}

function ensureProductDraft(productId = state.productId) {
  state.drafts[productId] = state.drafts[productId] || {};
  state.drafts[productId].questions = state.drafts[productId].questions || {};
  return state.drafts[productId];
}

function ensureQuestionDraft(productId, questionId) {
  const draft = ensureProductDraft(productId);
  draft.questions[questionId] = draft.questions[questionId] || {};
  draft.questions[questionId].options = draft.questions[questionId].options || {};
  draft.questions[questionId].extraOptions = draft.questions[questionId].extraOptions || [];
  return draft.questions[questionId];
}

function saveProductFromDom(renderAfter = true) {
  const draft = ensureProductDraft();
  document.querySelectorAll("#productForm [data-field]").forEach(input => {
    draft[input.dataset.field] = input.value;
  });
  saveDrafts();
  if (renderAfter) {
    render();
    showToast("产品基础已保存");
  }
}

function saveQuestionFromDom(renderAfter = true) {
  const p = product();
  const question = currentQuestion();
  if (!question) return;
  const savedQuestion = ensureQuestionDraft(p.id, question.itemId);
  document.querySelectorAll(".question-form [data-field]").forEach(input => {
    const fieldName = input.dataset.field;
    if (fieldName === "required") {
      savedQuestion.required = input.value === "Y";
    } else if (fieldName === "ordinal" || fieldName === "sortOrder") {
      savedQuestion[fieldName] = Number(input.value || 0);
    } else if (fieldName !== "optionCount") {
      savedQuestion[fieldName] = input.value;
    }
  });
  saveDrafts();
  if (renderAfter) {
    render();
    showToast("问题已保存");
  }
}

function saveOptionsFromDom(renderAfter = true) {
  const p = product();
  const question = currentQuestion();
  if (!question) return;
  const savedQuestion = ensureQuestionDraft(p.id, question.itemId);
  document.querySelectorAll(".option-table tbody tr").forEach(row => {
    const optionId = row.dataset.optionId;
    const target = optionId.startsWith("NEW_")
      ? savedQuestion.extraOptions.find(item => item.sourceId === optionId)
      : (savedQuestion.options[optionId] = savedQuestion.options[optionId] || {});
    if (!target) return;
    row.querySelectorAll("[data-option-field]").forEach(input => {
      const name = input.dataset.optionField;
      if (name === "ordinal" || name === "sortOrder") {
        target[name] = Number(input.value || 0);
      } else {
        target[name] = input.value;
      }
    });
    target.sourceId = optionId;
  });
  saveDrafts();
  if (renderAfter) {
    render();
    showToast("答案已保存");
  }
}

function saveComponentsFromDom(renderAfter = true) {
  const p = product();
  const draft = ensureProductDraft(p.id);
  draft.components = draft.components || {};
  document.querySelectorAll(".component-table tbody tr").forEach(row => {
    const componentId = row.dataset.componentId;
    draft.components[componentId] = draft.components[componentId] || {};
    row.querySelectorAll("[data-component-field]").forEach(input => {
      draft.components[componentId][input.dataset.componentField] = input.value;
    });
  });
  saveDrafts();
  if (renderAfter) {
    render();
    showToast("组件库已保存");
  }
}

function saveMediaAssetsFromDom(renderAfter = true) {
  const p = product();
  const draft = ensureProductDraft(p.id);
  draft.mediaAssets = draft.mediaAssets || {};
  document.querySelectorAll(".media-table tbody tr").forEach(row => {
    const mediaId = row.dataset.mediaId;
    draft.mediaAssets[mediaId] = draft.mediaAssets[mediaId] || {};
    row.querySelectorAll("[data-media-field]").forEach(input => {
      draft.mediaAssets[mediaId][input.dataset.mediaField] = input.value;
    });
  });
  saveDrafts();
  if (renderAfter) {
    render();
    showToast("资料绑定已保存");
  }
}

function saveDynamicRulesFromDom(renderAfter = true) {
  const p = product();
  const draft = ensureProductDraft(p.id);
  draft.dynamicRules = draft.dynamicRules || {};
  document.querySelectorAll(".rule-table tbody tr[data-rule-id]").forEach(row => {
    const ruleId = row.dataset.ruleId;
    draft.dynamicRules[ruleId] = draft.dynamicRules[ruleId] || {};
    row.querySelectorAll("[data-rule-field]").forEach(input => {
      draft.dynamicRules[ruleId][input.dataset.ruleField] = input.value;
    });
  });
  saveDrafts();
  if (renderAfter) {
    render();
    showToast("动态规则已保存");
  }
}

function addOption() {
  const p = product();
  const question = currentQuestion();
  if (!question) return;
  const savedQuestion = ensureQuestionDraft(p.id, question.itemId);
  const slots = slotsForProduct(p);
  const currentCount = questionDraft(question, slots.findIndex(slot => slot.itemId === question.itemId), p).options.length;
  const id = `NEW_${Date.now()}`;
  savedQuestion.extraOptions.push({
    sourceId: id,
    ordinal: currentCount + 1,
    sortOrder: (currentCount + 1) * 10,
    optionCode: `A_${question.itemId}_NEW_${currentCount + 1}`,
    cnName: "新答案",
    enName: "New Option",
    status: "DRAFT",
    componentName: ""
  });
  saveDrafts();
  render();
  showToast("已新增答案草稿");
}

function addQuestion() {
  const p = product();
  const draft = ensureProductDraft(p.id);
  const slots = slotsForProduct(p);
  const id = `NEW_Q_${Date.now()}`;
  draft.questions[id] = {
    sourceId: id,
    ordinal: slots.length + 1,
    sortOrder: (slots.length + 1) * 10,
    questionCode: `Q_NEW_${slots.length + 1}`,
    cnName: "新配置问题",
    enName: "New Config Question",
    inputType: "single_select",
    required: false,
    group: "基础信息",
    purpose: "新增问题草稿，保存后开发可落到 ConfigQuestion。",
    options: {},
    extraOptions: []
  };
  state.questionId = id;
  saveDrafts();
  render();
  showToast("已新增问题草稿");
}

function resetProductDraft() {
  const p = product();
  if (!window.confirm(`重置 ${p.name} 的本地配置草稿？`)) return;
  delete state.drafts[p.id];
  saveDrafts();
  render();
  showToast("当前产品草稿已重置");
}

function updateSavedState() {
  const count = Object.keys(state.drafts).length;
  $("savedState").textContent = `本地草稿 ${count}`;
}

function showToast(message) {
  const toast = $("toast");
  toast.textContent = message;
  toast.classList.add("show");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => toast.classList.remove("show"), 1600);
}

function applyNavState() {
  document.body.classList.toggle("nav-collapsed", state.navCollapsed);
  $("toggleNavBtn").textContent = state.navCollapsed ? "展开产品列表" : "收起产品列表";
}

document.addEventListener("click", event => {
  const productButton = event.target.closest("[data-product-id]");
  if (productButton) {
    state.productId = productButton.dataset.productId;
    state.questionId = "";
    state.motorRuleInput = null;
    render();
    return;
  }

  const questionButton = event.target.closest("[data-question-id]");
  if (questionButton) {
    state.questionId = questionButton.dataset.questionId;
    render();
    return;
  }

  const moduleButton = event.target.closest("[data-module-open]");
  if (moduleButton) {
    state.activeSharedModule = moduleButton.dataset.moduleOpen;
    renderSharedModuleLinks(product());
    renderSharedModuleDetail(product());
    return;
  }

  if (event.target.closest("[data-module-close]")) {
    state.activeSharedModule = "";
    renderSharedModuleLinks(product());
    renderSharedModuleDetail(product());
    return;
  }

  const action = event.target.closest("[data-action]")?.dataset.action;
  if (action === "save-product") saveProductFromDom();
  if (action === "save-question") saveQuestionFromDom();
  if (action === "save-options") saveOptionsFromDom();
  if (action === "save-components") saveComponentsFromDom();
  if (action === "save-media-assets") saveMediaAssetsFromDom();
  if (action === "save-dynamic-rules") saveDynamicRulesFromDom();
  if (action === "add-option") addOption();
});

function updateMotorRuleInputFromDom() {
  const panel = document.getElementById("motorRuleSimulator");
  if (!panel) return;
  state.motorRuleInput = { ...motorRuleInput(product()) };
  panel.querySelectorAll("[data-motor-field]").forEach(input => {
    const name = input.dataset.motorField;
    state.motorRuleInput[name] = name === "motorModel" ? input.value : Number(input.value || 0);
  });
  renderDynamicRulePanel(product());
  renderOutput(product());
}

document.addEventListener("input", event => {
  if (event.target.closest("#motorRuleSimulator")) updateMotorRuleInputFromDom();
});

document.addEventListener("change", event => {
  if (event.target.closest("#motorRuleSimulator")) updateMotorRuleInputFromDom();
});

$("productSearch").addEventListener("input", event => {
  state.search = event.target.value;
  renderProductList();
});

$("addQuestionBtn").addEventListener("click", addQuestion);
$("saveAllBtn").addEventListener("click", () => {
  saveProductFromDom(false);
  saveQuestionFromDom(false);
  saveOptionsFromDom(false);
  saveMediaAssetsFromDom(false);
  saveDynamicRulesFromDom(false);
  render();
  showToast("当前配置草稿已保存");
});
$("resetProductBtn").addEventListener("click", resetProductDraft);
$("toggleNavBtn").addEventListener("click", () => {
  state.navCollapsed = !state.navCollapsed;
  localStorage.setItem("skyspf-config-center-nav-collapsed", state.navCollapsed ? "1" : "0");
  applyNavState();
});

applyNavState();
render();
