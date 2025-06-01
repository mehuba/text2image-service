package com.example.text2image.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskStatus {

    public enum Status { PENDING, RUNNING, SUCCESS, FAILED, CANCELED }

    private String taskId;
    private Status status;
    private List<String> imageUrls;
    private String errorMessage;
}
