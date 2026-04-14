# 新机器 clone 后：在仓库根目录执行 .\setup.ps1
# 依赖：已安装 JDK 17+（与 backend 要求一致）、Node.js 18+、npm
#
# 以下由本脚本通过 Gradle / npm 生成，不在 Git 中（另一台电脑需重新执行本脚本或等价命令）：
#   - backend\build、backend\.gradle（及若设置了 GRADLE_USER_HOME 则该目录）
#   - frontend\node_modules、frontend\dist
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

Write-Host ">> 新机器请先确认（Git 不跟踪、需在本机准备）："
Write-Host "   1) MySQL：创建库 nscp_dev（或与 application.properties 中 URL 一致），并执行脚本："
Write-Host "      $root\backend\src\main\resources\db\stage1_schema.sql"
Write-Host "   2) 编辑 backend\src\main\resources\application.properties：数据源地址/账号/密码与本机 MySQL 一致。"
Write-Host "   3) 若使用自定义 GRADLE_USER_HOME（例如 backend\.gradle-home），该目录已在 .gitignore 中，勿提交。"
Write-Host ""

Write-Host ">> Backend: Gradle 拉取依赖并编译（compileJava）..."
Push-Location (Join-Path $root "backend")
& .\gradlew.bat compileJava --no-daemon
if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
Pop-Location

Write-Host ">> Frontend: npm ci（按 package-lock 安装）..."
Push-Location (Join-Path $root "frontend")
npm ci
if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
Pop-Location

Write-Host "完成。可选：在仓库根或 frontend 使用 .env* 做前端环境变量时，勿提交真实密钥（见根目录 .gitignore）。"
