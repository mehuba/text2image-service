package com.example.text2image.worker.controller;

import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.TaskStatus;
import com.example.text2image.common.dto.WorkerInfo;
import com.example.text2image.worker.service.ComfyUiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/internal")
public class WorkerTaskController {

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
        String masterCallbackUrl = System.getenv("MASTER_URL") + "/api/v1/tasks/callback";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 某些服务器默认拒绝非浏览器类请求, 必须加这个header否则报错403
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        HttpEntity<TaskStatus> entity = new HttpEntity<>(status, headers);

        try {
            restTemplate.exchange(masterCallbackUrl, HttpMethod.POST, entity, Void.class);
            System.out.println("Callback master: " + masterCallbackUrl + status);
        } catch (Exception e) {
            System.err.println("Failed to callback master: " + e.getMessage());
        }
    }
}
