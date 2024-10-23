package ru.aten.telegram_bot.openai.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatCompletionResponse(
        @JsonProperty("choices")
        List<Choice> choices) {
}
