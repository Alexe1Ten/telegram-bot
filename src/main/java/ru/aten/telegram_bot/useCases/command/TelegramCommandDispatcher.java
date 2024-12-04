package ru.aten.telegram_bot.useCases.command;

import java.util.List;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TelegramCommandDispatcher {

    private final List<TelegramCommandHandler> telegramCommandHandlerList;

    public BotApiMethod<?> processCommand(Message message) {
        if (!isCommand(message)) throw new IllegalArgumentException("Not a command passed");
        var text = message.getText();
        
        var suitedHandler = telegramCommandHandlerList.stream()
                .filter(it -> it.getSupportedCommands().getCommandValue().equals(text))
                .findAny();
        if (suitedHandler.isEmpty()) {
            return SendMessage.builder()
                .chatId(message.getChatId())
                .text("Not supported command: %s".formatted(text))
                .build();
        }
        return suitedHandler.orElseThrow().processCommand(message);
    }

    public boolean isCommand(Message message) {
        return message.hasText()
            && message.getText().startsWith("/");
    }
}

