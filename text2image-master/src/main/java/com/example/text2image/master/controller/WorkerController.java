package com.example.text2image.master.controller;

import com.example.text2image.common.dto.WorkerInfo;
import com.example.text2image.master.service.WorkerRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workers")
public class WorkerController {

    @Autowired
    private WorkerRegistryService registry;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody WorkerInfo info) {
        registry.register(info);
        return ResponseEntity.ok().build();
    }
}
