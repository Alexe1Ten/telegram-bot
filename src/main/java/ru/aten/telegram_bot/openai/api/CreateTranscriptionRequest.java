package ru.aten.telegram_bot.openai.api;

import java.io.File;

import lombok.Builder;

@Builder
public record CreateTranscriptionRequest(
        File audioFile,
        String model) {

}
