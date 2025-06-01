package com.example.text2image.worker.controller;

import com.example.text2image.common.dto.WorkerInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RestController
public class WorkerInfoController {

    @GetMapping("/gpu-usage")
    public Map<String, Object> getGpuUsage() throws IOException {
        Process process = Runtime.getRuntime().exec("nvidia-smi --query-gpu=utilization.gpu --format=csv,noheader,nounits");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String usage = reader.readLine().trim();

        Map<String, Object> result = new HashMap<>();
        result.put("usage", Integer.parseInt(usage));
        return result;
    }
}
