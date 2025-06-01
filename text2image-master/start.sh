#!/bin/bash

# 启动 Redis（后台）
redis-server /etc/redis/redis.conf &

# 启动 Spring Boot Master 服务
java -jar /app/master.jar