package ru.aten.telegram_bot.command.handler.edit;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.command.TelegramCommandHandler;
import ru.aten.telegram_bot.command.TelegramCommands;
import ru.aten.telegram_bot.exceptions.UserNotFoundException;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.model.UserInfo;
import ru.aten.telegram_bot.model.annotations.FieldDisplayName;
import ru.aten.telegram_bot.service.UserService;

@Slf4j
@Component
@AllArgsConstructor
public class EditUserCommandHandler implements TelegramCommandHandler {

    private final UserService userService;
    private final EditUserSelectionHandler selectionHandler;
    private final EditUserHandler userHandler;
    private final NewValueFromUserHandler newValueHandler;
    private final CancelHandler cancelHandler;

    @Override
    public BotApiMethod<?> processCommand(Message message) {
        if (!userService.isAdmin(message.getFrom().getId())) {
            return SendMessage.builder()
                    .chatId(message.getChatId().toString())
                    .text("Эта команда вам недоступна.")
                    .build();
        } else {
            return selectionHandler.handleUserSelection(message);
        }
    }

    public BotApiMethod<?> handleCallbackQuery(CallbackQuery callbackQuery) throws IOException, NoSuchFieldException, SecurityException {
        String data = callbackQuery.getData();
        String[] parts = data.split(":");
        if (data.startsWith("edit_user:")) {
            EditType type = EditType.getType(parts[1]);
            Long telegramId = Long.valueOf(parts[2]);

            return userHandler.handleEditUser(callbackQuery, type, telegramId);
        } else if (data.startsWith("edit_user_field:")) {
            EditType type = EditType.getType(parts[1]);
            Long requestFrom = Long.valueOf(parts[2]);
            Long telegramId = Long.valueOf(parts[3]);
            String fieldName = parts[4];
            return newValueHandler.handleNewValue(callbackQuery, type, requestFrom, telegramId, fieldName);
        } else if (data.startsWith("cancel_edit:")) {
            return cancelHandler.cancelOperation(callbackQuery);
        } else {
            return null;
        }
    }

    public BotApiMethod<?> requestNewValue(CallbackQuery callbackQuery, EditType editType, Long telegramId, Field field, String newValue) throws IOException {
        Optional<User> userOptional = userService.getUserByTelegramId(telegramId);
        if (userOptional.isEmpty()) {
            return EditMessageText.builder()
                    .chatId(callbackQuery.getMessage().getChatId().toString())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .text("Пользователь не найден")
                    .build();
        } else {
            try {
                User user = userOptional.get();
                UserInfo userInfo = user.getUserInfo();
                Object targObject;

                switch (editType) {
                    case FIELD -> {
                        targObject = user;
                    }
                    case INFO -> {
                        targObject = userInfo;
                    }
                    default -> {
                        return EditMessageText.builder()
                                .chatId(callbackQuery.getMessage().getChatId().toString())
                                .messageId(callbackQuery.getMessage().getMessageId())
                                .text("Неизвестный тип: " + editType.toString())
                                .build();
                    }
                }
                field.setAccessible(true);

                Object convertedValue = FieldValueConverter.convertToFieldType(field, newValue);
                field.set(targObject, convertedValue);

                user.setUserInfo(userInfo);
                userService.updateUser(user);
                userService.removeContext(callbackQuery.getFrom().getId());

                InlineKeyboardMarkup keyboardMarkup = createContinueFinishKeyboard(editType, telegramId);

                return EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(field.getAnnotation(FieldDisplayName.class).value() + " успешно изменено на " + convertedValue.toString())
                        .replyMarkup(keyboardMarkup)
                        .build();
            } catch (IllegalAccessException | IllegalArgumentException | SecurityException | UserNotFoundException e) {
                return EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text("Ошибка при обновлении поля " + field.getName())
                        .build();
            }
        }
    }

    private InlineKeyboardMarkup createContinueFinishKeyboard(EditType editType, Long telegramId) {
        InlineKeyboardButton continueButton = InlineKeyboardButton.builder()
                .text("Продолжить изменения")
                .callbackData("edit_user:%s:%s".formatted(editType.getTypeValue(), telegramId))
                .build();

        InlineKeyboardButton finishButton = InlineKeyboardButton.builder()
                .text("Завершить")
                .callbackData("cancel_edit:")
                .build();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(Arrays.asList(
                Collections.singletonList(continueButton),
                Collections.singletonList(finishButton)
        ));

        return keyboardMarkup;
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.EDIT_USER_COMMAND;
    }
}
