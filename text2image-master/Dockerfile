FROM ubuntu:22.04

# 安装必要组件
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk redis-server curl && \
    apt-get clean

# 设置工作目录
WORKDIR /app

# 复制已构建的 Spring Boot JAR 包（从主机）
COPY target/text2image-master.jar /app/master.jar

# 暴露服务端口（供 RunPod Worker 调用）
EXPOSE 8080 6379

# 启动脚本
COPY start.sh /start.sh
RUN chmod +x /start.sh

CMD ["/start.sh"]