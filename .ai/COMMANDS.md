# Commands

## Environment

- OS: Windows 11
- Shell: PowerShell
- Workspace: `D:\work\base-boot`

## Safe Read Commands

```powershell
git status --short
Get-ChildItem -Force
Get-ChildItem -Path .ai -Recurse -Filter *.md -File
rg --files
Get-Content -Raw -Encoding UTF8 <file>
rg -n "<pattern>" <paths>
git diff --check -- <paths>
```

## Useful Static Searches

```powershell
rg -n "Content-Language|content-language|LocaleResolver|MessageUtils|SysI18nMessageService" .
rg -n "formatUtc|parseUtc|toUtcPayload|withUtcDateRange|parseTime|addDateRange" admin-ui\src
rg -n "LocalDateTime\.now|new Date\(|DateUtils\.getNowDate|ZoneId\.systemDefault|TimeZone\.getDefault" bocoo-admin bocoo-common bocoo-modules-system bocoo-modules-generator
rg -n "[\u4e00-\u9fff]" admin-ui\src -g "*.ts" -g "*.vue" -g "!**/locales/**"
rg -n "@Scheduled|cron|XxlJob|xxl|job" bocoo-admin bocoo-common bocoo-modules-system bocoo-modules-generator admin-ui\src
rg -n "JsonFormat|java\.util\.Date|parseTime\(|yyyy-MM-dd HH:mm:ss" bocoo-modules-generator admin-ui\src bocoo-common bocoo-modules-system
```

## Build/Test Commands

Project rules require pausing and getting explicit approval before running these.

```powershell
mvn -DskipTests compile
cd admin-ui; npm run typecheck
cd admin-ui; npm run build
```

## Commands Not Run During Context Compaction

```powershell
mvn test
mvn compile
npm run typecheck
npm run build
npm run dev
```
