import type { CuttingConfig, WorkbenchSelection } from "../types";

export function templatesForConfig(config: CuttingConfig, selection: WorkbenchSelection) {
  const product = config.products.find((item) => item.key === selection.product) || config.products[0];
  const orderType = config.order_types.find((item) => item.key === selection.orderType) || config.order_types[0];
  const wrap = config.wrap_modes.find((item) => item.key === selection.wrapMode) || config.wrap_modes[0];
  const materials = selection.materials.length ? selection.materials : config.defaults.materials;
  const roll = `${selection.rollTotalLength || config.defaults.roll_total_length}-${selection.rollHeadWaste}-${selection.rollTailWaste}`;
  return [
    {
      label: `${product?.label || "产品"} + ${orderType?.label || "订单"}`,
      value: [
        selection.orderNo ? `订单号：${selection.orderNo}` : "",
        `订单类型：${orderType?.label || selection.orderType}`,
        `产品：${product?.label || selection.product}`,
        `门幅 ${materials.join("m 和 ")}m`,
        `面料可用长度 ${roll}`,
        wrap?.label || "不包",
        "宽度 高度 数量",
        "1.395 2.695 1",
        "1.315 2.695 1",
      ]
        .filter(Boolean)
        .join("\n"),
    },
    {
      label: "大货规格",
      value: [
        selection.orderNo ? `订单号：${selection.orderNo}` : "",
        "订单类型：大货",
        `产品：${product?.label || selection.product}`,
        `门幅 ${materials.join("m 和 ")}m`,
        `面料可用长度 ${roll}`,
        wrap?.label || "不包",
        "宽度 高度 数量",
        "1.2 1.8 100",
      ]
        .filter(Boolean)
        .join("\n"),
    },
  ];
}

export function welcomeText() {
  return [
    "你好，我是布料裁剪 Agent。",
    "",
    "左侧先设置订单号、产品、订单类型、包布和卷长；你可以新增产品或改倍率，不需要再改代码。",
    "",
    "我会先整理清单并给你确认，确认后再计算用料、卷数、损耗和报告生成费用。报告页会优先显示表格，长图默认折叠。",
  ].join("\n");
}
