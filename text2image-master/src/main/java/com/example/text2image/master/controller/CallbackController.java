package com.example.text2image.master.controller;


import com.example.text2image.common.dto.TaskStatus;
import com.example.text2image.master.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
public class CallbackController {

    @Autowired
    private TaskService taskService;

    @PostMapping("/callback")
    public ResponseEntity<Void> receiveCallback(@RequestBody TaskStatus status) {
        taskService.updateTaskStatus(status);
        return ResponseEntity.ok().build();
    }
}

