const DATA = window.CONFIG_CENTER_PROTOTYPE_DATA || { generatedAt: "", products: [] };
const STORAGE_KEY = "skyspf-config-center-complete-prototype";

const MODULES = [
  ["config", "配置中心"],
  ["overview", "业务验证"],
  ["model", "产品档案"],
  ["offering", "销售商品"],
  ["template", "下单模板"],
  ["slots", "下单问题"],
  ["rules", "业务规则"],
  ["price", "价格设置"],
  ["usage", "用量计算"],
  ["bom", "生产配件"],
  ["content", "图文资料"],
  ["testing", "测试上线"],
  ["order", "模拟订单"],
  ["snapshot", "订单留档"]
];

const STAGES = [
  ["建产品系列", "先定义柔纱帘、卷帘、蜂巢帘这类产品族"],
  ["建可售产品", "在系列下创建具体产品，如 L Cassette Motor Zebra Shades"],
  ["建组件库", "准备电机、面料、罩壳、底杆、太阳能板等可被引用的组件"],
  ["配置问题", "决定配置员要维护哪些问题和答案，不是先从下单页面开始"],
  ["绑定组件", "把某个答案和真实组件关联起来，例如选蓝牙方案带蓝牙电机"],
  ["配置规则", "只维护系统必须自动判断的逻辑，禁止组合只是其中一种"],
  ["配置价格", "维护尺寸、系列、控制方式对应的价格矩阵"],
  ["测试发布", "用真实配置跑一遍，确认可保存、可报价、可生成组件"]
];

const DEFAULT_PRODUCT_ID = DATA.products.find(item => item.name === "L Cassette Motor Zebra Shades")?.id || DATA.products[0]?.id || "";
const FEATURED_PRODUCT_NAMES = [
  "L Cassette Motor Zebra Shades",
  "Flat rail Motor Screen View Shades",
  "L Cassette Chain Blackout shades",
  "Standard Chain Roman Shades"
];

const state = {
  module: "config",
  productId: DEFAULT_PRODUCT_ID,
  search: "",
  selections: {},
  order: {
    width: 85,
    height: 90,
    quantity: 1,
    room: "Living Room / Window 1"
  },
  store: loadStore()
};

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

function raw(value) {
  return { __html: String(value ?? "") };
}

function clean(value) {
  return String(value ?? "").replace(/\s+/g, " ").trim();
}

function money(value, currency = "USD") {
  const amount = Number(value || 0);
  return `${currency === "USD" ? "$" : `${currency} `}${amount.toFixed(2)}`;
}

function inchToCm(value) {
  return Number((Number(value || 0) * 2.54).toFixed(2));
}

function slug(value) {
  return clean(value).toUpperCase().replace(/[^A-Z0-9]+/g, "_").replace(/^_|_$/g, "");
}

function codePart(value, fallback = "ITEM") {
  return slug(value) || fallback;
}

function labelKey(value) {
  return clean(value)
    .replaceAll("（", "(")
    .replaceAll("）", ")")
    .replaceAll("：", ":")
    .replace(/\s*\(\s*/g, "(")
    .replace(/\s*\)\s*/g, ")")
    .replace(/\s*--\s*/g, "--")
    .toLowerCase();
}

const SLOT_CN = Object.fromEntries([
  ["Basswood Fabric Type", "椴木百叶材质"],
  ["Blackout Shades Fabric Type", "遮光卷帘面料"],
  ["Bottom Rail", "底杆"],
  ["Bottom Rail Color", "底杆颜色"],
  ["Bottom Rail Color(Cellular Shades)", "蜂巢帘底杆颜色"],
  ["Cassette Color", "罩壳颜色"],
  ["Cassette Fabric Covering", "罩壳是否包布"],
  ["Cellular Shades Fabric Type", "蜂巢帘面料"],
  ["Chain Length", "拉珠/链条长度"],
  ["Control/Motor Direction", "控制/电机方向"],
  ["Dream Curtain Fabric Type", "梦幻帘面料"],
  ["Edge Binding", "包边"],
  ["Fabric Kinds", "面料类型"],
  ["Fabric Roll Type", "卷布方向"],
  ["Lining", "衬布"],
  ["Model Configuration", "控制方案"],
  ["Model Configuration(Chain Operated)", "拉珠控制方案"],
  ["Model Configuration(Honeycomb Shade Motor)", "蜂巢帘电机方案"],
  ["Model Configuration(Lithium Battery Motor)", "锂电池电机方案"],
  ["Mount Position", "安装方式"],
  ["Percentage1", "透光率"],
  ["Roman Shades Fabric Type", "罗马帘面料"],
  ["Screen View Shades Fabric Type", "透景帘面料"],
  ["Shape Side Channels", "侧轨形状"],
  ["Shade Side Channels", "侧轨形状"],
  ["Solar Panel", "太阳能板"],
  ["Solar Shades Fabric Type", "阳光卷帘面料"],
  ["Telescoping pole", "伸缩杆"],
  ["Zebra Shades Fabric Type", "柔纱帘面料"]
].map(([key, value]) => [labelKey(key), value]));

const TERM_CN = Object.fromEntries([
  ["APP+Remote(Bluetooth Motor)", "APP + 遥控器（蓝牙电机）"],
  ["APP+Voice+Remote(Zigbee)", "APP + 语音 + 遥控器（Zigbee）"],
  ["APP+Voice+Remote(Matter Motor:Special Speaker Mode)", "APP + 语音 + 遥控器（Matter 电机：特殊音箱模式）"],
  ["Ceiling installation with inside", "顶装 + 内装"],
  ["Ceiling installation with outside", "顶装 + 外装"],
  ["Side installation with inside", "侧装 + 内装"],
  ["Side installation with outside", "侧装 + 外装"],
  ["Centre opening", "中开"],
  ["Left stack", "左堆叠"],
  ["Right stack", "右堆叠"],
  ["Chain Operated(Beige plastic)", "拉珠控制（米色塑料）"],
  ["Chain Operated(Black plastic)", "拉珠控制（黑色塑料）"],
  ["Chain Operated(Brown plastic)", "拉珠控制（棕色塑料）"],
  ["Chain Operated(Grey plastic)", "拉珠控制（灰色塑料）"],
  ["Chain Operated(Stainless steel)", "拉珠控制（不锈钢）"],
  ["Chain Operated(White plastic)", "拉珠控制（白色塑料）"],
  ["Day and Night Cellular Motor", "日夜蜂巢帘电机"],
  ["Normal Cellular Motor", "普通蜂巢帘电机"],
  ["TDBU Cellular Motor", "上下双开蜂巢帘电机"],
  ["Remote Control Only(Remote Motor)", "仅遥控器（遥控电机）"],
  ["L shape side channel", "L 型侧轨"],
  ["U shape side channel", "U 型侧轨"],
  ["Ladder Cord", "梯绳"],
  ["Ladder Tape", "梯带"],
  ["Fabric wrap", "包布"],
  ["Farbric wrap", "包布"],
  ["No Fabric wrap", "不包布"],
  ["No fabric warp", "不包布"],
  ["Solar Panel", "加太阳能板"],
  ["No Solar Panel", "不加太阳能板"],
  ["Telescoping pole", "加伸缩杆"],
  ["No Telescoping pole", "不加伸缩杆"],
  ["Inside", "内装"],
  ["Outside", "外装"],
  ["Left", "左侧"],
  ["Right", "右侧"],
  ["Standard", "标准卷"],
  ["Reverse", "反卷"],
  ["One kind", "一种面料"],
  ["Two kinds", "两种面料"],
  ["Yes", "是"],
  ["No", "否"],
  ["White", "白色"],
  ["Black", "黑色"],
  ["Grey", "灰色"],
  ["Gray", "灰色"],
  ["Light Grey", "浅灰色"],
  ["Light Gray", "浅灰色"],
  ["Dark Grey", "深灰色"],
  ["Dark Gray", "深灰色"],
  ["Night Black", "夜黑"],
  ["Pearl White", "珍珠白"],
  ["Iron Gray", "铁灰"],
  ["Sand Beige", "沙米色"],
  ["Beige", "米色"],
  ["Khaki", "卡其色"],
  ["Dark Khaki", "深卡其"],
  ["Light Khaki", "浅卡其"],
  ["Brown", "棕色"],
  ["Light Brown", "浅棕色"],
  ["Medium Brown", "中棕色"],
  ["Coffee", "咖啡色"],
  ["Coffee Brown", "咖啡棕"],
  ["Chocolate", "巧克力色"],
  ["Cream", "米白色"],
  ["Ivory", "象牙白"],
  ["Off-white", "米白色"],
  ["Snow White", "雪白"],
  ["White Grey", "白灰色"],
  ["White Sand", "沙白色"],
  ["White Sandstone", "沙白石色"],
  ["White Slate", "白石纹"],
  ["Bronze", "古铜色"],
  ["Dark Bronze", "深古铜色"],
  ["Silver", "银色"],
  ["Silver Mist", "银雾色"],
  ["Blue", "蓝色"],
  ["Lake Blue", "湖蓝"],
  ["Sky Blue", "天蓝"],
  ["Sea Blue", "海蓝"],
  ["Ash Blue", "灰蓝"],
  ["Obsidian Blue", "黑曜蓝"],
  ["Pink", "粉色"],
  ["Light pink", "浅粉色"],
  ["Dark Pink", "深粉色"],
  ["Red", "红色"],
  ["Crimson", "深红色"],
  ["Green", "绿色"],
  ["Medium Green", "中绿色"],
  ["Light Green", "浅绿色"],
  ["Avocado Green", "牛油果绿"],
  ["Cyan Green", "青绿色"],
  ["Goose Yellow", "鹅黄色"],
  ["Cream Yellow", "奶油黄"],
  ["Fog Gray", "雾灰"],
  ["Glacier Gray", "冰川灰"],
  ["Nebula Purple", "星云紫"],
  ["Rich Brown", "深棕色"],
  ["Latte", "拿铁色"],
  ["Linen", "亚麻色"],
  ["Beige Linen", "米色亚麻"],
  ["White Linen", "白色亚麻"],
  ["Latte Linen", "拿铁亚麻"],
  ["Coffee Linen", "咖啡亚麻"],
  ["Tan", "棕褐色"],
  ["Plain White", "纯白"],
  ["White Stripes", "白色条纹"],
  ["Cream With White Stripes", "奶油白条纹"],
  ["Cream With Linen Stripes", "奶油亚麻条纹"],
  ["Cream With Grey Stripes", "奶油灰条纹"],
  ["Cream With Black Stripes", "奶油黑条纹"],
  ["Cream-White", "奶油白"],
  ["Maple", "枫木色"],
  ["Pecan", "山核桃木色"],
  ["Oak", "橡木色"],
  ["Rose wood", "玫瑰木色"],
  ["Marble", "大理石纹"],
  ["Walnut", "胡桃木色"],
  ["Zebrano", "斑马木色"],
  ["Natural", "原木色"],
  ["Teak", "柚木色"],
  ["Golden Oak", "金橡木色"],
  ["Cherry", "樱桃木色"],
  ["Hybrid", "混合纹"],
  ["Cube", "方格纹"],
  ["Pinstripe", "细条纹"],
  ["Wide Stripe", "宽条纹"]
].map(([key, value]) => [labelKey(key), value]));

