#!/usr/bin/env bash
# 构建前端（frontend/）并同步产物到 jar 内嵌静态目录 src/main/resources/static/
# 产物继续内嵌 jar，Dockerfile / compose 零改动；后端打包照常 mvn package
# 用法：在仓库根执行  scripts/build-frontend.sh
set -euo pipefail
cd "$(dirname "$0")/../frontend"

echo "[1/3] 安装依赖（npm ci 优先，失败回退 npm install）"
npm ci --no-audit --no-fund 2>/dev/null || npm install --no-audit --no-fund

echo "[2/3] vite build"
npm run build

echo "[3/3] 同步产物到 src/main/resources/static/（保留 images/ 与 res/）"
STATIC=../src/main/resources/static
# 清理旧构建产物：webpack 时代的根级 index.*.* / main.*.*，以及 Vite 的 static/ 子目录整体替换
find "$STATIC" -maxdepth 1 -type f \( -name 'index.*.*' -o -name 'main.*.*' \) -delete
rm -f "$STATIC/index.html"
rm -rf "$STATIC/static"
cp -r dist/. "$STATIC/"
echo "完成，static/ 现有内容："
ls "$STATIC"
