package com.example.text2image.master.service;

import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.WorkerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 某些服务器默认拒绝非浏览器类请求, 必须加这个header否则报错403
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            HttpEntity<TaskRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(workerUrl + "/internal/execute", HttpMethod.POST, entity, Void.class);
        } else {
            // 记录重试机制
        }
    }
}
