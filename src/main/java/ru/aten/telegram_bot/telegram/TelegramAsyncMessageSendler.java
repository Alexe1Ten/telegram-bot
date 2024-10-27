package ru.aten.telegram_bot.telegram;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelegramAsyncMessageSendler {

    private final DefaultAbsSender defaultAbsSender;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public TelegramAsyncMessageSendler(
        @Lazy DefaultAbsSender defaultAbsSender
        
    ) {
        this.defaultAbsSender = defaultAbsSender;
    }

    @SneakyThrows
    public void sendMessageAsync(
            String chatId,
            Supplier<SendMessage> action,
            Function<Throwable, SendMessage> onErrorHandler,
            Integer messageThreadId
    ) {
        var message = defaultAbsSender.execute(SendMessage.builder()
                .text("Ваш запрос принят в обработку, ожидайте")
                .chatId(chatId)
                .messageThreadId(messageThreadId)
                .build());

        CompletableFuture.supplyAsync(action, executorService)
                .exceptionally(onErrorHandler)
                .thenAccept(sendMessage -> {
                    try {
                        defaultAbsSender.execute(EditMessageText.builder()
                                    .chatId(chatId)
                                    .messageId(message.getMessageId())
                                    .text(sendMessage.getText())
                            .build());
                    } catch (TelegramApiException ex) {
                        log.error("Error while send request to telegram", ex);
                        throw new RuntimeException(ex);
                    }
                });

    }
}
