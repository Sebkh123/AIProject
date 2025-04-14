package com.example.aiproject.DTOmodel;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class detectedTranslateLangDTO {

    private String languageDetected;
    private double confidence;



}
