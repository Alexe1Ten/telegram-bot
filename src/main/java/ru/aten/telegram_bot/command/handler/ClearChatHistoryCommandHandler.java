package ru.aten.telegram_bot.command.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.command.TelegramCommandHandler;
import ru.aten.telegram_bot.command.TelegramCommands;
import ru.aten.telegram_bot.openai.api.ChatGptHistoryService;

@Component
@AllArgsConstructor
public class ClearChatHistoryCommandHandler implements TelegramCommandHandler{

    private final ChatGptHistoryService chatGptHistory;
    private final String CLEAR_MESSAGE = """
            История запросов для пользователя %s, успешно удалена
            """;

    @Override
    public BotApiMethod<?> processCommand(Update update) {
        var user = update.getMessage().getFrom();

        // Используем firstName пользователя (если доступно) или альтернативный текст
        String userName = (user != null) ? user.getFirstName() : "пользователь";

        chatGptHistory.clearHistory(update.getMessage().getChatId());
        return SendMessage.builder()
            .chatId(update.getMessage().getChatId())
            .text(CLEAR_MESSAGE.formatted(userName))
            .build();
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.CLEAR_COMMAND;
    }

}
