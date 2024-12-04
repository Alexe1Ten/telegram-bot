package ru.aten.telegram_bot.frameworksAndDrivers.openai.api;

import java.util.List;

import lombok.Builder;
import ru.aten.telegram_bot.useCases.dto.MessageDTO;

@Builder
public record ChatCompletionRequest(
        String model,
        List<MessageDTO> messages) {

}
