package ru.aten.telegram_bot.openai.api;

import java.util.List;

import lombok.Builder;

@Builder
public record ChatHistory(
        List<Message> chatMessages) {

}
