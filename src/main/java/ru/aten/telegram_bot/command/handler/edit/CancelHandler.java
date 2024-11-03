package ru.aten.telegram_bot.command.handler.edit;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class CancelHandler {

    public BotApiMethod<?> cancelOperation(CallbackQuery callbackQuery) {
        return EditMessageText.builder()
                .text("Изменения внесены")
                .chatId(callbackQuery.getMessage().getChatId().toString())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build();
    }
}
