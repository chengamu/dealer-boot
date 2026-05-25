# AI Context Compaction Template

Use this when `.ai` context becomes too large and needs to keep only current, long-lived project knowledge.

## Goal

Compress `.ai` context, keep long-term effective rules, and archive historical migration process.

## Read First

- Read `AGENTS.md`.
- Read all markdown files under `.ai/`.
- Understand current state before editing documents.

## Keep Active

- `AGENTS.md`
- `.ai/CURRENT.md`
- `.ai/TASKS.md`
- `.ai/HANDOFF.md`
- `.ai/DECISIONS.md`
- `.ai/BUGS.md`
- `.ai/COMMANDS.md`

## Create

- `.ai/archive/`

## Archive

Move historical details into `.ai/archive/*.md`, especially:

- UTC modernization process
- i18n modernization process
- generator migration process
- historical phase records

## Active File Rules

- `.ai/CURRENT.md` should keep only current technical rules, current architecture state, current development stage, and current remaining TODO.
- `.ai/TASKS.md` should keep only unfinished current tasks.
- `.ai/HANDOFF.md` should keep only the latest status summary.
- Remove duplicate content.
- Remove completed historical running logs.
- Do not delete long-term effective rules.

## Safety Rules

- Do not modify business code.
- Do not run build/test.
- Keep changes limited to documentation/context files unless explicitly requested.

## Final Output

Report:

- Which long-term rules were kept.
- Which historical content was archived.
- Current `.ai` structure.
