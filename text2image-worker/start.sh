#!/bin/bash

# 注入 RunPod 的 SSH 公钥（从环境变量）
echo "$RUNPOD_SSH_KEY" > /root/.ssh/authorized_keys
chmod 600 /root/.ssh/authorized_keys

# 启动 SSHD
service ssh start
# 启动 ComfyUI
cd /ComfyUI || exit
python3 main.py --listen 0.0.0.0 --port 8188 &

# 等待 ComfyUI 启动
sleep 10

# 启动 Java Worker
java -jar /app/worker.jar