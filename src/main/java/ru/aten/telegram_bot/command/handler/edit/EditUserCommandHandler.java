package ru.aten.telegram_bot.command.handler.edit;

import java.lang.reflect.Field;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.command.TelegramCommandHandler;
import ru.aten.telegram_bot.command.TelegramCommands;
import ru.aten.telegram_bot.exceptions.UserNotFoundException;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.model.annotations.FieldDisplayName;
import ru.aten.telegram_bot.service.UserService;

@Slf4j
@Component
@AllArgsConstructor
public class EditUserCommandHandler implements TelegramCommandHandler {

    private final UserService userService;
    private final EditUserSelectionHandler selectionHandler;
    private final EditUserFieldHandler fieldHandler;
    private final NewValueFromUserHandler newValueHandler;

    @Override
    public BotApiMethod<?> processCommand(Message message) {
        if (!message.getChat().isUserChat() && !userService.existsByTelegramId(message.getFrom().getId())) {
            return SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text("Эта команда вам недоступна.")
                .build();
        } else {
            return selectionHandler.handleUserSelection(message);
        }
    }

    public BotApiMethod<?> handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (data.startsWith("edit_user:")) {
            Long requestFrom = callbackQuery.getFrom().getId();
            Long telegramId = Long.valueOf(data.split(":")[1]);
            
            return fieldHandler.handleEditUser(callbackQuery, requestFrom, telegramId);
        } else if (data.startsWith("edit_user_field:")) {
            Long requestFrom = Long.valueOf(data.split(":")[1]);
            Long telegramId = Long.valueOf(data.split(":")[2]);
            String fieldName = data.split(":")[3];
            return newValueHandler.handleNewValue(callbackQuery, requestFrom, telegramId, fieldName);
        } else {
            return null;
        }
    }

    public BotApiMethod<?> requestNewValue(CallbackQuery callbackQuery, Long telegramId, String fieldName, String newValue) {
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
                Field field = user.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                Object convertedValue = FieldValueConverter.convertToFieldType(field, newValue);
                field.set(user, convertedValue);

                userService.updateUser(user);
                userService.removeContext(callbackQuery.getFrom().getId());

                return EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(field.getAnnotation(FieldDisplayName.class).value() + " успешно изменено на " + convertedValue.toString())
                        .build();
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | UserNotFoundException e) {
                return EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text("Ошибка при обновлении поля " + fieldName)
                        .build();
            }
        }
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.EDIT_USER_COMMAND;
    }
}
