# ============================================================
#  粮仓温湿度监控系统 - 一键停止脚本
# ============================================================

$ROOT = $PSScriptRoot

function Write-Step($msg) { Write-Host "▶ $msg" -ForegroundColor Yellow }
function Write-OK($msg)   { Write-Host "  ✔ $msg" -ForegroundColor Green }

Write-Host ""
Write-Host "  正在停止粮仓监控系统服务..." -ForegroundColor Cyan
Write-Host ""

function Stop-ByPidFile($label, $file) {
    $pidFile = Join-Path $ROOT $file
    if (Test-Path $pidFile) {
        $procId = Get-Content $pidFile -Raw
        $procId = $procId.Trim()
        try {
            $proc = Get-Process -Id $procId -ErrorAction Stop
            # 停止整个进程树（mvn/node 会产生子进程）
            $children = Get-CimInstance Win32_Process | Where-Object { $_.ParentProcessId -eq $procId }
            $children | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
            Write-OK "$label 已停止 (PID: $procId)"
        } catch {
            Write-Host "  ⚠ $label 进程 (PID: $procId) 已不存在或无法停止" -ForegroundColor DarkYellow
        }
        Remove-Item $pidFile -Force
    } else {
        Write-Host "  ⚠ 未找到 $label PID 文件，尝试按端口停止..." -ForegroundColor DarkYellow
    }
}

Stop-ByPidFile "后端服务" ".backend.pid"
Stop-ByPidFile "前端服务" ".frontend.pid"

# 兜底：按端口强杀
Write-Step "检查端口残留进程..."
@(8081, 5173) | ForEach-Object {
    $port = $_
    $conn = netstat -ano 2>$null | Select-String ":$port\s" | Select-Object -First 1
    if ($conn) {
        $pid2 = ($conn -split '\s+')[-1]
        try {
            Stop-Process -Id $pid2 -Force -ErrorAction Stop
            Write-OK "端口 $port 进程 (PID: $pid2) 已终止"
        } catch {
            Write-Host "  端口 $port 无残留进程" -ForegroundColor DarkGray
        }
    }
}

Write-Host ""
Write-Host "  所有服务已停止。" -ForegroundColor Green
Write-Host ""
Read-Host "按 Enter 关闭此窗口"
