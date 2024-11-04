package ru.aten.telegram_bot.command.handler.edit;

import java.lang.reflect.Field;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.aten.telegram_bot.model.enums.EditType;

@AllArgsConstructor
@Data
public class EditUserContext {

    private boolean isWaiting = false;
    private CallbackQuery callbackQuery;
    private EditType editType;
    private Long requestFrom;
    private Long telegramId;
    private Field field;

}
