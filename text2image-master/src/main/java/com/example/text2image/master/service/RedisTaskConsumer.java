package com.example.text2image.queue;

import com.example.text2image.common.dto.TaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RedisTaskConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${worker.url:http://localhost:8081}")
    private String workerUrl;

    public RedisTaskConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void pollTasks() {
        String json = redisTemplate.opsForList().leftPop("text2img:tasks");
        if (json != null) {
            try {
                TaskRequest task = objectMapper.readValue(json, TaskRequest.class);
                restTemplate.postForEntity(workerUrl + "/internal/execute", task, Void.class);
            } catch (Exception e) {
                System.err.println("Error dispatching task: " + e.getMessage());
            }
        }
    }
}