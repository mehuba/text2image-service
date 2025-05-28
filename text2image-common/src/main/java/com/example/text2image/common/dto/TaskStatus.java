package com.example.text2image.common.dto;

import lombok.Data;

@Data
public class TaskStatus {

    public enum Status { PENDING, RUNNING, SUCCESS, FAILED, CANCELED }

    private String taskId;
    private Status status;
    private String imageUrl;
    private String errorMessage;
}
