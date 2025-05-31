FROM ubuntu:22.04

# 安装依赖
RUN apt update && apt install -y \
    python3 python3-pip openjdk-8-jdk git wget

# 安装 ComfyUI
WORKDIR /workspace
RUN git clone https://github.com/comfyanonymous/ComfyUI.git
WORKDIR /workspace/ComfyUI
RUN pip install -r requirements.txt

# 拷贝 Java Worker 应用
COPY target/text2image-worker.jar /app/worker.jar

# 安装 StoryDiffusion 插件
RUN git clone https://github.com/smthemex/ComfyUI_StoryDiffusion.git /workspace/ComfyUI/custom_nodes/ComfyUI_StoryDiffusion \
    && pip install -r /workspace/ComfyUI/custom_nodes/ComfyUI_StoryDiffusion/requirements.txt

# 拷贝预定义工作流 JSON（可选）
COPY story_workflow.json /ComfyUI/user/default/workflows/story_workflow.json

# 启动脚本
COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 8080 8188

CMD ["/start.sh"]
