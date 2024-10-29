package ru.aten.telegram_bot.openai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.dto.MessageDTO;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.openai.api.ChatCompletionRequest;
import ru.aten.telegram_bot.openai.api.ChatGptHistoryService;
import ru.aten.telegram_bot.openai.api.OpenAiClient;

@Service
@AllArgsConstructor
public class ChatGPTService {

    private final OpenAiClient openAiClient;
    private final ChatGptHistoryService chatGptHistoryService;
    private final ContextService contextService;

    @Nonnull
    public String getResponseChatForUser(
            Long userId,
            String userTextInput) {
        chatGptHistoryService.createHistoryIfNotExist(userId);

        User me = User.builder()
                    .telegramId((long)835880897)
                    .firstName("Алексей")
                    .lastName("Тен")
                    .patronymic("Витальевич")
                    .position("Заместитель Управляющего Отделом Плитка")
                .build();

        List<MessageDTO> contextMessages = contextService.getContextForUser(userId);

        MessageDTO userMessageDTO = new MessageDTO();
        userMessageDTO.setContent(userTextInput);
        userMessageDTO.setRole("user");

        MessageDTO systemMessageDTO = new MessageDTO();
        systemMessageDTO.setRole("system");
        systemMessageDTO.setContent("Сегодня 28.10.2024");

        var historyDTO = chatGptHistoryService.addMessageToHistory(
                userId,
                userMessageDTO
        );

        List<MessageDTO> messages = new ArrayList<>();
        messages.add(userMessageDTO);
        messages.add(systemMessageDTO);

        var request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(messages)
                .build();

        var response = openAiClient.createChatCompletion(request);

        var messageFromGpt = response.choices().get(0).message();

        chatGptHistoryService.addMessageToHistory(userId, messageFromGpt);

        System.out.println(request);

        return messageFromGpt.getContent();
    }
}