const COMPONENT_CN = Object.fromEntries([
  ["Bluetooth Motor", "蓝牙电机"],
  ["Remote Motor", "遥控电机"],
  ["Matter Motor:Special Speaker Mode", "Matter 电机：特殊音箱模式"],
  ["Zigbee", "Zigbee 电机"],
  ["Solar Panel", "太阳能板"],
  ["Telescoping pole", "伸缩杆"],
  ["Cassette Fabric Covering", "罩壳包布"],
  ["Farbric wrap", "包布"]
].map(([key, value]) => [labelKey(key), value]));

function translateLabel(value, dictionary = {}) {
  const text = clean(value);
  if (!text) return "";
  const direct = dictionary[labelKey(text)] || TERM_CN[labelKey(text)];
  if (direct) return direct;
  const coded = text.match(/^(.+?)--\s*(.+)$/);
  if (coded) {
    const code = clean(coded[1]);
    const name = clean(coded[2]);
    const translatedName = TERM_CN[labelKey(name)];
    return translatedName ? `${code} - ${translatedName}` : "";
  }
  return "";
}

function labelParts(value, dictionary = {}) {
  const en = clean(value);
  const cn = translateLabel(en, dictionary);
  return { cn: cn && cn !== en ? cn : "", en };
}

function bilingualHtml(value, dictionary = {}) {
  const parts = labelParts(value, dictionary);
  if (!parts.en) return "";
  return parts.cn ? `${esc(parts.cn)}<small>${esc(parts.en)}</small>` : esc(parts.en);
}

function bilingualText(value, dictionary = {}, separator = " / ") {
  const parts = labelParts(value, dictionary);
  if (!parts.en) return "";
  return parts.cn ? `${parts.cn}${separator}${parts.en}` : parts.en;
}

function primaryText(value, dictionary = {}) {
  const parts = labelParts(value, dictionary);
  return parts.cn || parts.en;
}

function optionText(option) {
  const name = clean(option?.name || "");
  const description = clean(option?.description || "");
  if (description && description !== name && description.includes("--")) {
    return bilingualText(description, TERM_CN);
  }
  return bilingualText(name || description, TERM_CN);
}

function loadStore() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || { drafts: {}, snapshots: [], rules: {}, tests: {} };
  } catch {
    return { drafts: {}, snapshots: [], rules: {}, tests: {} };
  }
}

function saveStore() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state.store));
  updateSavedPill();
}

function product() {
  return DATA.products.find(item => item.id === state.productId) || DATA.products[0];
}

function allOptionsCount(p) {
  return p.slots.reduce((sum, slot) => sum + slot.options.length, 0);
}

function mandatoryCount(p) {
  return p.slots.filter(slot => slot.mandatory === "Y").length;
}

function productScore(p) {
  return allOptionsCount(p) + p.slots.length * 10 + p.componentMappings.length + (p.priceSamples.length ? 20 : 0);
}

function configProducts(activeProduct) {
  const ranked = [...DATA.products].sort((a, b) => productScore(b) - productScore(a));
  const list = [activeProduct, ...FEATURED_PRODUCT_NAMES.map(name => DATA.products.find(item => item.name === name)), ...ranked];
  const seen = new Set();
  return list.filter(item => {
    if (!item || seen.has(item.id)) return false;
    seen.add(item.id);
    return true;
  }).slice(0, 4);
}

function sampleOptions(slot, limit = 6) {
  const names = slot.options.slice(0, limit).map(option => optionText(option));
  return `${names.join(" / ")}${slot.options.length > limit ? " / ..." : ""}`;
}

function optionCloud(slot, limit = 999) {
  const options = slot.options.slice(0, limit);
  const more = slot.options.length > limit ? `<span class="option-chip muted">还有 ${slot.options.length - limit} 个</span>` : "";
  return `<div class="option-cloud">${options.map(option => `<span class="option-chip">${esc(optionText(option))}</span>`).join("")}${more}</div>`;
}

function productSampleCard(item, index) {
  return `
    <article class="product-sample">
      <span class="sample-index">${index + 1}</span>
      <h3>${esc(item.name)}</h3>
      <p>${esc(item.kind)} / ${esc(item.control)}</p>
      <div class="meta-row">
        <span class="tag accent">${item.slots.length} 问题</span>
        <span class="tag">${allOptionsCount(item)} 选项</span>
        <span class="tag">${item.componentMappings.length} 组件绑定</span>
        <span class="tag ${item.priceSamples.length ? "ok" : "warn"}">${item.priceSamples.length ? `${item.priceSamples.length} 价格档` : "价格待补"}</span>
      </div>
    </article>
  `;
}

function configSetupRows(p) {
  const price = p.priceSummary || {};
  return [
    ["产品系列 ProductSeries", p.kind, "先把柔纱帘、景观帘、遮光卷帘这类产品族建出来。后面产品、模板、价格都挂在系列下。", `seriesCode=${slug(p.kind)}`],
    ["可售产品 ConfigurableProduct", p.name, "客户或销售最终选择的是这个商品入口。它决定使用哪个模板、价格模式和上线状态。", `productCode=P_${p.id}`],
    ["组件库 Component", `${new Set(p.componentMappings.map(item => clean(item.componentName)).filter(Boolean)).size} 个组件/物料引用`, "先把电机、面料、罩壳、底杆、太阳能板等组件准备好，后面由答案自动绑定。", "ComponentCatalog"],
    ["配置模板 ConfigurationTemplate", `${p.slots.length} 个问题，${allOptionsCount(p)} 个答案`, "定义这个产品要问什么、顺序是什么、哪些必填、答案从哪里来。", `templateCode=TPL_${p.id}_DRAFT`],
    ["问题/选项 ConfigQuestion + ConfigOption", p.slots.map(slot => primaryText(slot.name, SLOT_CN)).slice(0, 4).join("、"), "问题是配置员维护的表单结构；选项是每个问题的答案库。", "ConfigQuestion / ConfigOption"],
    ["组件绑定 ComponentBinding", `${p.componentMappings.length} 条答案到组件的绑定`, "不是销售手动勾 BOM，而是选择某个答案后系统自动带出组件。", "answer -> component"],
    ["规则 RuleSet", `${mandatoryCount(p)} 个必填判断，${p.dimensionLimits.length} 个尺寸限制`, "规则只维护系统必须自动判断的逻辑。禁止组合只是其中一种，不是每个产品都要配。", "ConfigRule"],
    ["价格/用量 Price + Usage", p.priceSamples.length ? `${price.mode}，${price.rows || p.priceSamples.length} 个价格档` : "价格待补", "价格按矩阵、面积、用量或固定价维护；用量给价格和生产共用。", "PriceMatrix / UsageModel"],
    ["测试上线 Publish", releaseChecks(p).every(item => item.pass) ? "可进入审核" : "需要补齐后测试", "用真实配置跑下单、价格、组件、快照，测试通过才发布给报价/订单。", "Draft -> Testing -> Published"]
  ];
}

function questionRows(p) {
  return p.slots.map(slot => [
    slot.sequence || "",
    raw(`${bilingualHtml(slot.name, SLOT_CN)}<small>问题编码 Q_${esc(slot.itemId)}</small>`),
    slot.type === "SINGLE" ? "单选" : slot.type || "单选",
    raw(slot.mandatory === "Y" ? badge("必填", "danger") : badge("可选", "")),
    slot.options.length,
    raw(optionCloud(slot, slot.options.length > 16 ? 16 : 999)),
    "ConfigQuestion / ConfigOption"
  ]);
}

function productQuestionBlock(item) {
  return `
    <div class="sample-detail">
      <div class="sample-detail-head">
        <h3>${esc(item.name)}</h3>
        <span class="tag accent">${item.slots.length} 问题 / ${allOptionsCount(item)} 选项</span>
      </div>
      <div class="table-wrap compact">${table(["顺序", "配置问题", "必填", "选项数", "答案预览"], item.slots.map(slot => [
        slot.sequence || "",
        raw(bilingualHtml(slot.name, SLOT_CN)),
        slot.mandatory === "Y" ? "是" : "否",
        slot.options.length,
        sampleOptions(slot, 5)
      ]))}</div>
    </div>
  `;
}

