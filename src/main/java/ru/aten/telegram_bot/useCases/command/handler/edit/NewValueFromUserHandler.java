package ru.aten.telegram_bot.useCases.command.handler.edit;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.entities.FieldDisplayName;
import ru.aten.telegram_bot.entities.User;
import ru.aten.telegram_bot.entities.UserInfo;
import ru.aten.telegram_bot.useCases.UserService;
import ru.aten.telegram_bot.useCases.enums.EditType;

@Slf4j
@Component
@AllArgsConstructor
public class NewValueFromUserHandler {

    private final UserService userService;

    public BotApiMethod<?> handleNewValue(
            CallbackQuery callbackQuery,
            EditType editType,
            Long requestFrom,
            Long telegramId,
            String fieldName
    ) throws NoSuchFieldException, SecurityException {
        Optional<User> userOptional = userService.getUserByTelegramId(telegramId);
        if (userOptional.isEmpty()) {
            return EditMessageText.builder()
            .chatId(callbackQuery.getMessage().getChatId().toString())
            .messageId(callbackQuery.getMessage().getMessageId())
            .text("Пользователь не найден")
            .build();
        } else {
            User user = userOptional.get();
            UserInfo userInfo = user.getUserInfo();
            Field field;

            switch (editType) {
                case FIELD -> field = user.getClass().getDeclaredField(fieldName);
                case INFO -> field = userInfo.getClass().getDeclaredField(fieldName);
                default -> throw new NoSuchFieldException("Неизвестное поле");
            }

            fieldName = field.getAnnotation(FieldDisplayName.class).value();

            EditUserContext context = new EditUserContext(true, callbackQuery, editType, requestFrom, telegramId, field);
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
