package com.example.text2image.common.dto;

import lombok.Data;

@Data
public class Prompt {

    private Integer width;
    private Integer height;
    private String roleText;
    private String sceneText;
    private String posText;
    private String negText;
}
