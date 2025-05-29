package com.example.text2image.worker.controller;

import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.TaskStatus;
import com.example.text2image.worker.service.ComfyUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal")
public class WorkerTaskController {

    @Autowired
    private ComfyUiService comfyUiService;

    @PostMapping("/execute")
    public ResponseEntity<Void> executeTask(@RequestBody TaskRequest request) {
        CompletableFuture.runAsync(() -> {
            System.out.println("Executing prompt: " + request.getPrompt());
            // 调用 ComfyUI 逻辑，保存图片，更新状态
            TaskStatus status = comfyUiService.executeGeneration(request);
            System.out.println("Task " + request.getTaskId() + " status: " + status.getStatus());
        });
        return ResponseEntity.ok().build();
    }
}
