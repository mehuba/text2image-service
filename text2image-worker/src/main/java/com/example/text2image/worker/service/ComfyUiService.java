package com.example.text2image.worker.service;

import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;


@Service
public class ComfyUiService {

    @Value("classpath:data.json")
    private Resource resourceFile;
    @Value("${master.callback.url:http://localhost:8080/api/v1/tasks/callback}")
    private String masterCallbackUrl;
    @Value("${comfy.ui.url:https://localhost:8188/api}")
    private String comfyUiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    {
        restTemplate.getInterceptors().add((request, body, execution) -> {
            System.out.println("=== REQUEST HEADERS ===");
            request.getHeaders().forEach((k, v) -> System.out.println(k + ": " + v));
            System.out.println("URI: " + request.getURI());
            System.out.println("Method: " + request.getMethod());

            ClientHttpResponse response = execution.execute(request, body);

            System.out.println("=== RESPONSE HEADERS ===");
            response.getHeaders().forEach((k, v) -> System.out.println(k + ": " + v));

            return response;
        });
    }

    public TaskStatus executeGeneration(TaskRequest request) {
        TaskStatus status = new TaskStatus();
        status.setTaskId(request.getTaskId());
        status.setStatus(TaskStatus.Status.RUNNING);
        try {
            // 1. 构建 ComfyUI 的 JSON 请求
            Map promptBody = objectMapper.readValue(resourceFile.getFile(), Map.class);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 某些服务器默认拒绝非浏览器类请求, 必须加这个header否则报错403
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(promptBody, headers);
            // 2. 向 ComfyUI API 提交请求
            ResponseEntity<Map> response = restTemplate.exchange(comfyUiUrl + "/prompt", HttpMethod.POST, entity, Map.class);
            // 3. 等待执行完成（可选：轮询 / 回调）
            Thread.sleep(5000);  // 模拟等待生成完成
            System.out.println(response.getBody().toString());
            // 4. 读取生成图片结果
            String imagePath = "/path/to/output/image.png"; // ComfyUI 默认输出路径（需提前配置）
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                status.setStatus(TaskStatus.Status.FAILED);
                status.setErrorMessage("Image not found");
                callbackMaster(status);
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
            callbackMaster(status);
            return status;

        } catch (Exception e) {
            System.out.println(e);
            status.setStatus(TaskStatus.Status.FAILED);
            status.setErrorMessage(e.getMessage());
            callbackMaster(status);
            return status;
        }
    }

    private void callbackMaster(TaskStatus status) {
        try {
            restTemplate.postForEntity(masterCallbackUrl, status, Void.class);
        } catch (Exception e) {
            System.err.println("Failed to callback master: " + e.getMessage());
        }
    }
}
