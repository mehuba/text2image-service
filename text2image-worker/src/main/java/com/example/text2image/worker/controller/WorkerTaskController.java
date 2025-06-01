package com.example.text2image.worker.controller;

import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.TaskStatus;
import com.example.text2image.worker.service.ComfyUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal")
public class WorkerTaskController {

    @Value("${master.callback.url}")
    private String masterCallbackUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private ComfyUiService comfyUiService;

    @PostMapping("/execute")
    public ResponseEntity<Void> executeTask(@RequestBody TaskRequest request) {
        CompletableFuture.runAsync(() -> {
            System.out.println("Executing prompt: " + request.getPrompt());
            // 调用 ComfyUI 逻辑，保存图片，更新状态
            TaskStatus status = comfyUiService.executeGeneration(request);
            System.out.println("Task " + request.getTaskId() + " status: " + status.getStatus());
            callbackMaster(status);
        });
        return ResponseEntity.ok().build();
    }

    private void callbackMaster(TaskStatus status) {
        try {
            restTemplate.postForEntity(masterCallbackUrl, status, Void.class);
        } catch (Exception e) {
            System.err.println("Failed to callback master: " + e.getMessage());
        }
    }
}
