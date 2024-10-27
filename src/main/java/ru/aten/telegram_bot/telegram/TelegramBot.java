package ru.aten.telegram_bot.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.telegram.message.TelegramUpdateMessageHandler;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramUpdateMessageHandler telegramUpdateMessageHandler;

    public TelegramBot(
            @Value(value = "${bot.token}") String botToken,
            TelegramUpdateMessageHandler telegramUpdateMessageHandler
    ) {
        super(new DefaultBotOptions(), botToken);
        this.telegramUpdateMessageHandler = telegramUpdateMessageHandler;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        try {
            var method = processUpdate(update);
            if (method != null) {
                sendApiMethod(method);
            }
        } catch (Exception e) {
            log.error("Error while processing update", e);
            Long chatId = update.getMessage().getChatId();
            Integer messageThreadId = update.getMessage().getMessageThreadId();
            sendUserErrorMessage(chatId, messageThreadId);
        }

    }

    private BotApiMethod<?> processUpdate(Update update) {
        return update.hasMessage()
                ? telegramUpdateMessageHandler.handleMessage(update.getMessage())
                : null;
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
