#!/usr/bin/env bash
# 部署到测试服务器：本地构建 → scp 部署物 → 服务器重建镜像并启动（四件套 + app）
#
# 用法（Git Bash / Linux）:
#   SERVER_HOST=1.2.3.4 SERVER_USER=ubuntu ./scripts/deploy-test.sh
# 可选: SSH_PORT=22 REMOTE_DIR=/opt/radar SKIP_BUILD=1
set -euo pipefail
cd "$(dirname "$0")/.."

SERVER_HOST=${SERVER_HOST:?need SERVER_HOST}
SERVER_USER=${SERVER_USER:?need SERVER_USER}
SSH_PORT=${SSH_PORT:-22}
REMOTE_DIR=${REMOTE_DIR:-/opt/radar}
SSH_CMD="ssh -o StrictHostKeyChecking=accept-new -p $SSH_PORT $SERVER_USER@$SERVER_HOST"
SCP_CMD="scp -o StrictHostKeyChecking=accept-new -P $SSH_PORT"

echo "==> [1/4] 本地构建（JDK 21，可用 JAVA_HOME 覆盖）"
if [ "${SKIP_BUILD:-0}" != "1" ]; then
    mvn -q -s .mvn/settings.xml package -DskipTests
fi

echo "==> [2/4] 同步部署物到 $SERVER_USER@$SERVER_HOST:$REMOTE_DIR"
$SSH_CMD "sudo mkdir -p $REMOTE_DIR && sudo chown -R \$(whoami) $REMOTE_DIR"
$SCP_CMD target/radar.jar "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR/radar.jar"
$SCP_CMD Dockerfile docker-compose.yml "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR/"
$SCP_CMD -r sql resources "$SERVER_USER@$SERVER_HOST:$REMOTE_DIR/"

echo "==> [3/4] 服务器：启动/更新四件套 + 重建 app 镜像"
$SSH_CMD "cd $REMOTE_DIR && \
  docker compose up -d --wait && \
  docker compose --profile app up -d --build app"

echo "==> [4/4] 健康检查"
$SSH_CMD "sleep 15 && curl -sf http://localhost:6580/actuator/health && echo '' && \
  curl -sf -o /dev/null -w 'admin UI http code: %{http_code}\n' http://localhost:6580/"
echo "部署完成: http://$SERVER_HOST:6580"
