package com.example.text2image.worker.controller;
import com.example.text2image.common.dto.TaskRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal")
public class WorkerTaskController {

    @PostMapping("/execute")
    public ResponseEntity<Void> executeTask(@RequestBody TaskRequest request) {
        CompletableFuture.runAsync(() -> {
            System.out.println("Executing prompt: " + request.getPrompt());
            // 调用 ComfyUI 逻辑，保存图片，更新状态
        });
        return ResponseEntity.ok().build();
    }
}
