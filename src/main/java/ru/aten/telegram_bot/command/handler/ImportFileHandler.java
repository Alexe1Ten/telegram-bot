package ru.aten.telegram_bot.command.handler;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import ru.aten.telegram_bot.command.TelegramCommandHandler;
import ru.aten.telegram_bot.command.TelegramCommands;

public class ImportFileHandler implements TelegramCommandHandler{

    @Override
    public BotApiMethod<?> processCommand(Message message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processCommand'");
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.IMPORT_FILE;
    }

}
