package ru.aten.telegram_bot.command.handler;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class EditUserContext {
    private boolean isWaiting = false;
    private CallbackQuery callbackQuery;
    private Long requestFrom;
    private Long telegramId;
    private String fieldName;

    public boolean isWaiting() {
        return isWaiting;
    }
    public Long getTelegramId() {
        return telegramId;
    }
    public void setIsWaiting(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }
    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public Long getRequestFrom() {
        return requestFrom;
    }

    public void setRequestFrom(Long requestFrom) {
        this.requestFrom = requestFrom;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public CallbackQuery getCallbackQuery() {
        return callbackQuery;
    }

    public void setCallbackQuery(CallbackQuery callbackQuery) {
        this.callbackQuery = callbackQuery;
    }

}
