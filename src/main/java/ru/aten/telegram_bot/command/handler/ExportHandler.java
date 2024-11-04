package ru.aten.telegram_bot.command.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.command.TelegramCommandHandler;
import ru.aten.telegram_bot.model.enums.TelegramCommands;
import ru.aten.telegram_bot.service.UserService;

@Slf4j
@Component
@AllArgsConstructor
public class ExportHandler implements TelegramCommandHandler{

    private final UserService userService;

    @Override
    public BotApiMethod<?> processCommand(Message message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processCommand'");
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSupportedCommands'");
    }

}
