package com.example.text2image.worker.service;

import com.example.text2image.common.dto.WorkerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class WorkerStartupRegister implements ApplicationRunner {

    @Value("${master.register.url}")
    private String masterRegisterUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public void run(ApplicationArguments args) {
        WorkerInfo info = new WorkerInfo();
        info.setId(UUID.randomUUID().toString()); // 或从配置文件中读取固定ID
        info.setAddress(getComfyUIPublicUrl()); // Worker 的访问地址
        info.setGpuUsage(0.0);
        info.setAvailable(true);

        try {
            restTemplate.postForObject(masterRegisterUrl, info, Void.class);
            System.out.println("✅ Worker registered to master");
        } catch (Exception e) {
            System.err.println("❌ Failed to register worker: " + e.getMessage());
        }
    }

    public String getComfyUIPublicUrl() {
        String podId = System.getenv("RUNPOD_POD_ID");  // RunPod 提供的 POD 唯一 ID
        String port = "8188"; // ComfyUI 暴露端口
        return "https://" + podId + "-" + port + ".proxy.runpod.net";
    }

}