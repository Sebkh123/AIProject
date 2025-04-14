package com.example.aiproject.Service;

import com.example.aiproject.DTOmodel.TranslateRequestDTO;
import com.example.aiproject.DTOmodel.TranslateResponseDTO;
import com.example.aiproject.DTOmodel.detectedTranslateLangDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.json.JSONArray;


@Service
public class TranslationService {

    @Autowired
    private WebClient webClient;

    @Value("${openai.api.key}")
    private String openaiApiKey;





//




/*

    public Mono<TranslateResponseDTO> translate(TranslateRequestDTO requestDTO) {

        // Constructing the dynamic translation prompt
        String prompt = "Translate the following text from " + requestDTO.getSourceLanguage() +
                " to " + requestDTO.getTargetLanguage() + ": " + requestDTO.getText();

        return webClient.post()
                .uri("/chat/completions")  // Correct endpoint without extra '/v1'
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(new HashMap<String, Object>() {{
                    put("model", "gpt-3.5-turbo");  // Specify GPT-3.5 Turbo model
                    put("messages", new Object[] {
                            new HashMap<String, String>() {{
                                put("role", "user");
                                put("content", prompt);
                            }}
                    });
                    put("max_tokens", 100);  // Optional: maximum number of tokens
                    put("temperature", 0.7);  // Optional: controls the randomness
                }})
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                System.out.println("Error Response Body: " + responseBody);  // Log the error response body
                                return Mono.error(new RuntimeException("API error: " + responseBody));
                            });
                })
                .bodyToMono(TranslateResponseDTO.class)  // Map response to DTO
                .doOnNext(response -> {
                    // Log the translated text here
                    System.out.println("Translated Text: " + response.getTranslatedText());
                })
                .doOnTerminate(() -> System.out.println("Translation API call finished"));
    }


 */

//    public Mono<TranslateResponseDTO> translate(TranslateRequestDTO requestDTO) {
//        String inputText = requestDTO.getText().trim();
//        boolean isSingleWord = !inputText.contains(" ");
//
//        final int numberOfSuggestions;
//        String prompt;
//
//        if (isSingleWord) {
//            prompt = String.format(
//                    "Translate the word '%s' from %s to %s and provide 3 different possible translations.",
//                    inputText,
//                    requestDTO.getSourceLanguage(),
//                    requestDTO.getTargetLanguage()
//            );
//            numberOfSuggestions = 3;
//        } else {
//            prompt = String.format(
//                    "Translate the following sentence from %s to %s:\n%s",
//                    requestDTO.getSourceLanguage(),
//                    requestDTO.getTargetLanguage(),
//                    inputText
//            );
//            numberOfSuggestions = 1;
//        }
//
//
//        return webClient.post()
//                .uri("/chat/completions")
//                .header(HttpHeaders.CONTENT_TYPE, "application/json")
//                .bodyValue(new HashMap<String, Object>() {{
//                    put("model", "gpt-3.5-turbo");
//                    put("messages", new Object[]{
//                            new HashMap<String, String>() {{
//                                put("role", "user");
//                                put("content", prompt);
//                            }}
//                    });
//                    put("max_tokens", 150);
//                    put("temperature", 0.7);
//                    put("n", numberOfSuggestions);  //
//                }})
//                .retrieve()
//                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
//                    return clientResponse.bodyToMono(String.class)
//                            .flatMap(responseBody -> {
//                                System.out.println("Error Response Body: " + responseBody);
//                                return Mono.error(new RuntimeException("API error: " + responseBody));
//                            });
//                })
//                .bodyToMono(TranslateResponseDTO.class)
//                .doOnNext(response -> {
//                    System.out.println("Translated Text: " + response.getTranslatedText());
//                    if (response.getChoices() != null) {
//                        response.getChoices().forEach(choice ->
//                                System.out.println("Choice: " + choice.getMessage().getContent())
//                        );
//                    }
//                })
//                .doOnTerminate(() -> System.out.println("Translation API call finished"));
//    }

