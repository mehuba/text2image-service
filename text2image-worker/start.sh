#!/bin/bash

# 启动 ComfyUI
cd /workspace/ComfyUI || exit
python3 main.py --listen 0.0.0.0 --port 8188 &

# 等待 ComfyUI 启动
sleep 10

# 启动 Java Worker
java -jar /app/worker.jar