package ru.aten.telegram_bot.openai.api;

import java.util.List;

import lombok.Builder;
import ru.aten.telegram_bot.dto.MessageDTO;

@Builder
public record ChatCompletionRequest(
        String model,
        List<MessageDTO> messages) {

}
