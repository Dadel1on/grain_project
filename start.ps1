# ============================================================
#  粮仓温湿度监控系统 - 一键启动脚本
#  后端: Spring Boot (端口 8081)
#  前端: Vue3 + Vite (端口 5173)
# ============================================================

$Host.UI.RawUI.WindowTitle = "粮仓监控系统 - 启动中..."

$ROOT    = $PSScriptRoot
$BACKEND = Join-Path $ROOT "backend"
$FRONTEND = Join-Path $ROOT "frontend"

function Write-Banner {
    Write-Host ""
    Write-Host "  ╔══════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "  ║       粮仓温湿度监控系统  启动脚本       ║" -ForegroundColor Cyan
    Write-Host "  ╚══════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Step($msg) {
    Write-Host "▶ $msg" -ForegroundColor Yellow
}

function Write-OK($msg) {
    Write-Host "  ✔ $msg" -ForegroundColor Green
}

function Write-Fail($msg) {
    Write-Host "  ✘ $msg" -ForegroundColor Red
}

# ── 检查依赖工具 ─────────────────────────────────────────────
function Test-Command($name) {
    return $null -ne (Get-Command $name -ErrorAction SilentlyContinue)
}

Write-Banner

Write-Step "检查依赖环境..."

if (-not (Test-Command "java")) {
    Write-Fail "未找到 Java，请安装 JDK 17 并配置 PATH"
    Read-Host "按 Enter 退出"; exit 1
}
Write-OK "Java: $(java -version 2>&1 | Select-String 'version' | Select-Object -First 1)"

if (-not (Test-Command "mvn")) {
    Write-Fail "未找到 Maven，请安装 Maven 并配置 PATH"
    Read-Host "按 Enter 退出"; exit 1
}
Write-OK "Maven: $(mvn -version 2>&1 | Select-Object -First 1)"

if (-not (Test-Command "node")) {
    Write-Fail "未找到 Node.js，请安装 Node.js 并配置 PATH"
    Read-Host "按 Enter 退出"; exit 1
}
Write-OK "Node.js: $(node -v)"

if (-not (Test-Command "npm")) {
    Write-Fail "未找到 npm，请确认 Node.js 安装完整"
    Read-Host "按 Enter 退出"; exit 1
}
Write-OK "npm: $(npm -v)"

# ── 安装前端依赖（若 node_modules 不存在）──────────────────
Write-Host ""
Write-Step "检查前端依赖..."
$nodeModules = Join-Path $FRONTEND "node_modules"
if (-not (Test-Path $nodeModules)) {
    Write-Host "  正在安装前端依赖（首次运行需要一段时间）..." -ForegroundColor DarkYellow
    Push-Location $FRONTEND
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Fail "前端依赖安装失败，请检查网络或手动运行 npm install"
        Pop-Location
        Read-Host "按 Enter 退出"; exit 1
    }
    Pop-Location
    Write-OK "前端依赖安装完成"
} else {
    Write-OK "前端依赖已就绪"
}

# ── 启动后端 ─────────────────────────────────────────────────
Write-Host ""
Write-Step "启动后端服务（Spring Boot，端口 8081）..."
$backendLog = Join-Path $ROOT "backend.log"
$backendProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "cd /d `"$BACKEND`" && mvn spring-boot:run > `"$backendLog`" 2>&1" `
    -PassThru -WindowStyle Normal
Write-OK "后端进程已启动 (PID: $($backendProc.Id))，日志: backend.log"

# ── 等待后端就绪 ─────────────────────────────────────────────
Write-Host ""
Write-Step "等待后端启动（最长 120 秒）..."
$timeout = 120
$elapsed = 0
$ready   = $false
while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 3
    $elapsed += 3
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:8081/api/devices" `
                    -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        $ready = $true
        break
    } catch { }
    Write-Host "  已等待 ${elapsed}s..." -ForegroundColor DarkGray
}

if ($ready) {
    Write-OK "后端已就绪 → http://localhost:8081"
} else {
    Write-Host "  ⚠ 后端未在预期时间内响应，可能仍在启动，请查看 backend.log" -ForegroundColor DarkYellow
}

# ── 启动前端 ─────────────────────────────────────────────────
Write-Host ""
Write-Step "启动前端服务（Vite，端口 5173）..."
$frontendProc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "cd /d `"$FRONTEND`" && npm run dev" `
    -PassThru -WindowStyle Normal
Write-OK "前端进程已启动 (PID: $($frontendProc.Id))"

Start-Sleep -Seconds 4

# ── 打开浏览器 ───────────────────────────────────────────────
Write-Host ""
Write-Step "在浏览器中打开系统..."
Start-Process "http://localhost:5173"

# ── 完成提示 ─────────────────────────────────────────────────
Write-Host ""
Write-Host "  ╔══════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "  ║           系 统 已 成 功 启 动            ║" -ForegroundColor Green
Write-Host "  ╠══════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "  ║  前端地址: http://localhost:5173          ║" -ForegroundColor Green
Write-Host "  ║  后端地址: http://localhost:8081          ║" -ForegroundColor Green
Write-Host "  ║  后端日志: backend.log                    ║" -ForegroundColor Green
Write-Host "  ╠══════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "  ║  关闭此窗口不会停止服务进程               ║" -ForegroundColor DarkYellow
Write-Host "  ║  如需停止服务，请运行 stop.ps1            ║" -ForegroundColor DarkYellow
Write-Host "  ╚══════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "  后端进程 PID: $($backendProc.Id)  前端进程 PID: $($frontendProc.Id)" -ForegroundColor DarkGray

# 保存 PID 供 stop.ps1 使用
"$($backendProc.Id)" | Out-File (Join-Path $ROOT ".backend.pid") -Encoding utf8
"$($frontendProc.Id)" | Out-File (Join-Path $ROOT ".frontend.pid") -Encoding utf8

Read-Host "`n按 Enter 关闭此窗口（服务将继续在后台运行）"
