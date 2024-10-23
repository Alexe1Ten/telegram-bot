package ru.aten.telegram_bot.command;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramCommandHandler {

    BotApiMethod<?> processCommand(Update update);

    TelegramCommands getSupportedCommands();

}
