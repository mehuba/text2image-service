package com.example.text2image.master.service;


import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {

    private final Map<String, TaskStatus> taskStore = new ConcurrentHashMap<>();
//    private final MasterTaskDispatcher dispatcher = new MasterTaskDispatcher();
    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String submitTask(TaskRequest request) {
        String taskId = UUID.randomUUID().toString();
        request.setTaskId(taskId);
        TaskStatus status = new TaskStatus();
        status.setTaskId(taskId);
        status.setStatus(TaskStatus.Status.PENDING);
        taskStore.put(taskId, status);
        try {
            String json = objectMapper.writeValueAsString(request);
            redisTemplate.opsForList().rightPush("text2img:tasks", json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enqueue task", e);
        }
//        dispatcher.dispatchTask(request, taskId);
        return taskId;
    }

    public TaskStatus getTaskStatus(String taskId) {
        return taskStore.getOrDefault(taskId, null);
    }

    public void cancelTask(String taskId) {
        TaskStatus status = taskStore.get(taskId);
        if (status != null && status.getStatus() == TaskStatus.Status.PENDING) {
            status.setStatus(TaskStatus.Status.CANCELED);
        }
    }
}
