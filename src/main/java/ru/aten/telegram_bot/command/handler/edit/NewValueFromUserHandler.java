package ru.aten.telegram_bot.command.handler.edit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.service.UserService;

@Slf4j
@Component
@AllArgsConstructor
public class NewValueFromUserHandler {

    private final UserService userService;

    public BotApiMethod<?> handleNewValue(
            CallbackQuery callbackQuery,
            Long requestFrom,
            Long telegramId,
            String fieldName
    ) {
        Optional<User> userOptional = userService.getUserByTelegramId(telegramId);
        if (userOptional.isEmpty()) {
            return EditMessageText.builder()
                    .chatId(callbackQuery.getMessage().getChatId().toString())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .text("Пользователь не найден")
                    .build();
        } else {
            EditUserContext context = new EditUserContext(true, callbackQuery, requestFrom, telegramId, fieldName);
            Map<Long, EditUserContext> contextMap = new HashMap<>();
            contextMap.put(requestFrom, context);
            userService.setEditUserContext(contextMap);
            return EditMessageText.builder()
                    .chatId(callbackQuery.getMessage().getChatId().toString())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .text("Введите новое значение для поля: " + fieldName)
                    .build();
        }
    }
}
