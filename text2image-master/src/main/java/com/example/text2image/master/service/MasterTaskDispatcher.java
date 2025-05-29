package com.example.text2image.master.service;

import com.example.text2image.common.dto.TaskRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class MasterTaskDispatcher {

    private final List<String> workerUrls = Arrays.asList(
            "http://worker1:8081",
            "http://worker2:8082"
    );


    public String selectLeastLoadedWorker() {
        int minUsage = 100;
        String selected = null;

        for (String url : workerUrls) {
            try {
                ResponseEntity<Map> res = new RestTemplate().getForEntity(url + "/gpu-usage", Map.class);
                Integer usage = (Integer) res.getBody().get("usage");
                if (usage < minUsage) {
                    minUsage = usage;
                    selected = url;
                }
            } catch (Exception ignored) {

            }
        }

        return selected; // 返回最优 worker URL
    }

    public void dispatchTask(TaskRequest request, String taskId) {
        String workerUrl = selectLeastLoadedWorker();
        if (workerUrl != null) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(workerUrl + "/internal/execute", request, Void.class);
        } else {
            // 记录重试机制
        }
    }
}
