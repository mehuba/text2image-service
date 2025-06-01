package com.example.text2image.worker.service;

import com.example.text2image.common.dto.WorkerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class WorkerStartupRegister implements ApplicationRunner {


    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public void run(ApplicationArguments args) {
        WorkerInfo info = new WorkerInfo();
        info.setId(UUID.randomUUID().toString()); // 或从配置文件中读取固定ID
        info.setAddress(getComfyUIPublicUrl()); // Worker 的访问地址
        String masterRegisterUrl = System.getenv("MASTER_URL") + "/api/workers/register";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 某些服务器默认拒绝非浏览器类请求, 必须加这个header否则报错403
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        HttpEntity<WorkerInfo> entity = new HttpEntity<>(info, headers);
        restTemplate.exchange(masterRegisterUrl, HttpMethod.POST, entity, Void.class);
        System.out.println("✅ Worker registered to master: " + masterRegisterUrl);
    }

    public String getComfyUIPublicUrl() {
        String podId = System.getenv("RUNPOD_POD_ID");  // RunPod 提供的 POD 唯一 ID
        String port = "8188"; // ComfyUI 暴露端口
        return "https://" + podId + "-" + port + ".proxy.runpod.net";
    }

}