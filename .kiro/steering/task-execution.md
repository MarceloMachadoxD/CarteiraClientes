---
inclusion: manual
---

# Task Execution Rules

## Sequential Execution

- Always execute tasks from `tasks.md` in strict sequential order, one at a time.
- Never skip, reorder, or batch tasks together.

## No Parallel Delegation

- Do not delegate tasks to parallel sub-agents.
- All task execution must happen in the main context.

## Mandatory Stop After Each Task

- After completing each task, **stop immediately** and wait for explicit user feedback before proceeding.
- Only continue when the user provides approval (e.g., "LGTM") or a new instruction.
- Do not read ahead or begin the next task without this confirmation.
