package ru.aten.telegram_bot.command.handler.edit;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.model.UserInfo;
import ru.aten.telegram_bot.model.annotations.Displayable;
import ru.aten.telegram_bot.model.annotations.FieldDisplayName;
import ru.aten.telegram_bot.model.enums.EditType;
import ru.aten.telegram_bot.service.UserService;

@Slf4j
@Component
@AllArgsConstructor
public class EditUserHandler {

    private final UserService userService;

    public BotApiMethod<?> handleEditUser(CallbackQuery callbackQuery, EditType type, Long telegramId) throws IOException {
        Optional<User> userOptional = userService.getUserByTelegramId(telegramId);
        if (userOptional.isEmpty()) {
            return EditMessageText.builder()
                    .chatId(callbackQuery.getMessage().getChatId().toString())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .text("Пользователь не найден.")
                    .build();
        }

        User user = userOptional.get();
        InlineKeyboardMarkup keyboardMarkup;

        switch (type) {
            case FIELD -> keyboardMarkup = createFieldKeyboard(user, type, callbackQuery.getFrom().getId(), telegramId);
            case INFO -> {
                UserInfo userInfo = user.getUserInfo();
                keyboardMarkup = createFieldKeyboard(userInfo, type, callbackQuery.getFrom().getId(), telegramId);
            }
            default -> throw new IOException("Неизвестный тип");
        }


        Map<Long, EditUserContext> contextMap = new HashMap<>();
        userService.setEditUserContext(contextMap);
        return keyboardMarkup.getKeyboard().isEmpty()
                ? EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text("Нет доступных полей для редактирования.")
                        .build()
                : EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text("Выберите поле для редактирования:")
                        .replyMarkup(keyboardMarkup)
                        .build();
    }

    private InlineKeyboardMarkup createFieldKeyboard(Object object, EditType editType, Long requestFrom, Long telegramId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            if (!shouldDisplayField(field)) {
                continue;
            }

            try {
                Object value = field.get(object);
                if (value == null) {
                    value = "нет значения";
                }

                InlineKeyboardButton button = createButtonForField(field, editType, value, requestFrom, telegramId);
                keyboard.add(Collections.singletonList(button));
            } catch (IllegalAccessException e) {
                log.error("Ошибка доступа к полю пользователя: " + field.getName(), e);
            }
        }

        addNavigationButtons(keyboard, editType, telegramId);

        addCancelButton(keyboard);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    private boolean shouldDisplayField(Field field) {
        Displayable displayableAnnotation = field.getAnnotation(Displayable.class);
        return displayableAnnotation != null && displayableAnnotation.value();
    }

    private InlineKeyboardButton createButtonForField(Field field, EditType type, Object value, Long requestFrom, Long telegramId) {

        String fieldName = field.getAnnotation(FieldDisplayName.class).value();
        String buttonText = fieldName + ": " + value;
        String callbackData;
        callbackData = "edit_user_field:%s:%s:%s:%s".formatted(type.getTypeValue(), requestFrom, telegramId, field.getName());

        return InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(callbackData)
                .build();
    }

    private InlineKeyboardButton createNavigationButton(String text, EditType targetType, Long telegramId) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData("edit_user:%s:%s".formatted(targetType.getTypeValue(), telegramId))
                .build();
    }

    private void addNavigationButtons(List<List<InlineKeyboardButton>> keyboard, EditType editType, Long telegramId) {
        InlineKeyboardButton navigationButton;
        switch (editType) {
            case FIELD -> navigationButton = createNavigationButton("Дополнительно", EditType.INFO, telegramId);
            case INFO -> navigationButton = createNavigationButton("Назад", EditType.FIELD, telegramId);
            default -> {
                return;
            }
        }
        keyboard.add(Collections.singletonList(navigationButton));
    }
    
    public static void addCancelButton(List<List<InlineKeyboardButton>> keyboard) {
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder()
                .text("Отмена")
                .callbackData(String.format("cancel_edit:"))
                .build();
        keyboard.add(Collections.singletonList(cancelButton));
    }

}
