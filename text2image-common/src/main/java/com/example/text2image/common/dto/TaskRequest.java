package com.example.text2image.common.dto;


import lombok.Data;

@Data
public class TaskRequest {

    private String taskId;
    private String prompt;
}