function componentBindingRows(p, limit = 80) {
  return p.componentMappings.slice(0, limit).map(item => [
    raw(bilingualHtml(item.itemName, SLOT_CN)),
    bilingualText(item.optionName, TERM_CN),
    raw(`${bilingualHtml(item.componentName, COMPONENT_CN)}<small>组件编码 C_${esc(item.componentProductId || "待生成")}</small>`),
    item.quantity || "按规则",
    item.kind === "自动配件" ? "选择后加入生产组件" : "答案映射到组件/物料"
  ]);
}

function ruleGuideRows(p) {
  return [
    ["显示/隐藏问题", "当前面选择会决定后面问题是否出现", "比如手拉产品显示链条颜色，电动产品显示电机方案。", "按依赖关系自动重算可见问题"],
    ["筛选答案", "同一个问题的答案不是永远都能选", "比如某个面料系列只允许部分颜色或侧轨。", "重算 ConfigOption 的可选范围"],
    ["必填", "当前可见问题必须填写", `${p.name} 当前有 ${mandatoryCount(p)} 个必填问题。`, "不填不能发布配置或提交订单"],
    ["禁止组合", "两个答案明确不能同时出现时才用", "例如某面料不能配某罩壳颜色。当前样本未抽到显式禁止组合，不需要为了有规则而硬配。", "命中后拦截或提示"],
    ["尺寸限制", "宽高、面积、控制方式超出产品能力", p.dimensionLimits.length ? `当前有 ${p.dimensionLimits.length} 条尺寸限制可演示。` : "当前样本没有明确尺寸限制，发布前仍要确认资料。", "输出 WARNING / BLOCKER"],
    ["自动计算", "根据尺寸或选择生成系统值", "比如宽度超过阈值时电机数量从 1 变 2。", "输出派生值给价格和 BOM"],
    ["自动带配件", "选择答案后生成生产组件", `${p.componentMappings.length} 条绑定会把答案转成组件。`, "输出 ComponentBinding / BOM"],
    ["价格/用量", "算价和生产共用同一套配置结果", p.priceSamples.length ? `当前有 ${p.priceSamples.length} 条价格样本。` : "当前价格待补，不能直接发布。", "输出报价和配置快照"]
  ];
}

function configStoragePreview(p) {
  const components = [...new Map(p.componentMappings
    .filter(item => clean(item.componentName))
    .map(item => [clean(item.componentName), item])).values()].slice(0, 8);
  return {
    ProductSeries: {
      seriesCode: codePart(p.kind, `SERIES_${p.id}`),
      seriesName: p.kind,
      defaultUnit: "inch display / cm engine"
    },
    ConfigurableProduct: {
      productCode: `P_${p.id}`,
      productName: p.name,
      seriesCode: codePart(p.kind, `SERIES_${p.id}`),
      saleMode: p.kind === "标准配件" ? "STANDARD_SKU" : "CUSTOM_CONFIGURED",
      status: "DRAFT"
    },
    ConfigurationTemplate: {
      templateCode: `TPL_${p.id}_2026_06_DRAFT`,
      questionCount: p.slots.length,
      optionCount: allOptionsCount(p),
      versionStatus: "DRAFT"
    },
    ConfigQuestions: p.slots.map(slot => ({
      questionCode: `Q_${slot.itemId}`,
      name: bilingualText(slot.name, SLOT_CN),
      inputType: slot.type === "SINGLE" ? "single_select" : clean(slot.type || "single_select"),
      required: slot.mandatory === "Y",
      sortOrder: Number(slot.sequence || 0),
      optionCount: slot.options.length,
      optionExamples: slot.options.slice(0, 3).map(option => optionText(option))
    })),
    ComponentCatalog: components.map(item => ({
      componentCode: `C_${item.componentProductId || slug(item.componentName)}`,
      componentName: bilingualText(item.componentName, COMPONENT_CN)
    })),
    ComponentBindings: p.componentMappings.slice(0, 8).map(item => ({
      when: `${bilingualText(item.itemName, SLOT_CN)} = ${bilingualText(item.optionName, TERM_CN)}`,
      addComponent: bilingualText(item.componentName, COMPONENT_CN),
      quantity: Number(item.quantity || 0) || "by_rule"
    })),
    RuleSet: {
      requiredQuestions: mandatoryCount(p),
      forbidCombinations: [],
      dimensionLimits: p.dimensionLimits.length,
      autoComponentBindings: p.componentMappings.length,
      note: "禁止组合只有业务明确不兼容时才新增；当前样本重点是必填、选项、价格、组件绑定。"
    },
    PriceAndUsage: {
      priceMode: p.priceSummary?.mode || "待维护",
      priceRows: Number(p.priceSummary?.rows || p.priceSamples.length || 0),
      usageModels: usageModels(p).map(item => item.code)
    },
    PublishOutput: {
      visibleQuestions: "for sales/order UI",
      enabledOptions: "for valid selections",
      validationMessages: "for warnings/blockers",
      priceResult: "for quote/order",
      generatedComponents: "for production",
      configurationSnapshot: "for history"
    }
  };
}

function allPhysicalMappings(p) {
  return p.componentMappings.filter(item => Number(item.quantity || 0) > 0 || /motor|solar|chain|cord|rail|remote|wrap|zigbee|matter/i.test(item.componentName));
}

function currentDraft(p = product()) {
  const base = defaultDraft(p);
  return { ...base, ...(state.store.drafts[p.id] || {}) };
}

function defaultDraft(p) {
  return {
    modelCode: codePart(p.kind || p.name, `MODEL_${p.id}`),
    offeringCode: `${codePart(p.name, `PRODUCT_${p.id}`)}_${p.id}`,
    templateVersion: `${codePart(p.name, `PRODUCT_${p.id}`)}_2026_06_DRAFT`,
    saleMode: p.kind === "标准配件" ? "STANDARD_SKU" : "CUSTOM_CONFIGURED",
    displayUnit: "inch",
    internalUnit: "cm",
    sourceUnit: "inch",
    helpText: `${p.kind} 下单时先确认尺寸、控制方式、面料/颜色、安装方式和配件选项。`,
    installText: "Inside 测量窗框内宽高；Outside 确认覆盖范围。系统内部换算为 cm 后参与规则求值。",
    internalNote: `当前产品有 ${p.slots.length} 个下单问题、${allOptionsCount(p)} 个可选答案、${p.componentMappings.length} 条自动带出关系。`,
    publishNote: "草稿可保存；上线前必须通过模拟下单、价格检查、配件检查、订单留档检查。"
  };
}

function initSelections(p) {
  const existing = state.selections[p.id] || {};
  const next = {};
  for (const slot of p.slots) {
    next[slot.itemId] = existing[slot.itemId] || slot.options[0]?.optionId || "";
  }
  state.selections[p.id] = next;
}

function selectedOption(slot, p = product()) {
  const selectedId = state.selections[p.id]?.[slot.itemId];
  return slot.options.find(option => option.optionId === selectedId) || slot.options[0] || null;
}

function selectedMappings(p) {
  const selections = state.selections[p.id] || {};
  return p.componentMappings.filter(item => selections[item.itemId] === item.optionId);
}

function adapterMode(p) {
  if (p.kind === "标准配件") return "固定SKU价格";
  if (p.priceSummary.mode?.includes("Ladder")) return "按梯带/梯绳价格表";
  if (p.priceSummary.mode?.includes("分类")) return "按尺寸矩阵价格表";
  return "待维护价格规则";
}

function nearestPriceSample(p) {
  if (p.kind === "标准配件") return null;
  const width = Number(state.order.width || 0);
  const height = Number(state.order.height || 0);
  let samples = p.priceSamples || [];
  const selectedNames = Object.values(state.selections[p.id] || {})
    .map(optionId => p.slots.flatMap(slot => slot.options).find(option => option.optionId === optionId))
    .map(option => clean(option?.name));
  const ladder = selectedNames.find(name => name === "Ladder Tape" || name === "Ladder Cord");
  if (ladder) samples = samples.filter(sample => sample.modelConfigurationName === ladder);
  if (!samples.length) return null;
  return samples.reduce((best, sample) => {
    const score = Math.abs(Number(sample.width) - width) + Math.abs(Number(sample.height) - height);
    const bestScore = Math.abs(Number(best.width) - width) + Math.abs(Number(best.height) - height);
    return score < bestScore ? sample : best;
  }, samples[0]);
}

function usageModels(p) {
  const widthIn = Number(state.order.width || 0);
  const heightIn = Number(state.order.height || 0);
  const widthCm = inchToCm(widthIn);
  const heightCm = inchToCm(heightIn);
  const areaSqm = Number(((widthCm * heightCm) / 10000).toFixed(2));
  const fabricArea = Number((areaSqm * 1.08).toFixed(2));
  const profileLength = Math.max(0, Number((widthCm - 2).toFixed(2)));
  const chainLength = Number((heightCm * 1.15).toFixed(2));
  const motorQty = p.control === "电动" && widthIn > 157 ? 2 : p.control === "电动" ? 1 : 0;
  return [
    { code: "FABRIC_AREA", method: "BY_AREA", result: fabricArea, uom: "m2", params: "width_cm * height_cm / 10000 * 1.08" },
    { code: "PROFILE_LENGTH", method: "BY_WIDTH", result: profileLength, uom: "cm", params: "width_cm - 2" },
    { code: "CHAIN_LENGTH", method: "BY_HEIGHT", result: chainLength, uom: "cm", params: "height_cm * 1.15" },
    { code: "MOTOR_QTY", method: "THRESHOLD", result: motorQty, uom: "pcs", params: "electric && width_in > 157 ? 2 : 1" }
  ];
}

