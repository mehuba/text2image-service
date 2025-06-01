package com.example.text2image.master.service;

import com.example.text2image.common.dto.WorkerInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkerRegistryService {
    private final Map<String, WorkerInfo> workers = new ConcurrentHashMap<>();

    public void register(WorkerInfo info) {
        workers.put(info.getId(), info);
    }

    public String selectLeastLoadedWorker() {
        int minUsage = 100;
        String selected = workers.get(workers.keySet().iterator().next()).getAddress();

        for (Map.Entry<String, WorkerInfo> worker : workers.entrySet()) {
            try {
                ResponseEntity<Map> res = new RestTemplate().getForEntity(worker.getValue().getAddress() + "/gpu-usage", Map.class);
                Integer usage = (Integer) res.getBody().get("usage");
                if (usage < minUsage) {
                    minUsage = usage;
                    selected = worker.getValue().getAddress();
                }
            } catch (Exception ignored) {

            }
        }

        return selected; // 返回最优 worker URL
    }
}