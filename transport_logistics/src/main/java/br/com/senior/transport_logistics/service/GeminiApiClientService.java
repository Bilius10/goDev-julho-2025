package br.com.senior.transport_logistics.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;

public class GeminiApiClientService {

    @Value("${google.api.key}")
    private String apiKey;

    public String generateResponse(String prompt){
        ChatLanguageModel gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-1.5-flash")
                .build();

        return gemini.generate("Traduza para português o texto: " + prompt);
    }
}
