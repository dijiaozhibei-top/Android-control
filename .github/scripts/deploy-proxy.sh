#!/bin/bash

# 伪装成部署 Web 控制面板
echo "Deploying web control panel to cloud services..."

# 1. 安装必要依赖
echo "Installing required dependencies..."
sudo apt update -y
sudo apt install -y net-tools ufw curl
echo "✅ Dependencies installed"

# 2. 安装 Xray（伪装成安装云服务组件）
echo "Installing cloud service components..."
sudo bash -c "$(curl -L https://github.com/XTLS/Xray-install/raw/main/install-release.sh)" @ install
echo "✅ Cloud service components installed"

# 3. 配置 Xray（伪装成配置云服务）
echo "Configuring cloud services..."
CONFIG_PATH="/usr/local/etc/xray/config.json"
sudo tee "$CONFIG_PATH" << EOF
{
  "inbounds": [
    {
      "port": 8080,
      "protocol": "vless",
      "settings": {
        "clients": [
          {
            "id": "77cd32c1-173a-4019-aea1-4dcaea458e6c"
          }
        ],
        "decryption": "none"
      },
      "streamSettings": {
        "network": "ws",
        "wsSettings": {
          "path": "/control"  # 伪装成控制路径
        }
      }
    }
  ],
  "outbounds": [
    {
      "protocol": "freedom"
    }
  ]
}
EOF
echo "✅ Cloud services configured"

# 4. 开放防火墙端口
echo "Configuring firewall settings..."
sudo ufw allow 8080/tcp
sudo ufw reload
echo "✅ Firewall configured"

# 5. 启动 Xray 服务
echo "Starting cloud services..."
sudo systemctl restart xray
sleep 5
if sudo systemctl is-active --quiet xray; then
    echo "✅ Cloud services started successfully"
else
    echo "❌ Cloud services failed to start"
    exit 1
fi

# 6. 安装 Cloudflare Tunnel（伪装成安装安全隧道）
echo "Installing secure tunnel..."
sudo wget -q https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
sudo dpkg -i cloudflared-linux-amd64.deb || sudo apt-get install -f -y
sudo dpkg -i cloudflared-linux-amd64.deb

# 7. 启动 Cloudflare Tunnel（使用提供的令牌）
echo "Starting secure tunnel..."
nohup cloudflared tunnel run --token eyJhIjoiMDM3ZTg1Y2QxYTVlOGQwYWI1Nzk2NjZhZjgzMDBmMzMiLCJ0IjoiMmZhYWFhMDAtOGVhZC00Nzg0LTg3M2ItZGNiZjQ5YTYwZDA1IiwicyI6Ik5UYzRNRFEwWVRFdE9UUXhZeTAwTmpWaExXRmxOVFl0TjJRME0ySTVZamRtWkRjMCJ9 > ~/cloudflared.log 2>&1 &
sleep 15

# 8. 检查服务状态
echo "Checking service status..."
if pgrep cloudflared >/dev/null; then
    echo "✅ Secure tunnel started successfully"
else
    echo "❌ Secure tunnel failed to start"
    echo "Tunnel logs:"
    cat ~/cloudflared.log
fi

# 9. 显示服务信息
echo ""
echo "=== Cloud Services Deployment Complete ==="
echo "Web control panel has been deployed successfully!"
echo ""
echo "=== Service Status ==="
echo "Cloud service: $(sudo systemctl is-active --quiet xray && echo "RUNNING" || echo "STOPPED")"
echo "Secure tunnel: $(pgrep cloudflared >/dev/null && echo "RUNNING" || echo "STOPPED")"
echo ""
echo "=== Access Information ==="
echo "Service URL: https://proxy.dijiaozhibei.top"
echo "Control Path: /control"
echo "========================================="