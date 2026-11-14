#!/usr/bin/env bash
# 测试服务器初始化（Ubuntu/Debian 系，CentOS 请自行替换 apt 为 yum/dnf）:
# 安装 Docker + compose 插件 + JDK 21 + Maven + Git，配置国内镜像加速。
# 用法: ./scripts/server-bootstrap.sh   （在服务器上执行，需 sudo 权限）
set -euo pipefail

echo "==> 系统基础"
sudo apt-get update -y
sudo apt-get install -y ca-certificates curl git rsync

echo "==> Docker + compose 插件"
if ! command -v docker >/dev/null 2>&1; then
    curl -fsSL https://get.docker.com | sudo sh
fi
sudo usermod -aG docker "${SUDO_USER:-$USER}" || true
sudo mkdir -p /etc/docker
cat <<'EOF' | sudo tee /etc/docker/daemon.json
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://dockerproxy.net"
  ],
  "log-driver": "json-file",
  "log-opts": {"max-size": "10m", "max-file": "3"}
}
EOF
sudo systemctl enable --now docker
sudo systemctl restart docker

echo "==> JDK 21（Temurin，apt 源；失败则手动解压 zip 到 /opt/jdk-21）"
if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q '"21'; then
    sudo apt-get install -y temurin-21-jdk || {
      sudo mkdir -p /opt/jdk-21
      curl -fsSL https://mirrors.huaweicloud.com/openjdk/21.0.2/openjdk-21.0.2_linux-x64_bin.zip -o /tmp/jdk21.zip
      sudo unzip -q /tmp/jdk21.zip -d /opt && sudo mv /opt/jdk-21.0.2 /opt/jdk-21
      echo "export JAVA_HOME=/opt/jdk-21" | sudo tee /etc/profile.d/jdk21.sh
      echo 'export PATH=$JAVA_HOME/bin:$PATH' | sudo tee -a /etc/profile.d/jdk21.sh
    }
fi

echo "==> Maven"
if ! command -v mvn >/dev/null 2>&1; then
    sudo apt-get install -y maven || {
      curl -fsSL https://mirrors.tuna.tsinghua.edu.cn/apache/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz -o /tmp/mvn.tgz
      sudo tar -xzf /tmp/mvn.tgz -C /opt && sudo ln -sf /opt/apache-maven-3.9.11/bin/mvn /usr/local/bin/mvn
    }
fi

echo "==> 4G 内存机器建议加 2G swap"
if [ "$(free -m | awk '/^Swap/{print $2}')" -lt 2000 ] 2>/dev/null; then
    sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
    sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
    grep -q swapfile /etc/fstab || echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

echo "==> 完成。注意: docker 组需重新登录生效；JAVA_HOME 需 source /etc/profile.d/jdk21.sh"
docker --version; mvn -version 2>/dev/null | head -1; java -version 2>&1 | head -1
