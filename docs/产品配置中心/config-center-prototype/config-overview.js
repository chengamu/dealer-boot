const DATA = window.CONFIG_CENTER_PROTOTYPE_DATA || { products: [] };

const FEATURED_PRODUCT_NAMES = [
  "L Cassette Motor Zebra Shades",
  "Flat rail Motor Screen View Shades",
  "L Cassette Chain Blackout shades",
  "Standard Chain Roman Shades",
  "Solar Panel"
];

const state = {
  productId: DATA.products.find(item => item.name === FEATURED_PRODUCT_NAMES[0])?.id || DATA.products[0]?.id || "",
  search: ""
};

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
  ["Inside", "内装"],
  ["Outside", "外装"],
  ["Left", "左侧"],
  ["Right", "右侧"],
  ["Night Black", "夜黑"],
  ["Pearl White", "珍珠白"],
  ["Iron Gray", "铁灰"],
  ["Sand Beige", "沙米色"],
  ["Remote Control Only(Remote Motor)", "仅遥控器（遥控电机）"],
  ["Reverse", "反卷"],
  ["Solar Panel", "加太阳能板"],
  ["No Solar Panel", "不加太阳能板"],
  ["Standard", "标准卷"],
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

function bilingual(value, dictionary = {}) {
  const text = clean(value);
  const cn = translate(text, dictionary);
  return cn && cn !== text ? `${cn} / ${text}` : text;
}

function optionLabel(option) {
  const name = clean(option?.name || "");
  const description = clean(option?.description || "");
  if (description && description !== name && description.includes("--")) {
    return bilingual(description, TERM_CN);
  }
  return bilingual(name || description, TERM_CN);
}

function product() {
  return DATA.products.find(item => item.id === state.productId) || DATA.products[0];
}

function productOptionsCount(p) {
  return p.slots.reduce((sum, slot) => sum + slot.options.length, 0);
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
    const text = [item.id, item.name, item.kind, item.control, item.priceCategoryName, item.valanceCategoryName].join(" ").toLowerCase();
    return !term || text.includes(term);
  });
}

function controlMethodCode(p) {
  const text = `${p.control || ""} ${p.name || ""} ${p.controlCategoryName || ""}`.toLowerCase();
  if (/chain|手拉|拉珠/.test(text)) return "chain";
  if (/motor|电动|battery|remote|zigbee|matter/.test(text)) return "motor";
  if (/standard|标准|solar|panel/.test(text)) return "standard";
  return "custom";
}

function productCategory(p) {
  const kind = clean(p.kind || p.priceCategoryName || "未分类");
  const shape = clean(p.priceCategoryName || p.kind || "待维护产品形态");
  return { root: kind, shape };
}

function variantName(p) {
  const method = controlMethodCode(p);
  const labels = {
    motor: "电动销售变体",
    chain: "拉珠销售变体",
    standard: "标准品入口",
    custom: "自定义销售变体"
  };
  return labels[method] || labels.custom;
}

function componentCatalog(p) {
  const map = new Map();
  p.componentMappings.forEach(item => {
    const rawName = clean(item.componentName);
    if (!rawName) return;
    const code = clean(item.componentProductId || rawName.toUpperCase().replace(/[^A-Z0-9]+/g, "_"));
    const existing = map.get(code);
    if (existing) {
      existing.bindCount += 1;
      if (existing.samples.length < 3) existing.samples.push(item.optionName);
      return;
    }
    map.set(code, {
      code,
      name: bilingual(rawName, COMPONENT_CN),
      bindCount: 1,
      samples: [item.optionName].filter(Boolean)
    });
  });
  return [...map.values()].sort((a, b) => b.bindCount - a.bindCount);
}

function slotGroup(slot) {
  const name = clean(slot.name).toLowerCase();
  if (/fabric|material|color/.test(name)) return "面料/颜色";
  if (/control|motor|chain|direction/.test(name)) return "控制/配件方案";
  if (/mount|roll|rail|cassette|channel/.test(name)) return "结构安装";
  if (/solar|pole/.test(name)) return "配件";
  return "基础信息";
}

function questionPurpose(slot) {
  const name = clean(slot.name).toLowerCase();
  if (/direction/.test(name)) return "决定操作侧或电机出线侧，不代表控制方式。";
  if (/motor|chain|control/.test(name)) return "决定当前销售变体下的控制方案和自动配件。";
  if (/fabric|material|color/.test(name)) return "决定面料/颜色范围，并影响价格和生产物料。";
  if (/mount|roll|rail|cassette|channel/.test(name)) return "决定安装结构、外观件和生产加工方式。";
  if (/solar|pole/.test(name)) return "决定是否额外带出配件。";
  return "进入报价、订单和生产快照。";
}

function automationRows(p) {
  const hasMotor = controlMethodCode(p) === "motor";
  return [
    ["显示/隐藏", `当前变体 ${controlMethodCode(p)}`, hasMotor ? "显示电机、电源、遥控等问题；隐藏拉珠专属问题。" : "显示当前变体对应的问题；隐藏无关控制问题。"],
    ["必填校验", `${p.slots.filter(slot => slot.mandatory === "Y").length} 个必填问题`, "下单时必填问题未答不能提交。"],
    ["答案范围", `${productOptionsCount(p)} 个答案`, "按分类、产品模型、销售变体和面料组筛选可选答案。"],
    ["自动带组件", `${p.componentMappings.length} 条绑定`, "客户选中答案后自动带出面料、电机、太阳能板、底杆等组件。"],
    ["尺寸/能力", "宽高、面料重量、管径、电机型号", "超宽、超高、面料过重时提示双电机、换电机、禁选或 BLOCKER。"]
  ];
}

