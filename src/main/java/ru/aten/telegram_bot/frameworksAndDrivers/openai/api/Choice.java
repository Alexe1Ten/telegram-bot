package ru.aten.telegram_bot.frameworksAndDrivers.openai.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import ru.aten.telegram_bot.useCases.dto.MessageDTO;

public record Choice(
        @JsonProperty("message")
        MessageDTO message) {

}
