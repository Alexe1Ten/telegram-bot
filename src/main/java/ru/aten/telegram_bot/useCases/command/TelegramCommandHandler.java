package ru.aten.telegram_bot.useCases.command;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import ru.aten.telegram_bot.useCases.enums.TelegramCommands;

public interface TelegramCommandHandler {

    BotApiMethod<?> processCommand(Message message);

    TelegramCommands getSupportedCommands();

}
