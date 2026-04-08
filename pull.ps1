# ============================================================
#  粮仓温湿度监控系统 - Git 拉取更新脚本
#  远程仓库: https://github.com/Dadel1on/grain_project.git
# ============================================================

$Host.UI.RawUI.WindowTitle = "粮仓监控系统 - Git 拉取更新"
$ROOT = $PSScriptRoot

function Write-Banner {
    Write-Host ""
    Write-Host "  ╔══════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "  ║       粮仓温湿度监控系统  拉取更新       ║" -ForegroundColor Cyan
    Write-Host "  ╚══════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Step($msg) { Write-Host "▶ $msg" -ForegroundColor Yellow }
function Write-OK($msg)   { Write-Host "  ✔ $msg" -ForegroundColor Green }
function Write-Fail($msg) { Write-Host "  ✘ $msg" -ForegroundColor Red }

Write-Banner

# ── 检查 Git ─────────────────────────────────────────────────
Write-Step "检查 Git 环境..."
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Fail "未找到 Git，请安装 Git 并配置 PATH"
    Read-Host "按 Enter 退出"; exit 1
}
Write-OK "Git: $(git --version)"

# ── 进入项目根目录 ───────────────────────────────────────────
Set-Location $ROOT

# ── 显示当前状态 ─────────────────────────────────────────────
Write-Host ""
Write-Step "当前仓库状态..."
$branch  = git rev-parse --abbrev-ref HEAD 2>&1
$remote  = git remote get-url origin 2>&1
Write-Host "  分支  : $branch" -ForegroundColor DarkCyan
Write-Host "  远程  : $remote" -ForegroundColor DarkCyan
Write-Host "  本地最新提交:" -ForegroundColor DarkCyan
git log --oneline -3 | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }

# ── 检查本地未提交变更 ───────────────────────────────────────
Write-Host ""
Write-Step "检查本地变更..."
$status = git status --porcelain 2>&1
if ($status) {
    Write-Host "  ⚠ 检测到以下未提交的本地变更：" -ForegroundColor DarkYellow
    $status | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }
    Write-Host ""
    Write-Host "  请选择处理方式：" -ForegroundColor White
    Write-Host "  [1] 暂存本地变更 (git stash)，拉取后自动恢复" -ForegroundColor White
    Write-Host "  [2] 放弃本地变更，强制更新（不可撤销）" -ForegroundColor White
    Write-Host "  [3] 取消，手动处理后再运行此脚本" -ForegroundColor White
    Write-Host ""
    $choice = Read-Host "  请输入选项 [1/2/3]"

    switch ($choice.Trim()) {
        "1" {
            Write-Step "暂存本地变更 (git stash)..."
            git stash push -m "pull-script-auto-stash-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
            if ($LASTEXITCODE -ne 0) {
                Write-Fail "git stash 失败，请手动处理后重试"
                Read-Host "按 Enter 退出"; exit 1
            }
            Write-OK "本地变更已暂存"
            $doRestore = $true
        }
        "2" {
            Write-Step "放弃本地变更..."
            git checkout -- .
            git clean -fd
            Write-OK "本地变更已丢弃"
            $doRestore = $false
        }
        "3" {
            Write-Host "  已取消，未做任何修改。" -ForegroundColor DarkYellow
            Read-Host "按 Enter 退出"; exit 0
        }
        default {
            Write-Fail "无效选项，已取消"
            Read-Host "按 Enter 退出"; exit 1
        }
    }
} else {
    Write-OK "工作区干净，无未提交变更"
    $doRestore = $false
}

# ── 拉取最新代码 ─────────────────────────────────────────────
Write-Host ""
Write-Step "正在从远程拉取最新代码 (origin/$branch)..."
git fetch origin 2>&1 | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }

$mergeOutput = git merge "origin/$branch" 2>&1
$mergeOutput | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }

if ($LASTEXITCODE -ne 0) {
    Write-Fail "合并失败，可能存在冲突，请手动解决后运行："
    Write-Host "  git status" -ForegroundColor DarkGray
    Write-Host "  git mergetool" -ForegroundColor DarkGray
    Read-Host "按 Enter 退出"; exit 1
}

if ($mergeOutput -match "Already up to date") {
    Write-OK "代码已是最新，无需更新"
} else {
    Write-OK "代码拉取成功"
    Write-Host ""
    Write-Host "  最新提交记录：" -ForegroundColor DarkCyan
    git log --oneline -5 | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }
}

# ── 恢复暂存的变更 ───────────────────────────────────────────
if ($doRestore) {
    Write-Host ""
    Write-Step "恢复暂存的本地变更 (git stash pop)..."
    $popOutput = git stash pop 2>&1
    $popOutput | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkGray }
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ⚠ 恢复暂存变更时出现冲突，请手动运行 git stash pop 处理" -ForegroundColor DarkYellow
    } else {
        Write-OK "本地变更已恢复"
    }
}

# ── 提示重启服务 ─────────────────────────────────────────────
Write-Host ""
Write-Host "  ╔══════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "  ║           拉 取 更 新 完 成               ║" -ForegroundColor Green
Write-Host "  ╠══════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "  ║  如需使新代码生效，请重新启动服务：       ║" -ForegroundColor DarkYellow
Write-Host "  ║    .\stop.ps1  →  .\start.ps1            ║" -ForegroundColor DarkYellow
Write-Host "  ╚══════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

Read-Host "按 Enter 关闭此窗口"
