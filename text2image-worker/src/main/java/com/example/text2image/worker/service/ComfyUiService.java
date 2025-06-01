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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ComfyUiService {

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
            Map<String, Object> replacements = new HashMap<>();
            replacements.put("width", request.getPrompt().getWidth());
            replacements.put("height", request.getPrompt().getHeight());
            replacements.put("role_text", request.getPrompt().getRoleText());
            replacements.put("scene_text", request.getPrompt().getSceneText());
            replacements.put("pos_text", request.getPrompt().getPosText());
            replacements.put("neg_text", request.getPrompt().getNegText());

            String promptStr = loadAndReplace("data.json", replacements);
            Map promptBody = objectMapper.readValue(promptStr, Map.class);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 某些服务器默认拒绝非浏览器类请求, 必须加这个header否则报错403
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(promptBody, headers);
            // 2. 向 ComfyUI API 提交请求
            ResponseEntity<Map> promptResponse = restTemplate.exchange(comfyUiUrl + "/prompt", HttpMethod.POST, entity, Map.class);
            Map promptResult = promptResponse.getBody();
            System.out.println(promptResult);
            if (promptResult == null || promptResult.isEmpty()) {
                status.setStatus(TaskStatus.Status.FAILED);
                status.setErrorMessage("Prompt failed");
                return status;
            }
            // 3. 等待执行完成
            Thread.sleep(5000);
            Map historyResult = null;
            String promptId = promptResult.get("prompt_id").toString();
            while (historyResult == null || historyResult.isEmpty()) {
                ResponseEntity<Map> historyResponse = restTemplate.getForEntity(comfyUiUrl + "/history/" + promptId, Map.class);
                historyResult = historyResponse.getBody();
                Thread.sleep(3000);
            }
            Map<String, Object> map = (Map<String, Object>) historyResult.get(promptId);
            map = (Map<String, Object>) map.get("outputs");
            map = (Map<String, Object>) map.get("5");
            List<Map<String, Object>> images = (List<Map<String, Object>>) map.get("images");
            // 4. 设置状态完成
            status.setStatus(TaskStatus.Status.SUCCESS);
            List<String> imageUrls = images.stream()
                    .map(image -> getComfyUIPublicUrl() + "/view" + "?filename=" + image.get("filename"))
                    .collect(Collectors.toList());
            status.setImageUrls(imageUrls);

        } catch (Exception e) {
            System.out.println(e);
            status.setStatus(TaskStatus.Status.FAILED);
            status.setErrorMessage(e.getMessage());
            return status;
        }
        return status;
    }

    public String loadAndReplace(String resourcePath, Map<String, Object> placeholders) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            String content = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                content = content.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }

            return content;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load and replace json", e);
        }
    }

    public String getComfyUIPublicUrl() {
        String podId = System.getenv("RUNPOD_POD_ID");  // RunPod 提供的 POD 唯一 ID
        String port = "8188";
        return "https://" + podId + "-" + port + ".proxy.runpod.net";
    }
}
