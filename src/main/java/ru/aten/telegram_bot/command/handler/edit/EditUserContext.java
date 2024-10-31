package ru.aten.telegram_bot.command.handler.edit;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class EditUserContext {

    private boolean isWaiting = false;
    private CallbackQuery callbackQuery;
    private Long requestFrom;
    private Long telegramId;
    private String fieldName;

}
