#!/usr/bin/env bash
# 新机器 clone 后：在仓库根目录执行 bash setup.sh 或 chmod +x setup.sh && ./setup.sh
# 依赖：JDK 17+、Node.js 18+、npm
#
# 以下由本脚本通过 Gradle / npm 生成，不在 Git 中（另一台电脑需重新执行本脚本或等价命令）：
#   - backend/build、backend/.gradle（及若设置了 GRADLE_USER_HOME 则该目录）
#   - frontend/node_modules、frontend/dist
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo ">> 新机器请先确认（Git 不跟踪、需在本机准备）："
echo "   1) MySQL：创建库 nscp_dev（或与 application.properties 中 URL 一致），并执行脚本："
echo "      $ROOT/backend/src/main/resources/db/stage1_init.sql"
echo "   2) 编辑 backend/src/main/resources/application.properties：数据源地址/账号/密码与本机 MySQL 一致。"
echo "   3) 若使用自定义 GRADLE_USER_HOME（例如 backend/.gradle-home），该目录已在 .gitignore 中，勿提交。"
echo ""

echo ">> Backend: Gradle 拉取依赖并编译（compileJava）..."
(cd "$ROOT/backend" && ./gradlew compileJava --no-daemon)

echo ">> Frontend: npm ci（按 package-lock 安装）..."
(cd "$ROOT/frontend" && npm ci)

echo "完成。可选：在仓库根或 frontend 使用 .env* 做前端环境变量时，勿提交真实密钥（见根目录 .gitignore）。"
