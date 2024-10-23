package ru.aten.telegram_bot.openai.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import ru.aten.telegram_bot.dto.MessageDTO;

public record Choice(
        @JsonProperty("message")
        MessageDTO message) {

}
