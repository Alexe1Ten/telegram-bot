package ru.aten.telegram_bot.frameworksAndDrivers.telegram;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.useCases.command.handler.ExportHandler;
import ru.aten.telegram_bot.useCases.command.handler.edit.EditUserCommandHandler;
import ru.aten.telegram_bot.useCases.telegram.TelegramUpdateMessageHandler;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramUpdateMessageHandler telegramUpdateMessageHandler;
    private final EditUserCommandHandler editUserCommandHandler;
    private final ExportHandler exportHandler;

    public TelegramBot(
            @Value(value = "${bot.token}") String botToken,
            TelegramUpdateMessageHandler telegramUpdateMessageHandler,
            EditUserCommandHandler editUserCommandHandler,
            ExportHandler exportHandler
    ) {
        super(new DefaultBotOptions(), botToken);
        this.telegramUpdateMessageHandler = telegramUpdateMessageHandler;
        this.editUserCommandHandler = editUserCommandHandler;
        this.exportHandler = exportHandler;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        try {
            if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                String callbackData = callbackQuery.getData();
                BotApiMethod<?> response;

                switch (callbackData.split(":")[0]) {
                    case "edit_user":
                        response = editUserCommandHandler.handleCallbackQuery(callbackQuery);
                        break;
                    case "cancel":
                        response = editUserCommandHandler.handleCallbackQuery(callbackQuery);
                        break;
                    case "export":
                        response = exportHandler.handleCallbackQuery(callbackQuery);
                        break;
                    default:
                        throw new AssertionError();
                }


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

    @Override
    public CompletableFuture<Message> executeAsync(SendDocument sendDocument) {
        // TODO Auto-generated method stub
        return super.executeAsync(sendDocument);
    }
}
