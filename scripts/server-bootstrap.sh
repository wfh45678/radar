#!/usr/bin/env bash
# 测试服务器初始化（Ubuntu 24.04，需 sudo）:
# Docker + compose 插件 + JDK 21 + Maven + unzip + 2G swap，全部走系统源（腾讯云内网镜像速度快）。
# 用法: sudo bash server-bootstrap.sh
set -euo pipefail

echo "==> Docker + compose 插件 + JDK21 + Maven（Ubuntu 24.04 源内齐全）"
export DEBIAN_FRONTEND=noninteractive
apt-get update -y
apt-get install -y docker.io docker-compose-v2 openjdk-21-jdk-headless maven unzip curl git rsync

echo "==> 配置 registry mirror 与日志轮转"
mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://docker.1ms.run"
  ],
  "log-driver": "json-file",
  "log-opts": {"max-size": "10m", "max-file": "3"}
}
EOF
systemctl enable --now docker
systemctl restart docker

echo "==> docker 组加入 ubuntu（新登录会话生效）"
usermod -aG docker ${SUDO_USER:-ubuntu} || true

echo "==> 2G swap（4G 内存机兜底）"
if [ "$(free -m | awk '/^Swap/{print $2}')" -lt 2000 ]; then
    fallocate -l 2G /swapfile || dd if=/dev/zero of=/swapfile bs=1M count=2048
    chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
    grep -q swapfile /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

echo "==> 版本核对"
docker --version
docker compose version
java -version 2>&1 | head -1
mvn -version | head -1
free -m | grep -E "Mem|Swap"