    public Mono<TranslateResponseDTO> translate(TranslateRequestDTO requestDTO) {
        String inputText = requestDTO.getText().trim();
        boolean isSingleWord = !inputText.contains(" ");

        final int numberOfSuggestions;
        String prompt;

        if (isSingleWord) {
            prompt = String.format(
                    "Give exactly 5 direct translation options for the word '%s' from %s to %s, separated by commas only. No explanations, no numbering. Example: word1, word2, word3, word4, word5",
                    inputText,
                    requestDTO.getSourceLanguage(),
                    requestDTO.getTargetLanguage()
            );
            numberOfSuggestions = 1; // Get multiple suggestions
        } else {
            prompt = String.format(
                    "Translate the following sentence from %s to %s:\n%s",
                    requestDTO.getSourceLanguage(),
                    requestDTO.getTargetLanguage(),
                    inputText
            );
            numberOfSuggestions = 1;
        }

        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(new HashMap<String, Object>() {{
                    put("model", "gpt-3.5-turbo");
                    put("messages", new Object[]{
                            new HashMap<String, String>() {{
                                put("role", "user");
                                put("content", prompt);
                            }}
                    });
                    put("max_tokens", 150);
                    put("temperature", 0.7);
                    put("n", numberOfSuggestions);
                }})
                .retrieve()
                .bodyToMono(TranslateResponseDTO.class)
                .map(response -> {
                    if (isSingleWord && response.getChoices() != null && !response.getChoices().isEmpty()) {
                        // Combine content from all choices and split by comma
                        List<String> allTranslations = response.getChoices().stream()
                                .map(choice -> choice.getMessage().getContent())
                                .flatMap(content -> Arrays.stream(content.split(",")))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .distinct() // Optional: avoid duplicates
                                .collect(Collectors.toList());

                        if (!allTranslations.isEmpty()) {
                            response.setTranslatedText(allTranslations.get(0)); // First one is main
                            response.setSuggestions(allTranslations.subList(1, allTranslations.size())); // Rest are suggestions
                        } else {
                            response.setTranslatedText("No translation found.");
                            response.setSuggestions(Collections.emptyList());
                        }
                    } else {
                        // Full sentence translation
                        String fullText = response.getChoices().get(0).getMessage().getContent().trim();
                        response.setTranslatedText(fullText);
                        response.setSuggestions(Collections.emptyList());
                    }

                    return response;
                })
                .doOnNext(response -> {
                    System.out.println("Translated Text: " + response.getTranslatedText());
                    if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
                        System.out.println("Suggestions: " + response.getSuggestions());
                        System.out.println("API_KEY: " + System.getenv("API_KEY"));

                    }
                })
                .doOnTerminate(() -> System.out.println("Translation API call finished"));

    }





    private TranslateResponseDTO.Choice createChoice(String content) {
    TranslateResponseDTO.Message message = new TranslateResponseDTO.Message();
    message.setContent(content);
    message.setRole("assistant");

    TranslateResponseDTO.Choice choice = new TranslateResponseDTO.Choice();
    choice.setMessage(message);

    return choice;
}




    public String parseLanguage(String rawResponse) {
        try {
            JSONObject json = new JSONObject(rawResponse);
            JSONArray choices = json.getJSONArray("choices");
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message.getString("content").trim();
            // If the detected language is "Java", handle it as an exception
            if (content.equalsIgnoreCase("Java")) {
                return "Unknown"; // or some default value
            }
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }



    public Mono<detectedTranslateLangDTO> detectLanguage(String text) {
        ////String prompt = "What language is the following text written in? Just respond with the language name. Text: \"" + text + "\"";

        String prompt = "Identify the language of the following text, responding with the language name in English (e.g., Danish, Spanish, etc.): \"" + text + "\". Only reply with the language name.";


        return webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(new HashMap<String, Object>() {{
                    put("model", "gpt-3.5-turbo");
                    put("messages", new Object[] {
                            new HashMap<String, String>() {{
                                put("role", "user");
                                put("content", prompt);
                            }}
                    });
                    put("max_tokens", 20);
                    put("temperature", 0.0);
                }})
                .retrieve()
                .bodyToMono(String.class) // raw response first

                .doOnNext(response -> {
                    // Log the raw response to ensure it's what you expect
                    System.out.println("Raw response: " + response);
                })



                .map(response -> {
                    // Parse response and map it to DTO
                    detectedTranslateLangDTO dto = new detectedTranslateLangDTO();
                    dto.setLanguageDetected(parseLanguage(response)); // you’d implement this parser
                    dto.setConfidence(0.9); // hardcoded or estimate if needed
                    return dto;
                });
    }











    // Helper method to build the body for translation request
    private Object createRequestBody(TranslateRequestDTO requestDTO) {
        // You can customize this to match the structure required by OpenAI's API
        return new Object() {
            public final String model = "gpt-3.5-turbo";  // Choose the appropriate model
            public final String prompt = requestDTO.getText();
            public final int max_tokens = 100;
            public final double temperature = 0.7;
        };
    }
}