function autoBom(p) {
  const components = [];
  for (const item of selectedMappings(p)) {
    const qty = Number(item.quantity || 0);
    const name = clean(item.componentName);
    if (!name) continue;
    const physical = qty > 0 || /motor|solar panel|chain|cord|fabric wrap|bottom rail|remote|zigbee|matter/i.test(name);
    if (!physical) continue;
    components.push({
      component: name,
      productId: item.componentProductId,
      qty: qty > 0 ? qty : 1,
      source: clean(item.itemName),
      option: clean(item.optionName),
      usageCode: qty > 0 ? "FIXED" : "OPTION_MAPPING",
      note: qty > 0 ? "固定数量" : "按选择自动带出"
    });
  }
  const motorUsage = usageModels(p).find(item => item.code === "MOTOR_QTY");
  if (motorUsage?.result > 1) {
    const motor = components.find(item => /motor/i.test(item.component));
    if (motor) {
      motor.qty = Math.max(motor.qty, motorUsage.result);
      motor.usageCode = "MOTOR_QTY";
      motor.note = "超宽规则调整";
    } else {
      components.push({
        component: "Motor",
        productId: "",
        qty: motorUsage.result,
        source: "尺寸规则",
        option: "width > 157 inch",
        usageCode: "MOTOR_QTY",
        note: "原型规则"
      });
    }
  }
  return components;
}

function validations(p) {
  const out = [];
  const width = Number(state.order.width || 0);
  const height = Number(state.order.height || 0);
  for (const slot of p.slots) {
    if (slot.mandatory === "Y" && !state.selections[p.id]?.[slot.itemId]) {
      out.push({ level: "block", text: `${primaryText(slot.name, SLOT_CN)} 是必填项` });
    }
  }
  if (width <= 0 || height <= 0) out.push({ level: "block", text: "宽高必须大于 0" });
  for (const limit of p.dimensionLimits || []) {
    const maxW = Number(limit.maxWidth || 0);
    const maxH = Number(limit.maxHeight || 0);
    if ((maxW && width > maxW) || (maxH && height > maxH)) {
      out.push({ level: "warn", text: `${limit.productCategory || p.kind} ${limit.valance || ""} ${limit.series || ""} 限制 ${maxW || "-"} x ${maxH || "-"} inch` });
    }
  }
  if (p.control === "电动" && width > 157) out.push({ level: "warn", text: "宽度超过 157 inch，系统会自动改成双电机方案" });
  if (!p.priceSamples.length && p.kind !== "标准配件") out.push({ level: "warn", text: "这个产品还没有可用价格规则，上线前必须补齐价格" });
  if (p.kind === "标准配件") out.push({ level: "info", text: "标准配件按固定 SKU 价格销售，不需要填写宽高" });
  if (!out.length) out.push({ level: "ok", text: "当前配置可保存为报价或订单草稿" });
  return out;
}

function priceResult(p) {
  const qty = Number(state.order.quantity || 1);
  const sample = nearestPriceSample(p);
  if (p.kind === "标准配件") {
    return {
      total: 0,
      currency: "USD",
      note: "标准配件按固定价格销售。这里展示它也可以进入配置中心统一维护图片、说明和订单留档。",
      rows: [["价格方式", "固定SKU价格"], ["数量", qty], ["后续业务", "报价/订单直接读取固定价"]]
    };
  }
  if (!sample) {
    return {
      total: 0,
      currency: "USD",
      note: "这个商品还不能正式报价，因为价格规则没有维护完整。",
      rows: [["价格方式", "待维护"], ["业务影响", "不能发布给销售下单"], ["下一步", "补价格规则"]]
    };
  }
  const base = Number(sample.price || 0);
  return {
    total: base * qty,
    currency: sample.currency || "USD",
    note: `按 ${sample.width} x ${sample.height} inch 附近的价格档计算。${sample.seriesName ? `系列 ${sample.seriesName}` : ""}`,
    rows: [
      ["价格方式", adapterMode(p)],
      ["矩阵价", money(base, sample.currency)],
      ["数量", qty],
      ["业务影响", "报价和订单会锁定此价格"]
    ]
  };
}

function defaultRules(p) {
  const rules = [];
  for (const slot of p.slots) {
    if (slot.mandatory === "Y") {
      rules.push({ type: "必填", condition: "该问题显示时", action: `${primaryText(slot.name, SLOT_CN)} 必须填写`, severity: "BLOCKER", source: "业务规则" });
    }
  }
  if (p.slots.some(slot => /fabric|material/i.test(slot.name))) {
    rules.push({ type: "选项联动", condition: "选择面料或材质后", action: "只显示可用颜色/面料选项", severity: "INFO", source: "业务规则" });
  }
  if (p.control === "电动") {
    rules.push({ type: "自动计算", condition: "宽度 > 157 inch", action: "电机数量自动变成 2", severity: "WARNING", source: "业务规则" });
  }
  if (p.slots.some(slot => /solar panel/i.test(slot.name))) {
    rules.push({ type: "自动带配件", condition: `选择 ${bilingualText("Solar Panel", TERM_CN)}`, action: `生产配件加入 ${bilingualText("Solar Panel", TERM_CN)}`, severity: "INFO", source: "业务规则" });
  }
  for (const limit of p.dimensionLimits || []) {
    rules.push({ type: "尺寸限制", condition: `${limit.productCategory} ${limit.valance} ${limit.series}`, action: `最大 ${limit.maxWidth} x ${limit.maxHeight} inch`, severity: "WARNING", source: "业务规则" });
  }
  return rules;
}

function customRules(p) {
  return state.store.rules[p.id] || [];
}

function snapshot(p) {
  const selections = {};
  for (const slot of p.slots) {
    const option = selectedOption(slot, p);
    selections[bilingualText(slot.name, SLOT_CN)] = option ? optionText(option) : "";
  }
  const draft = currentDraft(p);
  const price = priceResult(p);
  return {
    productId: p.id,
    productName: p.name,
    productType: p.kind,
    room: state.order.room,
    templateVersion: draft.templateVersion,
    dimensions: {
      display: { width: Number(state.order.width), height: Number(state.order.height), unit: draft.displayUnit },
      production: { width: inchToCm(state.order.width), height: inchToCm(state.order.height), unit: draft.internalUnit },
      quantity: Number(state.order.quantity)
    },
    selections,
    price: { amount: Number(price.total.toFixed(2)), currency: price.currency, source: adapterMode(p) },
    calculatedUsage: usageModels(p).map(item => ({ name: usageLabel(item.code), result: item.result, unit: item.uom })),
    productionItems: autoBom(p).map(item => ({
      component: bilingualText(item.component, COMPONENT_CN),
      qty: item.qty,
      source: bilingualText(item.source, SLOT_CN),
      option: bilingualText(item.option, TERM_CN)
    })),
    messages: validations(p).map(item => item.text)
  };
}

function render() {
  const p = product();
  if (!p) return;
  initSelections(p);
  renderProductList();
  renderHeader(p);
  renderTabs();
  renderModule(p);
  renderRail(p);
  updateSavedPill();
}

function renderProductList() {
  const term = state.search.toLowerCase();
  const items = DATA.products.filter(p => [p.id, p.name, p.kind, p.control].join(" ").toLowerCase().includes(term));
  $("productList").innerHTML = items.map(p => `
    <button class="product-card ${p.id === state.productId ? "active" : ""}" type="button" data-product-id="${esc(p.id)}">
      <strong>${esc(p.name)}</strong>
      <small>${esc(p.kind)} / ${esc(p.control)}</small>
      <span class="meta-row">
        <span class="tag accent">${p.slots.length} 问题</span>
        <span class="tag">${allOptionsCount(p)} 选项</span>
        <span class="tag ${p.priceSamples.length ? "ok" : "warn"}">${esc(p.priceSummary.mode)}</span>
      </span>
    </button>
  `).join("") || `<div class="empty">没有匹配产品。</div>`;
}

function renderHeader(p) {
  $("productTags").innerHTML = `
    <span class="tag">${esc(p.kind)}</span>
    <span class="tag">${esc(p.control)}</span>
    <span class="tag ${p.priceSamples.length ? "ok" : "warn"}">${esc(adapterMode(p))}</span>
  `;
  $("productTitle").textContent = p.name;
  $("productSummary").textContent = `${p.kind} / ${p.control}。这个产品需要维护 ${p.slots.length} 个下单问题、${allOptionsCount(p)} 个可选答案，配置好以后销售可下单，系统可算价并生成生产配件。`;
  const metrics = [
    ["下单问题", p.slots.length],
    ["可选答案", allOptionsCount(p)],
    ["自动带出", p.componentMappings.length],
    ["价格档", p.priceSamples.length]
  ];
  $("metricGrid").innerHTML = metrics.map(item => `<div class="metric"><span>${item[0]}</span><b>${item[1]}</b></div>`).join("");
}

function renderTabs() {
  $("moduleTabs").innerHTML = MODULES.map(([id, label]) => `
    <button class="module-tab ${id === state.module ? "active" : ""}" type="button" data-module="${id}">${label}</button>
  `).join("");
}

function renderModule(p) {
  const map = {
    config: renderConfigCenter,
    overview: renderOverview,
    model: renderModel,
    offering: renderOffering,
    template: renderTemplate,
    slots: renderSlots,
    rules: renderRules,
    price: renderPrice,
    usage: renderUsage,
    bom: renderBom,
    content: renderContent,
    testing: renderTesting,
    order: renderOrder,
    snapshot: renderSnapshot
  };
  $("moduleContent").innerHTML = (map[state.module] || renderOverview)(p);
}

