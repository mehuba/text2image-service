package com.example.text2image.master.service;

import com.example.text2image.common.dto.TaskRequest;
import com.example.text2image.common.dto.WorkerInfo;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkerRegistryService {
    private final Map<String, WorkerInfo> workers = new ConcurrentHashMap<>();
    private RestTemplate restTemplate = new RestTemplate();

    public void register(WorkerInfo info) {
        workers.put(info.getId(), info);
    }

    public String selectLeastLoadedWorker() {
        int minUsage = 100;
        String selected = workers.get(workers.keySet().iterator().next()).getAddress();

        for (Map.Entry<String, WorkerInfo> worker : workers.entrySet()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                // 某些服务器默认拒绝非浏览器类请求, 必须加这个header否则报错403
                headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                HttpEntity<TaskRequest> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> res = restTemplate.exchange(worker.getValue().getAddress() + "/gpu-usage", HttpMethod.GET, entity, Map.class);
                Integer usage = (Integer) res.getBody().get("usage");
                if (usage < minUsage) {
                    minUsage = usage;
                    selected = worker.getValue().getAddress();
                }
            } catch (Exception ignored) {

            }
        }
        System.out.println("Selected worker: " + selected);
        return selected; // 返回最优 worker URL
    }
}