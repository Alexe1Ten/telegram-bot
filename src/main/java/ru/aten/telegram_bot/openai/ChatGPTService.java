package ru.aten.telegram_bot.openai;

import org.springframework.stereotype.Service;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.openai.api.ChatCompletionRequest;
import ru.aten.telegram_bot.openai.api.ChatGptHistoryService;
import ru.aten.telegram_bot.openai.api.Message;
import ru.aten.telegram_bot.openai.api.OpenAiClient;

@Service
@AllArgsConstructor
public class ChatGPTService {

    private final OpenAiClient openAiClient;
    private final ChatGptHistoryService chatGptHistory;

    @Nonnull
    public String getResponseChatForUser(
            Long userId,
            String userTextInput
    ) {
        chatGptHistory.createHistoryIfNotExist(userId);

        var history = chatGptHistory.addMessageToHistory(
                userId,
                Message.builder()
                        .content(userTextInput)
                        .role("user")
                        .build()
        );

        var request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(history.chatMessages())
                .build();

        var response = openAiClient.createChatCompletion(request);

        var messageFromGpt = response.choices().get(0).message();

        chatGptHistory.addMessageToHistory(userId, messageFromGpt);

        return messageFromGpt.content();
    }
}
