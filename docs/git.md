# Git Workflow & Best Practices

## 1. Professional Git Feature Branch Workflow

```text
main branch
    ↓ (git switch -c feature/add-dto-mappers)
feature branch
    ↓ (code & test)
git commit -m "feat: refactor DTOs into request and response packages"
    ↓
git push origin feature/add-dto-mappers
    ↓
Pull Request (Code Review & CI Build)
    ↓
Merge into main
```

---

## 2. Essential Git Commands Reference

```bash
# Initialize repository
git init

# Check status of modified / untracked files
git status

# Stage files for commit
git add .

# Commit staged changes with message
git commit -m "refactor: extract entity mappers from service layer"

# Branch management
git branch                   # List branches
git switch -c feature/branch # Create and switch to new branch
git switch main              # Switch back to main branch

# Merging & Syncing
git merge feature/branch

# Inspection & Stashing
git log --oneline
git diff
git stash
git stash pop
```

---

## 3. Security & Ignored Files (`.gitignore`)
Never commit sensitive credentials, JWT secrets, local `.env` files, compiled target artifacts, or IDE settings.
See [`.gitignore`](file:///c:/Users/91891/.gemini/antigravity-ide/scratch/expense-tracker-api/.gitignore).
