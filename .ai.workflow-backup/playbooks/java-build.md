# Java 构建 Playbook

仅在 Maven / jar / compile / runtime 问题出现时按需加载。

## 检查项

- 运行构建相关命令前，先确认命令目标和模块。
- 如果运行行为和源码不一致，检查本地 Maven 产物是否过期。
- 未确认产物或版本不一致前，不要先修改业务逻辑。

## 常见坑：Maven 旧 jar / 未 install

遇到类 / 方法版本不一致，或运行行为和源码不一致时，先检查本地 Maven install 和旧 jar，不要第一时间修改业务逻辑。

## 常见坑：mapper XML 打包检查

- 如果 `target/classes` 有 mapper XML，但模块 jar 没有，先比较 jar 时间和 `target/classes` 时间，再重新 `package/install`。
- 本项目的 Spring Boot 发布 jar 是 `bocoo-admin/target/dist/bocoo-admin.jar`，不是 `bocoo-admin/target/bocoo-admin.jar`。
- 检查发布包 mapper XML 时，需要抽查 `BOOT-INF/lib/bocoo-modules-*.jar` 内部的 `mapper/**/*.xml`。
