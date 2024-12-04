package ru.aten.telegram_bot.frameworksAndDrivers.openai.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatCompletionResponse(
        @JsonProperty("choices")
        List<Choice> choices) {
}