function renderConfigCenter(p) {
  const samples = configProducts(p);
  return `
    ${section("配置中心：一个产品从 0 到 1 怎么建", "这个模块只讲后台配置，不讲销售怎么下单。ERP 操作员要评审的是：产品系列、产品、组件、问题/选项、规则、价格/用量、测试上线是否能把一个真实产品配置出来。", `
      <div class="grid-3">
        ${[
          ["先建结构", "产品系列 -> 可售产品 -> 组件库。先把产品是什么、能卖什么、会用到哪些组件说清楚。"],
          ["再建配置", "问题/选项 -> 规则 -> 价格/用量。配置员维护的是客户要选什么，以及系统要自动判断什么。"],
          ["最后验证", "测试上线 -> 给报价/订单/生产使用。下单只是验证配置结果，不是配置中心的主线。"]
        ].map(card).join("")}
      </div>
      <div class="flow">${STAGES.map((stage, index) => `<div class="flow-step"><b>${index + 1}. ${stage[0]}</b><span>${stage[1]}</span></div>`).join("")}</div>
    `)}
    ${section("真实产品样本，不只看一个柔纱帘", "这里至少放 3 个从数据里抽出来的复杂产品。它们共用同一套配置中心链路，但问题数量、答案数量、组件绑定和价格完整度不同。", `
      <div class="product-samples">${samples.map(productSampleCard).join("")}</div>
      <div class="table-wrap">${table(["产品", "系列/控制", "配置问题", "可选答案", "组件绑定", "价格能力", "用来评审什么"], samples.map(item => [
        item.name,
        `${item.kind} / ${item.control}`,
        `${item.slots.length} 个`,
        `${allOptionsCount(item)} 个`,
        `${item.componentMappings.length} 条`,
        item.priceSamples.length ? `${item.priceSamples.length} 个价格样本` : "价格待补",
        item.id === p.id ? "当前选中，下面完整展开" : "用于对照不同产品复杂度"
      ]))}</div>
    `)}
    ${section("三个产品的配置问题清单", "这一段给 ERP 操作员快速对照：不同产品到底要配置哪些问题、每个问题有多少答案。", `
      <div class="sample-stack">${samples.slice(0, 3).map(productQuestionBlock).join("")}</div>
    `)}
    ${section(`${p.name} 从 0 到 1 设置清单`, "以当前选中产品为例，把配置中心要保存的业务对象按顺序展开。这里用的是新架构对象名，不再把页面做成旧数据字段对照表。", `
      <div class="table-wrap">${table(["配置层", "当前真实数据", "配置员要做什么", "保存到新架构"], configSetupRows(p))}</div>
    `)}
    ${section(`${p.name} 的问题和选项怎么维护`, `当前产品有 ${p.slots.length} 个问题、${allOptionsCount(p)} 个答案。中文是操作员看到的含义，英文保留用于和供应商资料/开发编码核对。`, `
      <div class="table-wrap">${table(["顺序", "配置问题", "填写方式", "是否必填", "答案数", "答案库", "保存对象"], questionRows(p))}</div>
    `)}
    ${section("答案库完整展开", "这里不是模拟下单，而是展示配置员要维护的答案库。比如柔纱帘的 108 个答案，就是按 12 个问题分别存到 ConfigOption。", `
      <div class="option-groups">
        ${p.slots.map(slot => `
          <div class="option-group">
            <div class="option-group-head">
              <h3>${bilingualHtml(slot.name, SLOT_CN)}</h3>
              <span class="tag accent">${slot.options.length} 个答案</span>
            </div>
            ${optionCloud(slot)}
          </div>
        `).join("")}
      </div>
    `)}
    ${section("组件绑定：答案如何变成生产组件", "配置中心要解决的是“选完以后生产需要什么”。销售不手工勾组件，配置员只维护答案到组件的绑定关系。", `
      <div class="grid-3">
        ${[
          ["组件库", "先维护电机、面料、罩壳、底杆、太阳能板、伸缩杆等组件。"],
          ["答案绑定", "比如选择蓝牙电机方案，系统带出 Bluetooth Motor。"],
          ["生产输出", "下游生产读取配置结果自动生成组件清单和数量。"]
        ].map(card).join("")}
      </div>
      <div class="table-wrap">${table(["当这个问题", "客户选择这个答案", "系统绑定组件", "数量", "业务含义"], componentBindingRows(p))}</div>
    `)}
    ${section("规则到底是什么，禁止组合不是必配项", "规则是系统自动判断。不是每个产品都要设置所有规则；能用选项和组件绑定表达的，不要硬写成规则。", `
      <div class="table-wrap">${table(["规则类型", "什么时候需要", "业务例子", "系统输出"], ruleGuideRows(p))}</div>
    `)}
    ${section("配置中心怎么存数据", "下面是按新架构对象生成的草稿快照，用来帮助开发理解页面背后的保存结构。真实开发时对象可以拆表或合表，但业务含义不能丢。", `
      <pre class="snapshot">${esc(JSON.stringify(configStoragePreview(p), null, 2))}</pre>
    `)}
    ${section("配置完成后才给业务使用", "配置中心发布出去的不是一个页面，而是一组可被报价、订单、生产读取的结果。", `
      <div class="table-wrap">${table(["输出给谁", "拿到什么", "业务用途"], [
        ["报价", "可见问题、可选答案、价格结果、提醒", "销售按配置报价，不再人工查表。"],
        ["订单", "客户选择、尺寸、价格、校验结果、模板版本", "订单保存完整配置快照，后面规则改了也能看懂。"],
        ["生产", "组件清单、数量、用量结果、特殊提示", "生产按系统生成的组件做货。"],
        ["后期接口", "稳定 code、中文/英文名称、发布版本", "其他系统直接读配置中心，不再散落各处。"]
      ])}</div>
    `)}
  `;
}

function renderOverview(p) {
  return `
    ${section("这个配置中心是做什么的", "它不是给技术看字段的页面，而是让产品、销售、报价、订单、生产都使用同一套产品规则。先在这里把商品能不能卖、怎么选、怎么算价、生产带什么配件设置清楚，后面业务就不用靠人工记。", `
      <div class="grid-3">
        ${[
          ["给产品人员", "维护商品档案、下单问题、图片说明，保证销售看到的是正确产品。"],
          ["给销售/报价", "选择商品后按系统问题填写，系统自动检查尺寸和组合，并算出价格。"],
          ["给订单/生产", "订单保存当时配置和配件清单，生产按留档做货，不再从备注里猜。"]
        ].map(card).join("")}
      </div>
    `)}
    ${section("业务主流程", "像“先建部门，再在人员里选择部门，后面按部门控制权限”一样，配置中心也有固定先后关系。", `
      <div class="flow">${STAGES.map((stage, index) => `<div class="flow-step"><b>${index + 1}. ${stage[0]}</b><span>${stage[1]}</span></div>`).join("")}</div>
    `)}
    ${section("每个模块和后面业务的关系", "ERP 操作员评审时可以按这一张表判断：有没有符合你们现在的真实流程。", `
      <div class="table-wrap">${table(["先设置什么", "这个模块做什么", "后面业务怎么用"], [
        ["产品档案", "定义这是哪类产品、是否需要尺寸、是否需要生产配件", "销售商品、下单模板、价格规则都会引用它"],
        ["销售商品", "决定客户/销售实际能买哪个商品，是标准品还是定制品", "报价和订单先选择销售商品"],
        ["下单模板", "决定销售下单时要填写哪些问题", "报价单、订单页面按模板显示字段"],
        ["业务规则", "控制哪些选项能一起选、哪些尺寸不允许、哪些配件自动出现", "下单时自动提醒或拦截错误配置"],
        ["价格设置", "维护固定价、尺寸价、面积价、选项加价", "报价和订单自动算价格并锁价"],
        ["生产配件", "维护选择某些配置后生产需要哪些组件", "订单转生产时自动生成配件清单"],
        ["测试上线", "上线前模拟下单检查价格、规则、配件、留档是否正确", "通过后销售才能正式使用"]
      ])}</div>
    `)}
    ${section("当前产品配置概览", "这部分只是帮助评审这个产品复杂度，不代表新系统要按旧系统字段展示。", `
      <div class="grid-3">
        ${[
          ["下单问题", `${p.slots.length} 个问题，例如控制方向、面料、安装方式、配件选择。`],
          ["可选答案", `${allOptionsCount(p)} 个答案，系统会按产品和规则筛选。`],
          ["价格能力", p.priceSamples.length ? "已有价格档可演示自动报价。" : "价格规则未完整，不能正式上线。"],
          ["生产配件", allPhysicalMappings(p).length ? "已有自动带出关系，可演示生产配件。" : "需要补生产配件规则。"],
          ["资料内容", "需要维护客户帮助、测量说明、安装说明、图片资料。"],
          ["上线检查", "必须通过模拟订单、价格、配件、留档检查后才能发布。"]
        ].map(card).join("")}
      </div>
    `)}
  `;
}

function renderModel(p) {
  const d = currentDraft(p);
  return `
    ${section("产品档案做什么", "先定义这个产品属于哪一类、销售时是否需要填尺寸、是否会生成生产配件。后面的销售商品、下单模板、价格、生产配件都会引用这个档案。", `
      <div class="grid-3" data-draft-scope="model">
        ${field("modelCode", "产品档案编码", d.modelCode)}
        ${field("productType", "产品类型", p.kind)}
        ${selectField("saleMode", "销售方式", d.saleMode, [["CUSTOM_CONFIGURED", "定制品：销售下单要填配置"], ["STANDARD_SKU", "标准品：固定 SKU"], ["SEMI_STANDARD", "半标准品：少量选项"], ["ACCESSORY", "配件/服务"]])}
        ${selectField("displayUnit", "销售页面显示单位", d.displayUnit, ["inch", "cm"])}
        ${selectField("internalUnit", "生产/计算单位", d.internalUnit, ["cm"])}
        ${field("businessOwner", "负责部门", "产品部 / 销售运营")}
      </div>
      ${actions("model")}
    `)}
    ${section("和后面业务的关系", "这些开关决定报价、订单、生产会不会使用某些功能。", `
      <div class="grid-4">
        ${card(["需要尺寸", p.kind === "标准配件" ? "不需要，标准品可直接下单。" : "需要，报价和生产都要用宽高。"])}
        ${card(["需要生产配件", allPhysicalMappings(p).length ? `需要，当前可演示 ${allPhysicalMappings(p).length} 条自动带出。` : "暂未配置，上线前要补。"])}
        ${card(["需要价格规则", p.priceSamples.length ? "需要，当前可演示自动报价。" : "需要补齐，否则销售不能下单。"])}
        ${card(["需要订单留档", "需要。订单必须保存当时选择、价格和配件，避免后面规则改了看不懂旧单。"])}
      </div>
    `)}
  `;
}

