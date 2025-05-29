package com.example.text2image.worker.service;

import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.TaskStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class ComfyUiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String comfyUiUrl = "http://localhost:8188";  // ComfyUI 本地 API

    public TaskStatus executeGeneration(TaskRequest request) {
        TaskStatus status = new TaskStatus();
        status.setTaskId(request.getTaskId());
        status.setStatus(TaskStatus.Status.RUNNING);
        Map<String, String> body = new HashMap<>();
        try {
            // 1. 构建 ComfyUI 的 JSON 请求（以下结构需根据实际 workflow 编辑器导出）
            body.put("prompt", request.getPrompt());
            body.put("workflow", "your_workflow_name");

            // 2. 向 ComfyUI API 提交请求
            Map result = restTemplate.postForObject(comfyUiUrl + "/prompt", body, Map.class);

            // 3. 等待执行完成（可选：轮询 / 回调）
            Thread.sleep(5000);  // 模拟等待生成完成

            // 4. 读取生成图片结果
            String imagePath = "/path/to/output/image.png"; // ComfyUI 默认输出路径（需提前配置）
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                status.setStatus(TaskStatus.Status.FAILED);
                status.setErrorMessage("Image not found");
                return status;
            }

            // 5. 拷贝或上传图片到公共目录
            String publicDir = "./static/results/";
            Files.createDirectories(Paths.get(publicDir));
            String newName = UUID.randomUUID() + ".png";
            Path target = Paths.get(publicDir + newName);
            Files.copy(imageFile.toPath(), target);

            // 6. 设置状态完成
            status.setStatus(TaskStatus.Status.SUCCESS);
            status.setImageUrl("/results/" + newName);
            return status;

        } catch (Exception e) {
            status.setStatus(TaskStatus.Status.FAILED);
            status.setErrorMessage(e.getMessage());
            return status;
        }
    }
}