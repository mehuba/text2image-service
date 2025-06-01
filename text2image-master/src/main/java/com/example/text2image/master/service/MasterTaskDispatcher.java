package com.example.text2image.master.service;

import com.example.text2image.common.dto.TaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class MasterTaskDispatcher {

    @Autowired
    private WorkerRegistryService registry;

    public void dispatchTask(TaskRequest request, String taskId) {
        String workerUrl = registry.selectLeastLoadedWorker();
        if (workerUrl != null) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(workerUrl + "/internal/execute", request, Void.class);
        } else {
            // 记录重试机制
        }
    }
}
