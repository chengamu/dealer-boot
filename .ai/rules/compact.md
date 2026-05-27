## 9. /compact

`/compact` 是 Working Context Squash。

它不只是压缩 CURRENT，而是把 `.ai/CURRENT.md` 压缩成“另一个 Agent 明天接手也能继续工作”的最小必要上下文。

目标：

- 让另一个 Agent 仍然能继续工作。
- 让 `.ai/CURRENT.md` 恢复到最小工作状态。
- 避免把 `.ai/CURRENT.md` 变成聊天记录。

执行时：

1. 不修改业务代码。
2. 不清空 `.ai/RULES.md`、`.ai/CONTEXT.md`、`.ai/MEMORY.md`。
3. 精简 `.ai/CURRENT.md`。
4. 把可复用经验、重复踩坑、关键决策提炼到 `.ai/MEMORY.md`。
5. 不删除仍未解决的问题。

必须保留：

- 当前目标
- 当前状态
- 当前 blockers
- 当前风险
- 已完成
- 未完成
- 当前验证结果
- Remaining Risks
- 下一步

必须删除或压缩：

- 长推理
- 长日志
- 重复解释
- 已废弃方案
- 失败尝试
- 长 browser trace
- 长 network dump
- 长 console output
- 长 code-reviewer 输出
- 长 browser-debugger 输出
- 大段 build/test 输出

输出：

1. 压缩了哪些内容。
2. 保留了哪些关键信息。
3. 提炼了哪些经验。
4. 当前下一步。
