package ru.aten.telegram_bot.command.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import ru.aten.telegram_bot.command.TelegramCommandHandler;
import ru.aten.telegram_bot.command.TelegramCommands;

@Component
public class StartCommandHandler implements TelegramCommandHandler {

    private final String HELLO_MESSAGE = """
            Привет %s,
            Этим ботом ты можешь пользоваться для общения с GPT
            Каждое сообщение запоминается для контекста
            Очистить контекст можно с помощью команды /clear
            """;

    @Override
    public BotApiMethod<?> processCommand(Update update) {
        // Получаем пользователя, который отправил сообщение
        var user = update.getMessage().getFrom();

        // Используем firstName пользователя (если доступно) или альтернативный текст
        String userName = (user != null) ? user.getFirstName() : "пользователь";

        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(HELLO_MESSAGE.formatted(userName))
                .build();
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.START_COMMAND;
    }

}
