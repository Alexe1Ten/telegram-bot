package ru.aten.telegram_bot.telegram;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.command.handler.edit.EditUserCommandHandler;
import ru.aten.telegram_bot.telegram.message.TelegramUpdateMessageHandler;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramUpdateMessageHandler telegramUpdateMessageHandler;
    private final EditUserCommandHandler editUserCommandHandler;

    public TelegramBot(
            @Value(value = "${bot.token}") String botToken,
            TelegramUpdateMessageHandler telegramUpdateMessageHandler,
            EditUserCommandHandler editUserCommandHandler
    ) {
        super(new DefaultBotOptions(), botToken);
        this.telegramUpdateMessageHandler = telegramUpdateMessageHandler;
        this.editUserCommandHandler = editUserCommandHandler;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        try {
            if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                BotApiMethod<?> response = editUserCommandHandler.handleCallbackQuery(callbackQuery);
                if (response != null) {
                    try {
                        execute(response);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                var method = processUpdate(update);
                if (method != null) {
                    sendApiMethod(method);
                }

            }

        } catch (Exception e) {
            log.error("Error while processing update", e);
            Long chatId = update.getMessage().getChatId();
            Integer messageThreadId = update.getMessage().getMessageThreadId();
            sendUserErrorMessage(chatId, messageThreadId);
        }

    }

    private BotApiMethod<?> processUpdate(Update update) {
        try {
            return update.hasMessage()
                    ? telegramUpdateMessageHandler.handleMessage(update.getMessage())
                    : null;
        } catch (TelegramApiException | IOException e) {
            log.error("Error while processing update", e);
            Long chatId = update.getMessage().getChatId();
            Integer messageThreadId = update.getMessage().getMessageThreadId();
            sendUserErrorMessage(chatId, messageThreadId);
            return null;
        }
    }

    @SneakyThrows
    private void sendUserErrorMessage(Long userId, Integer messageThreadId) {
        SendMessage.SendMessageBuilder sendMessageBuilder = SendMessage.builder()
                .chatId(userId)
                .text("Произошла ошибка, попробуйте позже");

        // Проверка на messageThreadId
        if (messageThreadId != null) {
            sendMessageBuilder.messageThreadId(messageThreadId);
        }

        sendApiMethod(sendMessageBuilder.build());
    }

    @Override
    public String getBotUsername() {
        return "DondonnyBot";
    }

}