function renderOffering(p) {
  const d = currentDraft(p);
  return `
    ${section("销售商品做什么", "产品档案只是分类和规则基础；销售商品才是销售、报价、订单里真正能选择的商品。一个产品档案下面可以有多个销售商品，比如不同系列、不同控制方式、配件、服务。", `
      <div class="grid-3" data-draft-scope="offering">
        ${field("offeringCode", "销售商品编码", d.offeringCode)}
        ${field("sellingName", "销售商品名称", p.name)}
        ${field("modelRef", "关联产品档案", p.kind)}
        ${field("structure", "产品结构", p.valanceCategoryName || "按产品档案")}
        ${field("controlCategory", "默认控制方式", p.controlCategoryName || p.control)}
        ${selectField("sellingStatus", "销售状态", "草稿", ["草稿", "可销售", "暂停销售", "已归档"])}
      </div>
      ${actions("offering")}
    `)}
    ${section("销售方式差异", "ERP 操作员只需要判断这个商品怎么卖；系统根据销售方式决定后续要不要填模板、价格、配件。", `
      <div class="table-wrap">${table(["销售方式", "后台要维护什么", "报价/订单会怎么走"], [
        ["标准品", "固定价格、库存、图片、说明", "销售直接选数量加入订单"],
        ["半标准品", "少量选项、固定价或选项加价", "销售选择几个选项后下单"],
        ["定制品", "下单模板、业务规则、价格、生产配件", "销售必须填写完整配置，系统自动算价和配件"],
        ["配件/服务", "适用产品、固定价、说明", "可以单独售卖，也可以被定制品自动带出"]
      ])}</div>
    `)}
  `;
}

function renderTemplate(p) {
  const d = currentDraft(p);
  return `
    ${section("下单模板做什么", "下单模板决定销售在报价/订单里要填写哪些问题。模板上线后，销售只看到发布版本；草稿修改不会影响已经下过的订单。", `
      <div class="grid-3" data-draft-scope="template">
        ${field("templateVersion", "模板版本编码", d.templateVersion)}
        ${selectField("templateStatus", "模板状态", "草稿", ["草稿", "测试中", "已上线", "已归档"])}
        ${field("templateOwner", "维护人", "产品运营")}
      </div>
      ${actions("template")}
    `)}
    ${section("为什么模板要分版本", "价格、选项、规则会变，但旧订单不能跟着变。版本化后，新订单用新版本，旧订单保留当时版本。", `
      <div class="table-wrap">${table(["下单问题", "依赖前置选择", "业务阶段", "异常怎么处理"], p.slots.map(slot => [
        raw(bilingualHtml(slot.name, SLOT_CN)),
        dependencyForSlot(slot),
        slot.mandatory === "Y" ? "必填检查" : "选项联动",
        slot.mandatory === "Y" ? "不填不能下单" : "给出提醒或自动筛选"
      ]))}</div>
    `)}
  `;
}

function renderSlots(p) {
  return `
    ${section("下单问题做什么", "这里维护销售下单时看到的问题，例如控制方式、面料、颜色、安装方式、是否要太阳能板。中文是 ERP 操作名，英文小字保留给开发和数据对照；销售只需要按问题填写，系统后面会自动算价和生成配件。", `
      <div class="table-wrap">${table(["显示顺序", "销售要填写的问题", "填写方式", "是否必填", "可选答案数", "答案示例"], p.slots.map(slot => [
        slot.sequence || "",
        raw(`${bilingualHtml(slot.name, SLOT_CN)}<small>问题编码 Q_${esc(slot.itemId)}</small>`),
        slot.type === "SINGLE" ? "单选" : slot.type || "单选",
        raw(slot.mandatory === "Y" ? badge("是", "danger") : badge("否", "")),
        slot.options.length,
        slot.options.slice(0, 6).map(option => esc(optionText(option))).join(" / ") + (slot.options.length > 6 ? " ..." : "")
      ]))}</div>
    `)}
    ${section("和后面业务的关系", "下单问题不是备注。每个答案都会影响后续规则：能不能选、价格怎么算、生产带什么配件。", `
      <div class="grid-3">
        ${p.slots.slice(0, 6).map(slot => card([bilingualText(slot.name, SLOT_CN), `${slot.options.length} 个答案。可设置默认答案、排序、图片、适用范围，也可影响价格或配件。`])).join("")}
      </div>
    `)}
  `;
}

function renderRules(p) {
  const rules = [...defaultRules(p), ...customRules(p)];
  return `
    ${section("业务规则做什么", "业务规则负责把人工经验变成系统自动判断。先把产品、组件、问题和答案配置好，再补规则；禁止组合只是“两个答案不能同时选”时才用，不是必经步骤。", `
      <div class="grid-4">
        ${selectField("ruleTypeInput", "规则类型", "提醒", [
          ["显示/隐藏问题", "显示/隐藏问题：前置选择控制后面问题"],
          ["筛选答案", "筛选答案：只保留当前能选的答案"],
          ["必填", "必填：当前问题不填不能保存"],
          ["禁止组合", "禁止组合：两个答案不能同时出现"],
          ["尺寸限制", "尺寸限制：宽高或面积超出能力"],
          ["提醒", "提醒：允许保存但要人工复核"],
          ["自动计算", "自动计算：生成数量、长度、面积等结果"],
          ["自动带配件", "自动带配件：答案触发生产组件"]
        ])}
        ${selectField("ruleSlotInput", "当客户填写", p.slots[0]?.itemId || "", p.slots.map(slot => [slot.itemId, bilingualText(slot.name, SLOT_CN)]))}
        ${field("ruleValueInput", "选择了什么", optionText(p.slots[0]?.options[0]))}
        ${field("ruleActionInput", "系统自动做什么", "提醒 / 禁止下单 / 自动带配件 / 自动改数量")}
      </div>
      <div class="actions"><button class="btn" type="button" data-action="add-rule">新增规则草稿</button></div>
    `)}
    ${section("规则类型说明", "ERP 操作员可以先按这张表判断：这条业务逻辑到底该维护成问题、答案、组件绑定，还是规则。", `
      <div class="table-wrap">${table(["规则类型", "什么时候需要", "业务例子", "系统输出"], ruleGuideRows(p))}</div>
    `)}
    ${section("当前业务规则清单", "ERP 操作员可以按这张表检查：系统自动判断是否符合真实业务。", `
      <div class="table-wrap">${table(["规则类型", "触发条件", "系统动作", "严重程度", "业务来源"], rules.map(rule => [
        rule.type,
        rule.condition,
        rule.action,
        raw(badge(rule.severity, rule.severity === "BLOCKER" ? "danger" : rule.severity === "WARNING" ? "warn" : "ok")),
        rule.source || "Local Draft"
      ]))}</div>
    `)}
  `;
}

function renderPrice(p) {
  const summary = p.priceSummary || {};
  const sample = nearestPriceSample(p);
  const rows = p.priceSamples.slice(0, 24).map(item => [
    item.width,
    item.height,
    item.seriesName || item.modelConfigurationName || "-",
    item.controlCategoryId || "-",
    money(item.price, item.currency)
  ]);
  return `
    ${section("价格设置做什么", "这里维护这个商品如何报价。销售下单时不再手工查价，系统根据尺寸、系列、控制方式、配件和数量自动计算价格。", `
      <div class="grid-3">
        ${card(["价格方式", p.kind === "标准配件" ? "固定 SKU 价格" : adapterMode(p)])}
        ${card(["当前试算", sample ? `${sample.width} x ${sample.height} inch，${money(sample.price, sample.currency)}` : "还不能试算"])}
        ${card(["业务影响", sample || p.kind === "标准配件" ? "销售可进入模拟下单检查价格。" : "不能上线给销售使用。"])}
      </div>
    `)}
    ${section("价格规则摘要", "ERP 操作员主要检查价格范围、币种和生效规则是否符合业务。", `
      <div class="table-wrap">${table(["字段", "值"], [
        ["价格模式", summary.mode || "待补"],
        ["价格档数量", summary.rows || 0],
        ["宽度范围", `${summary.minWidth || "-"} - ${summary.maxWidth || "-"} inch`],
        ["高度范围", `${summary.minHeight || "-"} - ${summary.maxHeight || "-"} inch`],
        ["价格范围", `${money(summary.minPrice, summary.currency)} - ${money(summary.maxPrice, summary.currency)}`]
      ])}</div>
    `)}
    ${section("价格档预览", "这张表模拟价格维护页面。正式系统里操作员应能按系列、控制方式、宽高档查询和维护价格。", `
      <div class="table-wrap">${table(["宽度", "高度", "系列/规格", "控制方式", "价格"], rows)}</div>
    `)}
  `;
}

