package ru.aten.telegram_bot.openai.api;

import java.io.File;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TranscribeVoiceToTextService {

    private final OpenAiClient openAiClient;

    public String transcribe(File audioFile) {
        var response = openAiClient.createTranscription(
                CreateTranscriptionRequest.builder()
                        .audioFile(audioFile)
                        .model("whisper-1")
                        .build()
        );

        return response.text();
    }
}
