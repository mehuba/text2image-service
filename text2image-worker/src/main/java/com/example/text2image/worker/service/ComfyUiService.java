package com.example.text2image.worker.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Service
public class ComfyUiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String callComfyUI(String prompt) {
        // 假设 ComfyUI 本地服务地址
        String url = "http://localhost:8188/generate";
        Map<String, String> body = new HashMap<>();
        body.put("prompt", prompt);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
        // 根据实际返回结构调整
        return response.getBody().get("image_url").toString();
    }
}