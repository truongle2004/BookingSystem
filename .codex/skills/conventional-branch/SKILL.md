---
name: conventional-branch
description: 'Create Git branches that follow the Conventional Branch spec (feature/, bugfix/, hotfix/, release/, chore/). Use when creating a branch, naming a branch, or checking a branch name.'
---

# Conventional Branch

Format: `<type>/<description>`

## Types

| Type | Alias | Use for |
|------|-------|---------|
| `feature/` | `feat/` | New features |
| `bugfix/` | `fix/` | Bug fixes |
| `hotfix/` | | Urgent production fixes |
| `release/` | | Releases (dots allowed: `release/v1.2.0`) |
| `chore/` | | Deps, docs, config |

`main`, `master`, and `develop` are trunk branches. They have no prefix, and you must not create new branches with those names.

## Rules

- Lowercase letters, digits, hyphens, and dots only (dots only in `release/`)
- No spaces, underscores, or special characters
- No consecutive hyphens or dots, and no leading or trailing hyphen or dot
- Use kebab-case with 2-5 words, e.g. `feature/add-oauth-login`
- Include a ticket number if given, e.g. `feature/issue-123-add-oauth`

## Workflow

1. **Get the type and description.** Default to `feature` if unclear.
2. **Fix the name** if it breaks a rule: lowercase it, turn spaces and underscores into hyphens, and collapse repeated hyphens.
3. **Find the base branch:**
```bash
   git symbolic-ref --short refs/remotes/origin/HEAD 2>/dev/null | sed 's|^origin/||'
```
   If empty, use the first that exists: `develop`, `main`, `master`.
4. **Create it:**
```bash
   git checkout <base> && git pull origin <base>
   git checkout -b <type>/<description>
```
5. **Confirm** the new branch name and remind the user to run `git push -u origin <branch-name>`.