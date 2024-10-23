package ru.aten.telegram_bot.openai;

import org.springframework.stereotype.Service;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.dto.MessageDTO;
import ru.aten.telegram_bot.openai.api.ChatCompletionRequest;
import ru.aten.telegram_bot.openai.api.ChatGptHistoryService;
import ru.aten.telegram_bot.openai.api.OpenAiClient;

@Service
@AllArgsConstructor
public class ChatGPTService {

    private final OpenAiClient openAiClient;
    private final ChatGptHistoryService chatGptHistoryService;

    @Nonnull
    public String getResponseChatForUser(
            Long userId,
            String userTextInput) {
        chatGptHistoryService.createHistoryIfNotExist(userId);

        MessageDTO userMessageDTO = new MessageDTO();
        userMessageDTO.setContent(userTextInput);
        userMessageDTO.setRole("user");

        var historyDTO = chatGptHistoryService.addMessageToHistory(
                userId,
                userMessageDTO
        );

        var request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(historyDTO.getMessages())
                .build();

        var response = openAiClient.createChatCompletion(request);

        var messageFromGpt = response.choices().get(0).message();
        

        chatGptHistoryService.addMessageToHistory(userId, messageFromGpt);

        return messageFromGpt.getContent();
    }
}
