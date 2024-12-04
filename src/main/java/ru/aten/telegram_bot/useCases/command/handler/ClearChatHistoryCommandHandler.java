package ru.aten.telegram_bot.useCases.command.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.frameworksAndDrivers.openai.api.ChatGptHistoryService;
import ru.aten.telegram_bot.useCases.command.TelegramCommandHandler;
import ru.aten.telegram_bot.useCases.enums.TelegramCommands;

@Component
@AllArgsConstructor
public class ClearChatHistoryCommandHandler implements TelegramCommandHandler{

    private final ChatGptHistoryService chatGptHistory;
    private final String CLEAR_MESSAGE = """
            История запросов для пользователя %s, успешно удалена
            """;

    @Override
    public BotApiMethod<?> processCommand(Message message) {
        var user = message.getFrom();

        // Используем firstName пользователя (если доступно) или альтернативный текст
        String userName = (user != null) ? user.getFirstName() : "пользователь";

        chatGptHistory.clearHistory(message.getChatId());
        return SendMessage.builder()
            .chatId(message.getChatId())
            .text(CLEAR_MESSAGE.formatted(userName))
            .build();
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.CLEAR_COMMAND;
    }

}
