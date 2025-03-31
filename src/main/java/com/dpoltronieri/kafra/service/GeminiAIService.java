package com.dpoltronieri.kafra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@Service
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);
    private final ChatLanguageModel chatLanguageModel;

    public GeminiAIService(@Value("${spring.ai.gemini_api_key}") String key) {
        String apiKey = key;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.error("GEMINI_API_KEY is not configured in application properties (spring.ai.gemini_api_key).");
            throw new IllegalStateException("GEMINI_API_KEY is not configured.");
        }

        try {
            log.info("Initializing GoogleAiGeminiChatModel with model 'gemini-2.0-flash'");
            chatLanguageModel = GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName("gemini-2.0-flash") // Or "gemini-1.0-pro" - check Gemini API docs
                    .temperature(0.7)      // Adjust temperature as needed
                    .build();
            log.info("GoogleAiGeminiChatModel initialized successfully.");
        } catch (Exception e) {
            log.error("Failed to initialize GoogleAiGeminiChatModel", e);
            throw new RuntimeException("Could not initialize Gemini AI Model", e);
        }
    }

    public String summarizeText(String text) {
        // This method seems less flexible than the one below. Consider deprecating or removing if unused.
        log.warn("Using basic summarizeText method. Consider using the version with header/tailer for better prompt control.");
        String prompt = "Summarize the following forum post messages:\n\n" + text + "\n\nProvide a concise summary.";
        return executeChat(prompt);
    }

    public String summarizeText(String header, String tailer, String text) {
        String prompt = header + "\n" + text + "\n" + tailer;
        return executeChat(prompt);
    }

    // Helper method to centralize chat execution and error logging
    private String executeChat(String prompt) {
        log.debug("Sending prompt to Gemini: {}", prompt); // Log prompt at debug level
        try {
            String response = chatLanguageModel.chat(prompt);
            log.debug("Received response from Gemini."); // Avoid logging potentially large response unless necessary
            return response;
        } catch (Exception e) {
            log.error("Error calling Gemini API (via LangChain4j): {}", e.getMessage(), e);
            // Return a generic error message or re-throw a custom exception
            return "Error communicating with the AI service.";
        }
    }
}
