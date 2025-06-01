package com.example.text2image.master.controller;


import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.TaskStatus;
import com.example.text2image.master.service.RedisTaskConsumer;
import com.example.text2image.master.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TaskController {
    private final Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private TaskService taskService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateImage(@RequestBody TaskRequest request) {
        logger.info("Generating image {}", request);
        String taskId = taskService.submitTask(request);
        return ResponseEntity.ok(taskId);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskStatus> getStatus(@PathVariable String taskId) {
        return ResponseEntity.ok(taskService.getTaskStatus(taskId));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> cancelTask(@PathVariable String taskId) {
        taskService.cancelTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