function releaseChecks(p) {
  return [
    ["分类清楚", productCategory(p).root && productCategory(p).shape],
    ["销售变体清楚", Boolean(controlMethodCode(p))],
    ["问题完整", p.slots.length > 0],
    ["答案完整", productOptionsCount(p) > 0],
    ["组件绑定", p.componentMappings.length > 0 || p.kind === "标准配件"],
    ["价格样本", p.priceSamples.length > 0],
    ["可销售说明", true]
  ];
}

function pill(text, cls = "") {
  return `<span class="tag ${cls}">${esc(text)}</span>`;
}

function infoRow(label, value) {
  return `<div class="overview-info-row"><span>${esc(label)}</span><strong>${esc(value || "待维护")}</strong></div>`;
}

function listRows(rows) {
  return `<div class="overview-list">${rows.map(row => `<div class="overview-list-row"><strong>${esc(row[0])}</strong><span>${esc(row[1])}</span><p>${esc(row[2])}</p></div>`).join("")}</div>`;
}

function renderProductList() {
  $("overviewProductList").innerHTML = productsForList().map((item, index) => `
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

function render() {
  const p = product();
  if (!p) return;
  const category = productCategory(p);
  const components = componentCatalog(p);
  const checks = releaseChecks(p);
  const passed = checks.filter(item => item[1]).length;

  renderProductList();
  $("overviewTitle").textContent = p.name;
  $("overviewSummary").textContent = `${category.root} / ${category.shape} / ${variantName(p)}。销售看到的是配置结果说明，不需要进入后台字段。`;
  $("overviewStatus").innerHTML = [
    pill(`${p.slots.length} 问题`, "accent"),
    pill(`${productOptionsCount(p)} 选项`, "accent"),
    pill(`${components.length} 组件`, ""),
    pill(`${passed}/${checks.length} 发布检查`, passed === checks.length ? "ok" : "warn")
  ].join("");

  $("identityPanel").innerHTML = `
    <div class="overview-info">
      ${infoRow("一级分类", category.root)}
      ${infoRow("二级产品形态", category.shape)}
      ${infoRow("产品模型", p.name)}
      ${infoRow("销售变体", variantName(p))}
      ${infoRow("变体维度", `system = ${controlMethodCode(p)}`)}
      ${infoRow("销售模式", p.kind === "标准配件" ? "标准品 / 固定 SKU" : "定制品 / 按单配置")}
    </div>
  `;

  $("questionsPanel").innerHTML = `
    <div class="overview-question-stack">
      ${p.slots.slice(0, 10).map((slot, index) => `
        <div class="overview-question">
          <span class="question-seq">${index + 1}</span>
          <div>
            <strong>${esc(bilingual(slot.name, SLOT_CN))}</strong>
            <p>${esc(questionPurpose(slot))}</p>
          </div>
          <span class="tag">${esc(slotGroup(slot))}</span>
          <span class="tag ${slot.mandatory === "Y" ? "danger" : ""}">${slot.mandatory === "Y" ? "必填" : "可选"}</span>
          <span class="tag accent">${slot.options.length} 答案</span>
        </div>
      `).join("")}
      ${p.slots.length > 10 ? `<div class="overview-more">还有 ${p.slots.length - 10} 个问题，销售可进入下单模拟查看完整表单。</div>` : ""}
    </div>
  `;

  $("automationPanel").innerHTML = listRows(automationRows(p));

  $("priceBomPanel").innerHTML = `
    <div class="overview-split">
      <div>
        <h3>价格来源</h3>
        ${listRows([
          ["基础价格", p.priceSamples.length ? `${p.priceSamples.length} 个矩阵样本` : "固定价或待维护", "按分类、产品模型、销售变体维度、宽高命中价格。"],
          ["选配加价", "电机、太阳能板、服务、特殊结构", "矩阵基础价之外输出价格明细。"],
          ["用量价格", "面料面积 / 型材长度 / 电机数量", "价格和 BOM 共用同一套用量结果。"]
        ])}
      </div>
      <div>
        <h3>物料组件</h3>
        <div class="overview-component-list">
          ${components.slice(0, 8).map(item => `
            <div>
              <strong>${esc(item.name)}</strong>
              <span>${esc(item.bindCount)} 条答案绑定</span>
            </div>
          `).join("") || `<div class="empty">标准品或当前样本未抽到组件绑定。</div>`}
        </div>
      </div>
    </div>
  `;

  $("salesChecklist").innerHTML = checks.map(([label, ok]) => `
    <div class="overview-check ${ok ? "ok" : "warn"}">
      <strong>${esc(ok ? "通过" : "待补")}</strong>
      <span>${esc(label)}</span>
    </div>
  `).join("");

  $("simulationPanel").innerHTML = `
    <p class="overview-note">销售如果要像客户一样试选一遍，进入业务原型；如果要修改配置，回到录入工作台。</p>
    <div class="overview-actions">
      <a class="entry-btn" href="./index.html">进入下单模拟</a>
      <a class="entry-btn secondary" href="./config-center.html">进入录入工作台</a>
    </div>
  `;
}

document.addEventListener("click", event => {
  const productButton = event.target.closest("[data-product-id]");
  if (!productButton) return;
  state.productId = productButton.dataset.productId;
  render();
});

$("overviewSearch").addEventListener("input", event => {
  state.search = event.target.value;
  renderProductList();
});

render();
