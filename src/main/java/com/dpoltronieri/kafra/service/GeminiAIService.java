package com.dpoltronieri.kafra.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiAIService {

    private final ChatLanguageModel chatLanguageModel;

    public GeminiAIService(@Value("${spring.ai.gemini_api_key}") String key) {
        String apiKey = key;
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable not set!");
        }

        chatLanguageModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.0-flash") // Or "gemini-1.0-pro" - check Gemini API docs
                .temperature(0.7)      // Adjust temperature as needed
                .build();
    }

    public String summarizeText(String text) {
        String prompt = "Summarize the following forum post messages:\n\n" + text + "\n\nProvide a concise summary.";
        try {
            return chatLanguageModel.chat(prompt);
        } catch (Exception e) {
            System.err.println("Error calling Gemini API (via LangChain4j): " + e.getMessage());
            return "Error summarizing the forum post.";
        }
    }

    public String summarizeText(String header, String tailer, String text) {
        String prompt = header + "\n"
        + text + "\n"
        + tailer; 
        try {
            return chatLanguageModel.chat(prompt);
        } catch (Exception e) {
            System.err.println("Error calling Gemini API (via LangChain4j): " + e.getMessage());
            return "Error summarizing the forum post.";
        }
    }
}