function renderUsage(p) {
  return `
    ${section("用量计算做什么", "窗帘不是只卖一个 SKU。系统要根据宽高算面料面积、轨道长度、链条长度、电机数量。这些结果会同时影响价格和生产配件。", `
      <div class="table-wrap">${table(["业务用量", "计算方式", "当前结果", "单位", "说明"], usageModels(p).map(item => [
        usageLabel(item.code),
        usageMethodLabel(item.method),
        item.result,
        item.uom,
        usageBusinessNote(item.code)
      ]))}</div>
    `)}
    ${section("和价格/生产的关系", "销售只填宽高，系统自动换算为生产和计价需要的单位。操作员只需要检查公式是否符合工厂口径。", `
      <div class="grid-3">
        ${card(["销售看到", `宽高单位 ${currentDraft(p).displayUnit}`])}
        ${card(["生产计算", `内部按 ${currentDraft(p).internalUnit} 计算`])}
        ${card(["订单留档", "同时保存销售输入和系统计算结果"])}
      </div>
    `)}
  `;
}

function renderBom(p) {
  const bom = autoBom(p);
  return `
    ${section("生产配件做什么", "销售下单选了某些配置后，系统自动告诉生产需要哪些配件。比如选电机就带电机，选太阳能板就带太阳能板，超宽就变双电机。", `
      <div class="table-wrap">${table(["生产需要什么", "数量", "数量来源", "由哪个选择触发", "说明"], bom.map(item => [
        raw(`${bilingualHtml(item.component, COMPONENT_CN)}<small>${esc(item.productId)}</small>`),
        item.qty,
        usageLabel(item.usageCode),
        raw(`${bilingualHtml(item.source, SLOT_CN)}<small>${esc(bilingualText(item.option, TERM_CN))}</small>`),
        item.note
      ]))}</div>
    `)}
    ${section("自动带出规则预览", "这张表用于让 ERP 操作员检查：客户选择某个答案后，系统带出的生产物料是否正确。", `
      <div class="table-wrap">${table(["下单问题", "客户选择", "系统带出", "数量", "类型"], p.componentMappings.slice(0, 80).map(item => [
        raw(bilingualHtml(item.itemName, SLOT_CN)),
        bilingualText(item.optionName, TERM_CN),
        raw(`${bilingualHtml(item.componentName, COMPONENT_CN)}<small>${esc(item.componentProductId)}</small>`),
        item.quantity || "待定",
        item.kind
      ]))}</div>
    `)}
  `;
}

function renderContent(p) {
  const d = currentDraft(p);
  return `
    ${section("图文资料做什么", "销售和客户下单时不能只看到编码。这里维护产品图片、颜色/面料图、测量说明、安装说明和内部备注。", `
      <div class="grid-2" data-draft-scope="content">
        ${textarea("helpText", "客户帮助说明", d.helpText)}
        ${textarea("installText", "安装/测量说明", d.installText)}
        ${textarea("internalNote", "内部备注", d.internalNote)}
        ${textarea("publishNote", "发布备注", d.publishNote)}
      </div>
      ${actions("content")}
    `)}
    ${section("资料会影响哪里", "维护好资料后，销售下单、客户确认、生产查看都会使用同一套说明，避免不同人员各写各的备注。", `
      <div class="grid-4">
        ${["产品主图", "报价和订单选择商品时展示。", "颜色/面料图", "客户选择面料颜色时展示。", "测量说明", "减少销售填错尺寸。", "安装说明", "交付和售后可复用。"].reduce((list, item, index, arr) => index % 2 === 0 ? [...list, card([item, arr[index + 1]])] : list, []).join("")}
      </div>
    `)}
    ${section("资料资产应该怎么存", "图片不是写在备注里。产品主图、颜色图、面料图、组件图和安装测量文件都先进入资料资产库，再绑定到产品、问题答案或组件。", `
      <div class="table-wrap">${table(["资料类型", "绑定对象", "例子", "发布前检查"], [
        ["产品主图", p.name, `P_${p.id}_MAIN.jpg`, "缺主图可保存草稿，发布前提醒"],
        ["颜色/面料图", "颜色或面料答案", "COLOR_N001_PureWhite.jpg / FABRIC_YS180002_White.jpg", "客户会看到的色卡图缺失时应拦截"],
        ["组件图片", "组件库里的电机、遥控、Mini bridge", "COMPONENT_BLUETOOTH_MOTOR.jpg", "组件被答案绑定后必须能解释用途"],
        ["安装/测量说明", "产品或安装方式问题", "MEASURE_INSIDE_OUTSIDE.pdf", "销售下单和客户确认共用"]
      ])}</div>
    `)}
    ${section("哪些对象都要有说明", "客户反馈提醒我们：提示和文字说明很重要。产品、问题、答案、组件、动态规则都要能解释清楚，不只是上传图片。", `
      <div class="table-wrap">${table(["对象", "需要说明什么", "谁会看"], [
        ["产品/系列", "产品说明、适用场景、客户说明、内部备注", "销售、客户、运营"],
        ["配置问题", "为什么要填、怎么填、安装/测量帮助、必填原因", "销售、客户"],
        ["配置答案", "选中含义、选中效果、限制原因、色卡/面料图", "销售、客户、订单快照"],
        ["组件/标准品", "是什么、怎么安装、是否包含、是否收费、适用范围", "销售、客户、生产"],
        ["动态规则", "为什么禁选、为什么自动带出、为什么换方案", "销售、内部、生产"]
      ])}</div>
    `)}
  `;
}

function renderTesting(p) {
  const checks = releaseChecks(p);
  return `
    ${section("测试上线做什么", "产品配置不是保存后就能给销售用。上线前要像真实销售一样模拟下单，检查问题、规则、价格、配件、订单留档是否正确。", `
      <div class="table-wrap">${table(["检查项目", "结果", "业务说明"], checks.map(item => [
        item.name,
        raw(badge(item.pass ? "PASS" : "FAIL", item.pass ? "ok" : "danger")),
        item.note
      ]))}</div>
      <div class="actions">
        <button class="btn" type="button" data-action="run-tests">运行测试</button>
        <button class="btn secondary" type="button" data-action="save-test">保存当前用例</button>
      </div>
    `)}
    ${section("核心测试用例", "每个产品上线前至少要保存几条典型订单。以后修改配置时，系统自动重跑这些用例，避免改坏旧规则。", `
      <div class="table-wrap">${table(["测试场景", "模拟输入", "应该得到什么结果"], defaultTests(p).map(item => [
        item.name,
        item.input,
        item.expected
      ]))}</div>
    `)}
  `;
}

function renderOrder(p) {
  return `
    ${section("模拟真实销售下单", "这个页面模拟销售在报价或订单里实际操作。销售选择商品、填写宽高和配置项，右侧立刻显示价格、提醒和生产配件。", `
      <div class="grid-4">
        ${orderField("width", "宽度 inch", state.order.width, "number")}
        ${orderField("height", "高度 inch", state.order.height, "number")}
        ${orderField("quantity", "数量", state.order.quantity, "number")}
        ${orderField("room", "房间/窗口", state.order.room, "text")}
      </div>
    `)}
    ${section("销售需要填写的问题", "这里显示的是该商品发布后的下单表单。ERP 操作员可以检查：这些问题是否够用、顺序是否合理、选项是否正确；下拉答案按“中文 / English”展示，便于和原始数据核对。", `
      <div class="grid-2">
        ${p.slots.map(slot => `
          <div class="field">
            <label>${bilingualHtml(slot.name, SLOT_CN)} ${slot.mandatory === "Y" ? badge("必填", "danger") : ""}</label>
            <select data-slot-id="${esc(slot.itemId)}">
              ${slot.options.map(option => `<option value="${esc(option.optionId)}" ${state.selections[p.id]?.[slot.itemId] === option.optionId ? "selected" : ""}>${esc(optionText(option))}</option>`).join("")}
            </select>
          </div>
        `).join("")}
      </div>
      <div class="actions"><button class="btn" type="button" data-action="save-snapshot">保存订单留档</button></div>
    `)}
  `;
}

function renderSnapshot(p) {
  const saved = state.store.snapshots.filter(item => item.productId === p.id).slice(-8).reverse();
  return `
    ${section("订单留档做什么", "订单不能只保存一段备注。必须保存当时客户怎么选、系统怎么算价、生产需要什么配件。这样以后产品规则改了，旧订单仍然看得懂。", `<pre class="snapshot">${esc(JSON.stringify(snapshot(p), null, 2))}</pre>`)}
    ${section("已保存的模拟订单", "这里保存在浏览器里，用来演示历史订单不受配置改版影响。正式系统应保存到订单行配置留档。", `
      <div class="actions"><button class="btn secondary" type="button" data-action="clear-snapshots">清空当前产品留档</button></div>
      <div class="list">
        ${saved.length ? saved.map((item, index) => `
          <div class="list-item">
            <strong>${esc(item.savedAt)} / ${money(item.price.amount, item.price.currency)}</strong>
            <small>${esc(item.dimensions.display.width)} x ${esc(item.dimensions.display.height)} ${esc(item.dimensions.display.unit)}，${Object.keys(item.selections).length} 个选项</small>
          </div>
        `).join("") : `<div class="empty">还没有保存当前产品的订单留档。</div>`}
      </div>
    `)}
  `;
}

function renderRail(p) {
  const price = priceResult(p);
  $("priceValue").textContent = price.total ? money(price.total, price.currency) : "待定";
  $("priceNote").textContent = price.note;
  $("priceBreakdown").innerHTML = price.rows.map(row => `<div class="break-row"><span>${esc(row[0])}</span><strong>${esc(row[1])}</strong></div>`).join("");
  $("stageList").innerHTML = STAGES.map((stage, index) => `<div class="stage"><b>${index + 1}</b><span>${esc(stage[0])}: ${esc(stage[1])}</span></div>`).join("");
  $("validationList").innerHTML = validations(p).map(item => `<div class="list-item"><span class="tag ${levelClass(item.level)}">${esc(item.level)}</span><strong>${esc(item.text)}</strong></div>`).join("");
  const bom = autoBom(p);
  $("bomList").innerHTML = bom.length ? bom.map(item => `<div class="list-item"><strong>${esc(bilingualText(item.component, COMPONENT_CN))} x ${esc(item.qty)}</strong><small>${esc(bilingualText(item.source, SLOT_CN))}: ${esc(bilingualText(item.option, TERM_CN))} / ${esc(usageLabel(item.usageCode))} / ${esc(item.note)}</small></div>`).join("") : `<div class="empty">当前配置没有自动带出物理组件。</div>`;
}

