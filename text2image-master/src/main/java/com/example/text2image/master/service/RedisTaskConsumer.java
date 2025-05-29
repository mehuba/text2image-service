package com.example.text2image.master.service;

import com.example.text2image.common.dto.TaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RedisTaskConsumer {

    @Autowired
    private MasterTaskDispatcher masterTaskDispatcher;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisTaskConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void pollTasks() {
        String json = redisTemplate.opsForList().leftPop("text2img:tasks");
        if (json != null) {
            try {
                TaskRequest task = objectMapper.readValue(json, TaskRequest.class);
                masterTaskDispatcher.dispatchTask(task, task.getTaskId());
            } catch (Exception e) {
                System.err.println("Error dispatching task: " + e.getMessage());
            }
        }
    }
}