package com.example.aiproject.Controller;

import com.example.aiproject.DTOmodel.TranslateRequestDTO;
import com.example.aiproject.DTOmodel.TranslateResponseDTO;
import com.example.aiproject.DTOmodel.detectedTranslateLangDTO;
import com.example.aiproject.DTOmodel.languageRequestDTO;
import com.example.aiproject.Service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:63343") // Allow cross-origin requests (for frontend integration)
public class TranslationController {

    @Autowired
    private TranslationService translationService;


//
//    @PostMapping("/translate")
//    public Mono<TranslateResponseDTO> translate(@RequestBody TranslateRequestDTO requestDTO) {
//        System.out.println("Received request for translation: " + requestDTO);
//        System.out.println("Source Language: " + requestDTO.getSourceLanguage());
//        System.out.println("Target Language: " + requestDTO.getTargetLanguage());
//        System.out.println("Text: " + requestDTO.getText());
//
//        return translationService.translate(requestDTO);
//    }


    @PostMapping("/translate")
    public ResponseEntity<TranslateResponseDTO> translate(@RequestBody TranslateRequestDTO request) {
        // Call the translation service
        Mono<TranslateResponseDTO> response = translationService.translate(request);

        // Log the translation result
        String translatedText = response.block().getTranslatedText();
        System.out.println("Translated Text: " + translatedText);

        return ResponseEntity.ok(response.block());
    }




    @GetMapping("/testT")
    public String checkService() {
        return "it is up and running";
    }



    /*
    @PostMapping("/detectedLanguage")
    public Mono<detectedTranslateLangDTO> detectedLanguage(@RequestParam String text) {
       return translationService.detectLanguage(text);


    }

     */

    @PostMapping("/detectedLanguage")
    public Mono<detectedTranslateLangDTO> detectedLanguage(@RequestBody languageRequestDTO request) {
        return translationService.detectLanguage(request.getText());
    }



}



