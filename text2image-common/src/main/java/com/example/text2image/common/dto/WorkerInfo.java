package com.example.text2image.common.dto;

import lombok.Data;

@Data
public class WorkerInfo {
    private String id;
    private String address; // http://ip:port
    private double gpuUsage;
    private boolean available;
}
