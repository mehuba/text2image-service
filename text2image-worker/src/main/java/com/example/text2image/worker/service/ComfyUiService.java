package com.example.text2image.worker.service;

import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class ComfyUiService {

    @Value("classpath:template.json")
    private Resource resourceFile;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String comfyUiUrl = "https://mnqbr1rkrmsamm-3000.proxy.runpod.net/api";

    public TaskStatus executeGeneration(TaskRequest request) {
        TaskStatus status = new TaskStatus();
        status.setTaskId(request.getTaskId());
        status.setStatus(TaskStatus.Status.RUNNING);
        try {
            // 1. 构建 ComfyUI 的 JSON 请求
            Map promptBody = objectMapper.readValue(resourceFile.getFile(), Map.class);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.add("Host", "mnqbr1rkrmsamm-3000.proxy.runpod.net");
//            headers.setHost(new InetSocketAddress("mnqbr1rkrmsamm-3000.proxy.runpod.net", 0));
//            byte[] jsonBytes = Files.readAllBytes(resourceFile.getFile().toPath());
//            headers.setContentLength(jsonBytes.length); // 设置 Content-Length 手动
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(promptBody, headers);
            // 2. 向 ComfyUI API 提交请求
            ResponseEntity<Map> response = restTemplate.exchange(comfyUiUrl + "/prompt", HttpMethod.POST, entity, Map.class);

            // Step 3: 创建 HttpURLConnection 请求
//            URL url = new URL(comfyUiUrl + "/prompt");
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Host", "mnqbr1rkrmsamm-3000.proxy.runpod.net");
////            connection.setRequestProperty("Origin", "https://mnqbr1rkrmsamm-3000.proxy.runpod.net");
//            connection.setRequestProperty("Content-Length", String.valueOf(jsonBytes.length));  // 手动设置 Content-Length
//            connection.setDoOutput(true);
//
//            String jsonString = new String(jsonBytes);
//
//            try (OutputStream os = connection.getOutputStream()) {
//                byte[] input = jsonString.getBytes("utf-8");
//                os.write(input, 0, input.length);
//            }
//
//            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
//                StringBuilder response = new StringBuilder();
//                String responseLine;
//                while ((responseLine = br.readLine()) != null) {
//                    response.append(responseLine.trim());
//                }
//                System.out.println(response.toString());
//            }
            // 3. 等待执行完成（可选：轮询 / 回调）
            Thread.sleep(5000);  // 模拟等待生成完成
//            System.out.println(response.getBody().toString());
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
            System.out.println(e);
            status.setStatus(TaskStatus.Status.FAILED);
            status.setErrorMessage(e.getMessage());
            return status;
        }
    }
}