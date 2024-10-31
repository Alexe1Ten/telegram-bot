package ru.aten.telegram_bot.command.handler.edit;

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
import ru.aten.telegram_bot.model.annotations.AdminOnly;
import ru.aten.telegram_bot.model.annotations.Displayable;
import ru.aten.telegram_bot.model.annotations.FieldDisplayName;
import ru.aten.telegram_bot.model.annotations.Modifiable;
import ru.aten.telegram_bot.service.UserService;

@Slf4j
@Component
@AllArgsConstructor
public class EditUserFieldHandler {

    private final UserService userService;

    public BotApiMethod<?> handleEditUser(CallbackQuery callbackQuery, Long requestFrom, Long telegramId) {
        Optional<User> userOptional = userService.getUserByTelegramId(telegramId);
        if (userOptional.isEmpty()) {
            return EditMessageText.builder()
                    .chatId(callbackQuery.getMessage().getChatId().toString())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .text("Пользователь не найден.")
                    .build();
        }

        InlineKeyboardMarkup keyboardMarkup = createUserFieldKeyboard(userOptional.get(), callbackQuery.getFrom().getId(), telegramId);
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

    private InlineKeyboardMarkup createUserFieldKeyboard(User user, Long requestFrom, Long telegramId) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        Field[] fields = User.class.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            if (!shouldDisplayField(field)) {
                continue;
            }

            try {
                Object value = field.get(user);
                if (value == null) {
                    value = "нет значения";
                }

                InlineKeyboardButton button = createButtonForField(field, value, requestFrom, telegramId);
                keyboard.add(Collections.singletonList(button));
            } catch (IllegalAccessException e) {
                log.error("Ошибка доступа к полю пользователя: " + field.getName(), e);
            }
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    private boolean shouldDisplayField(Field field) {
        Displayable displayableAnnotation = field.getAnnotation(Displayable.class);
        return displayableAnnotation != null && displayableAnnotation.value();
    }

    private InlineKeyboardButton createButtonForField(Field field, Object value, Long requestFrom, Long telegramId) {
        Optional<User> userOptional = userService.getUserByTelegramId(telegramId);

        User user = userOptional.get();
        boolean isAdmin = userService.isAdmin(user);

        String fieldName = field.getAnnotation(FieldDisplayName.class).value();
        AdminOnly adminOnlyAnnotation = field.getAnnotation(AdminOnly.class);
        Modifiable modifiableAnnotation = field.getAnnotation(Modifiable.class);

        String buttonText = fieldName + ": " + value;
        String callbackData;

        if (adminOnlyAnnotation
                != null && adminOnlyAnnotation.value()
                && !isAdmin) {
            buttonText += " (Только для администратора)";
            callbackData = "edit_user_field:" + requestFrom + ":" + telegramId + ":" + field.getName();
        } else if (modifiableAnnotation
                == null || !modifiableAnnotation.value()) {
            buttonText += " (Не доступно для изменения)";
            callbackData = "disabled:" + field.getName();
        } else {
            buttonText = "Изменить " + buttonText;
            callbackData = "edit_user_field:" + requestFrom + ":" + telegramId + ":" + field.getName();
        }

        return InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(callbackData)
                .build();
    }

}
