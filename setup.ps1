# 新机器 clone 后：在仓库根目录执行 .\setup.ps1
# 依赖：已安装 JDK 17+（与 backend 要求一致）、Node.js 18+、npm
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

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

Write-Host "完成。若后端需数据库/本地配置，请自行复制或新建被 .gitignore 忽略的配置文件（如 .env、application-local.properties）。"
