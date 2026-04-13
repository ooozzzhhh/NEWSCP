#!/usr/bin/env bash
# 新机器 clone 后：在仓库根目录执行 bash setup.sh 或 chmod +x setup.sh && ./setup.sh
# 依赖：JDK 17+、Node.js 18+、npm
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo ">> Backend: Gradle 拉取依赖并编译（compileJava）..."
(cd "$ROOT/backend" && ./gradlew compileJava --no-daemon)

echo ">> Frontend: npm ci（按 package-lock 安装）..."
(cd "$ROOT/frontend" && npm ci)

echo "完成。若后端需数据库/本地配置，请自行复制或新建被 .gitignore 忽略的配置文件（如 .env、application-local.properties）。"