function section(title, desc, body) {
  return `<section class="section"><h2>${esc(title)}</h2><p>${esc(desc)}</p>${body}</section>`;
}

function card(item) {
  return `<div class="mini-card"><h3>${esc(item[0])}</h3><p>${esc(item[1])}</p></div>`;
}

function field(key, label, value, readonly = false) {
  return `<div class="field"><label>${esc(label)}</label><input data-draft-key="${esc(key)}" value="${esc(value)}" ${readonly ? "readonly" : ""}></div>`;
}

function textarea(key, label, value) {
  return `<div class="field"><label>${esc(label)}</label><textarea data-draft-key="${esc(key)}">${esc(value)}</textarea></div>`;
}

function selectField(key, label, value, options) {
  const opts = options.map(option => {
    const pair = Array.isArray(option) ? option : [option, option];
    return `<option value="${esc(pair[0])}" ${String(pair[0]) === String(value) ? "selected" : ""}>${esc(pair[1])}</option>`;
  }).join("");
  return `<div class="field"><label>${esc(label)}</label><select data-draft-key="${esc(key)}">${opts}</select></div>`;
}

function orderField(key, label, value, type) {
  return `<div class="field"><label>${esc(label)}</label><input data-order-key="${esc(key)}" type="${type}" value="${esc(value)}" ${type === "number" ? 'min="1" step="1"' : ""}></div>`;
}

function actions(scope) {
  return `<div class="actions"><button class="btn" type="button" data-action="save-draft" data-scope="${scope}">保存草稿</button><button class="btn secondary" type="button" data-action="reset-draft" data-scope="${scope}">重置本产品草稿</button></div>`;
}

function table(headers, rows) {
  return `<table><thead><tr>${headers.map(head => `<th>${esc(head)}</th>`).join("")}</tr></thead><tbody>${rows.length ? rows.map(row => `<tr>${row.map(cell => `<td>${cell?.__html ?? esc(cell)}</td>`).join("")}</tr>`).join("") : `<tr><td colspan="${headers.length}"><div class="empty">暂无数据。</div></td></tr>`}</tbody></table>`;
}

function badge(label, cls = "") {
  return `<span class="tag ${cls}">${esc(label)}</span>`;
}

function dependencyForSlot(slot) {
  const name = slot.name.toLowerCase();
  if (/color|fabric|material/.test(name)) return "产品类型 / 系列 / 已选面料";
  if (/motor|control/.test(name)) return "产品类型 / 控制方式";
  if (/solar|remote|pole/.test(name)) return "控制方式";
  if (/width|height|mount/.test(name)) return "产品类型 / 安装结构";
  return "下单模板";
}

function releaseChecks(p) {
  const hasPrice = p.kind === "标准配件" || p.priceSamples.length > 0;
  const hasBom = p.kind === "标准配件" || allPhysicalMappings(p).length > 0;
  const blockers = validations(p).filter(item => item.level === "block");
  return [
    { name: "下单问题完整", pass: p.slots.length > 0, note: `当前有 ${p.slots.length} 个下单问题` },
    { name: "必填项检查", pass: !blockers.length, note: blockers.length ? blockers.map(item => item.text).join("; ") : "当前必填项都能填写" },
    { name: "价格可计算", pass: hasPrice, note: hasPrice ? adapterMode(p) : "还没有可用价格，不能上线" },
    { name: "生产配件可生成", pass: hasBom, note: hasBom ? `${allPhysicalMappings(p).length} 条自动带出关系` : "没有生产配件规则" },
    { name: "订单留档完整", pass: true, note: "包含客户选择、尺寸、价格、生产配件和提醒" },
    { name: "上线状态", pass: hasPrice && !blockers.length, note: hasPrice && !blockers.length ? "可进入人工审核" : "需补齐后再审核" }
  ];
}

function defaultTests(p) {
  return [
    { name: "正常报价", input: `${state.order.width} x ${state.order.height} inch，选择默认答案`, expected: "能算出价格或明确提示不能报价" },
    { name: "必填问题", input: `${p.slots.filter(slot => slot.mandatory === "Y").length} 个必填问题`, expected: "不填时系统拦截，填完后允许继续" },
    { name: "生产配件", input: selectedMappings(p).length ? "选择会带配件的答案" : "当前默认答案", expected: "系统自动列出生产需要的配件" },
    { name: "订单留档", input: "保存一张模拟订单", expected: "能复原客户选择、价格和生产配件" }
  ];
}

function usageLabel(code) {
  const labels = {
    FABRIC_AREA: "面料面积",
    PROFILE_LENGTH: "轨道/型材长度",
    CHAIN_LENGTH: "链条/拉绳长度",
    MOTOR_QTY: "电机数量",
    FIXED: "固定数量",
    OPTION_MAPPING: "按客户选择"
  };
  return labels[code] || code || "业务数量";
}

function usageMethodLabel(method) {
  const labels = {
    BY_AREA: "按面积",
    BY_WIDTH: "按宽度",
    BY_HEIGHT: "按高度",
    THRESHOLD: "按阈值判断",
    FIXED: "固定数量"
  };
  return labels[method] || method;
}

function usageBusinessNote(code) {
  const notes = {
    FABRIC_AREA: "用于面料用量和面料价格",
    PROFILE_LENGTH: "用于轨道、底杆、外壳等长度材料",
    CHAIN_LENGTH: "用于链条、拉绳等控制配件",
    MOTOR_QTY: "用于电动产品的电机数量和生产配件"
  };
  return notes[code] || "用于价格或生产配件";
}

function levelClass(level) {
  if (level === "ok") return "ok";
  if (level === "block") return "danger";
  if (level === "warn") return "warn";
  return "";
}

function saveDraftFromDom() {
  const p = product();
  const draft = { ...(state.store.drafts[p.id] || {}) };
  document.querySelectorAll("[data-draft-key]").forEach(input => {
    draft[input.dataset.draftKey] = input.value;
  });
  state.store.drafts[p.id] = draft;
  saveStore();
  showToast("草稿已保存");
}

function resetDraft() {
  const p = product();
  delete state.store.drafts[p.id];
  saveStore();
  render();
  showToast("已重置当前产品草稿");
}

function addRule() {
  const p = product();
  const slotId = $("ruleSlotInput")?.value;
  const slot = p.slots.find(item => item.itemId === slotId) || p.slots[0];
  const rule = {
    type: $("ruleTypeInput")?.value || "WARN",
    condition: `${bilingualText(slot?.name || "slot", SLOT_CN)} = ${$("ruleValueInput")?.value || "*"}`,
    action: $("ruleActionInput")?.value || "show warning",
    severity: "INFO",
    source: "本地草稿"
  };
  state.store.rules[p.id] = [...(state.store.rules[p.id] || []), rule];
  saveStore();
  render();
  showToast("规则草稿已新增");
}

function saveSnapshot() {
  const item = { ...snapshot(product()), savedAt: new Date().toLocaleString() };
  state.store.snapshots.push(item);
  saveStore();
  showToast("订单留档已保存");
  state.module = "snapshot";
  render();
}

function clearSnapshots() {
  const p = product();
  state.store.snapshots = state.store.snapshots.filter(item => item.productId !== p.id);
  saveStore();
  render();
  showToast("已清空当前产品留档");
}

function updateSavedPill() {
  const draftCount = Object.keys(state.store.drafts || {}).length;
  const snapshotCount = (state.store.snapshots || []).length;
  $("savedPill").textContent = `本地草稿 ${draftCount} / 留档 ${snapshotCount}`;
}

function showToast(message) {
  const toast = $("toast");
  toast.textContent = message;
  toast.classList.add("show");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => toast.classList.remove("show"), 1600);
}

document.addEventListener("click", event => {
  const productButton = event.target.closest("[data-product-id]");
  if (productButton) {
    state.productId = productButton.dataset.productId;
    state.module = "config";
    render();
    return;
  }
  const moduleButton = event.target.closest("[data-module]");
  if (moduleButton) {
    state.module = moduleButton.dataset.module;
    render();
    return;
  }
  const action = event.target.closest("[data-action]")?.dataset.action;
  if (!action) return;
  if (action === "save-draft") saveDraftFromDom();
  if (action === "reset-draft") resetDraft();
  if (action === "add-rule") addRule();
  if (action === "run-tests") showToast("测试已运行，结果见发布检查表");
  if (action === "save-test") showToast("当前用例草稿已保存");
  if (action === "save-snapshot") saveSnapshot();
  if (action === "clear-snapshots") clearSnapshots();
});

document.addEventListener("input", event => {
  if (event.target.id === "productSearch") {
    state.search = event.target.value;
    renderProductList();
    return;
  }
  if (event.target.dataset.orderKey) {
    state.order[event.target.dataset.orderKey] = event.target.value;
    render();
  }
});

document.addEventListener("change", event => {
  if (event.target.dataset.slotId) {
    const p = product();
    state.selections[p.id][event.target.dataset.slotId] = event.target.value;
    render();
  }
});

$("resetAllBtn").addEventListener("click", () => {
  if (!window.confirm("清空所有本地草稿和订单留档？")) return;
  localStorage.removeItem(STORAGE_KEY);
  state.store = loadStore();
  render();
  showToast("本地草稿已清空");
});

render();
