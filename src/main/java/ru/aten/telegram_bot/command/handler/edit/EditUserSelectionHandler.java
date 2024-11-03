package ru.aten.telegram_bot.command.handler.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.service.UserService;

@Component
@AllArgsConstructor
public class EditUserSelectionHandler {

    private final UserService userService;

    public BotApiMethod<?> handleUserSelection(Message message) {

        InlineKeyboardMarkup keyboardMarkup = createUserSelectionKeyboard();
        return SendMessage.builder()
            .chatId(message.getChatId().toString())
            .text("Выберете пользователя для редактирования: ")
            .replyMarkup(keyboardMarkup)
            .build();
    }

    private InlineKeyboardMarkup createUserSelectionKeyboard() {
        List<User> users = userService.getAllUsers();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (User user : users) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(user.getFirstName() + " " + user.getLastName())
                .callbackData("edit_user:%s:%s".formatted(EditType.FIELD.getTypeValue(), user.getTelegramId()))
                .build();
            keyboard.add(Collections.singletonList(button));
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }
}
