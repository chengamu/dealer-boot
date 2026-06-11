# HK Agent

Python 多 Agent 服务骨架。当前内置第一个子 Agent：`fabric_cutting`，用于布料裁剪排料、损耗计算和 H5 工艺指导卡生成。

## Run

```bash
uv run hk-agent
```

或：

```bash
uv run uvicorn web.app:app --reload
```

打开：

```text
http://127.0.0.1:8000
```

## APIs

- `GET /api/agents`：查看已注册 Agent。
- `POST /api/agents/{agent_id}/run`：通过统一入口运行子 Agent。
- `POST /api/fabric-cutting/calculate`：结构化布料裁剪计算。
- `POST /api/fabric-cutting/calculate-excel`：上传标准 Excel 清单并计算。
- `GET /reports/{report_id}`：打开 H5 工艺指导卡。

## LLM Env

支持 OpenAI-compatible 配置：

```text
DS_API_KEY
DS_BASE_URL
DS_MODEL
```

也兼容：

```text
DEEPSEEK_API_KEY / DEEPSEEK_BASE_URL
OPENAI_API_KEY / OPENAI_BASE_URL / OPENAI_MODEL
```

没有配置模型时，结构化计算和基础文本解析仍可运行。
