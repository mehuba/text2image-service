FROM ubuntu:22.04

# 安装依赖
RUN apt update && apt install -y \
    python3 python3-pip openjdk-8-jdk git wget

# 安装 ComfyUI
WORKDIR /
RUN git clone https://github.com/comfyanonymous/ComfyUI.git
WORKDIR /ComfyUI
RUN pip install -r requirements.txt

# 拷贝 Java Worker 应用
COPY target/text2image-worker.jar /app/worker.jar

# 安装 StoryDiffusion 插件
RUN git clone https://github.com/smthemex/ComfyUI_StoryDiffusion.git /ComfyUI/custom_nodes/ComfyUI_StoryDiffusion \
    && pip install -r /ComfyUI/custom_nodes/ComfyUI_StoryDiffusion/requirements.txt

# 拷贝预定义工作流 JSON（可选）
COPY story_workflow.json /ComfyUI/user/default/workflows/story_workflow.json

# 安装 SSH 服务
RUN apt update && apt install -y openssh-server

# 创建 SSH 运行目录
RUN mkdir /var/run/sshd

# 设置 root 密码（可选，不建议远程登录用）
RUN echo 'root:runpod' | chpasswd

# 启用公钥登录
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN sed -i 's/#PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config

# 设置 authorized_keys 路径
RUN mkdir -p /root/.ssh && chmod 700 /root/.ssh

# 启动脚本
COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 8080 8188

CMD ["/start.sh"]
