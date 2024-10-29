package ru.aten.telegram_bot.command.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ru.aten.telegram_bot.service.UserService;

@Slf4j
@Component
@AllArgsConstructor
public class EditUserCommandHandler implements TelegramCommandHandler {

    private final UserService userService;
    private Map<Long, EditUserContext> editUserContexts = new HashMap<>();

    @Override
    public BotApiMethod<?> processCommand(Message message) {

        if (!message.getChat().isUserChat() && !userService.existsByTelegramId(message.getFrom().getId())) {
            return SendMessage.builder()
                    .chatId(message.getChatId().toString())
                    .text("Эта команда вам недоступна.")
                    .build();
        }
        List<User> users = userService.getAllUsers();

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (User user : users) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(user.getFirstName() + " " + user.getLastName())
                    .callbackData("edit_user:" + user.getTelegramId())
                    .build();

            keyboard.add(Collections.singletonList(button));
        }

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(message.getChatId().toString())
                .text("Выберите пользователя для редактирования:")
                .replyMarkup(keyboardMarkup)
                .build();

        return sendMessage;
    }

    public BotApiMethod<?> handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();

        if (data.startsWith("edit_user:")) {
            long telegramId = Long.parseLong(data.split(":")[1]);
            return editUser(callbackQuery, telegramId);
        }
        if (data.startsWith("edit_user_field:")) {
            Long requestFrom = callbackQuery.getFrom().getId();
            long telegramId = Long.parseLong(data.split(":")[2]);
            String fieldName = data.split(":")[3];

            editUserContexts.put(requestFrom, new EditUserContext(true, callbackQuery, requestFrom, telegramId, fieldName));

            return editUserFields(callbackQuery, telegramId, fieldName);
        }

        return null;
    }

    private BotApiMethod<?> editUser(CallbackQuery callbackQuery, Long telegramId) {
        Optional<User> userOptional = userService.findByTelegramId(telegramId);
        Long requestFrom = callbackQuery.getFrom().getId();
    
        if (userOptional.isPresent()) {
            User user = userOptional.get();
    
            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    
            Field[] fields = User.class.getDeclaredFields();
    
            for (Field field : fields) {
                String fieldName = field.getName();
                
                
                field.setAccessible(true);
                try {
                    Object value = field.get(user);
    
                    if (value == null) {
                        value = "Нет значения";
                    }
    
                    InlineKeyboardButton button = InlineKeyboardButton.builder()
                            .text("Изменить " + fieldName + ": " + value)
                            .callbackData("edit_user_field:" + requestFrom + ":" + telegramId + ":" + fieldName)
                            .build();
    
                    keyboard.add(Collections.singletonList(button));
    
                } catch (IllegalAccessException e) {
                    log.error("Ошибка доступа к полю пользователя: " + field.getName(), e);
                }
            }
    
            if (keyboard.isEmpty()) {
                return new SendMessage(callbackQuery.getMessage().getChatId().toString(), "Нет доступных полей для редактирования.");
            }
    
            keyboardMarkup.setKeyboard(keyboard);
    
            return EditMessageText.builder()
                    .chatId(callbackQuery.getMessage().getChatId().toString())
                    .messageId(callbackQuery.getMessage().getMessageId())
                    .text("Выберите поле для редактирования:")
                    .replyMarkup(keyboardMarkup)
                    .build();
        } else {
            return new SendMessage(callbackQuery.getMessage().getChatId().toString(), "Пользователь не найден.");
        }
    }

    private BotApiMethod<?> editUserFields(CallbackQuery callbackQuery, Long telegramId, String fieldName) {


        return EditMessageText.builder()
                .chatId(callbackQuery.getMessage().getChatId().toString())
                .messageId(callbackQuery.getMessage().getMessageId())
                .text("Введите новое значение для поля " + fieldName + ":")
                .build();
    }

    public BotApiMethod<?> requestNewValue(CallbackQuery callbackQuery, Long telegramId, String fieldName, String newValue) {
        Optional<User> userOptional = userService.findByTelegramId(telegramId);
    
        if (userOptional.isPresent()) {
            User user = userOptional.get();
    
            try {
                Field field = User.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                
                // Преобразование значения в нужный тип
                Object typedValue = convertToFieldType(field, newValue);
                field.set(user, typedValue);
                
                userService.updateUser(user);
    
                return EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text(fieldName + " успешно заменено на: " + newValue)
                        .build();
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | UserNotFoundException e) {
                log.error("Error while changing field: " + fieldName, e);
                return EditMessageText.builder()
                        .chatId(callbackQuery.getMessage().getChatId().toString())
                        .messageId(callbackQuery.getMessage().getMessageId())
                        .text("Ошибка при изменении поля: " + fieldName)
                        .build();
            }
        } else {
            return new SendMessage(callbackQuery.getMessage().getChatId().toString(), "Пользователь не найден.");
        }
    }

    private Object convertToFieldType(Field field, String newValue) {
        if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
            return Integer.valueOf(newValue);
        } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
            return Long.valueOf(newValue);
        } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
            return Double.valueOf(newValue);
        } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
            return Boolean.valueOf(newValue);
        } else {
            return newValue; // Если тип - строка или другой объект
        }
    }

    @Override
    public TelegramCommands getSupportedCommands() {
        return TelegramCommands.EDIT_USER_COMMAND;
    }

    public Map<Long, EditUserContext> getEditUserContexts() {
        return editUserContexts;
    }

    public void setEditUserContexts(Map<Long, EditUserContext> editUserContexts) {
        this.editUserContexts = editUserContexts;
    }

}
