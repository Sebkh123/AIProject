package com.example.aiproject.DTOmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TranslateRequestDTO {

    private String text;
    private String sourceLanguage;
    private String targetLanguage;

    private boolean alternatives;


}
