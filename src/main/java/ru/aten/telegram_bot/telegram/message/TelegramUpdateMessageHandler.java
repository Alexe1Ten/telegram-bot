package ru.aten.telegram_bot.telegram.message;

import java.net.MalformedURLException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.command.TelegramCommandDispatcher;
import ru.aten.telegram_bot.command.handler.EditUserCommandHandler;
import ru.aten.telegram_bot.command.handler.EditUserContext;
import ru.aten.telegram_bot.command.handler.NewMemberHandler;
import ru.aten.telegram_bot.telegram.TelegramAsyncMessageSendler;

@Slf4j
@Service
@AllArgsConstructor
public class TelegramUpdateMessageHandler {

    private final TelegramCommandDispatcher telegramCommandDispatcher;
    private final TelegramAsyncMessageSendler telegramAsyncMessageSendler;
    private final TelegramTextHandler telegramTextHandler;
    private final TelegramVoiceHandler telegramVoiceHandler;
    private final NewMemberHandler newMemberHandler;
    private final EditUserCommandHandler editUserCommandHandler;

    
    public BotApiMethod<?> handleMessage(Message message) {
        Map<Long, EditUserContext> editUserContexts = editUserCommandHandler.getEditUserContexts();
        Long userId = message.getFrom().getId();

        if (telegramCommandDispatcher.isCommand(message)) {
            return telegramCommandDispatcher.processCommand(message);
        }

        if (editUserContexts.containsKey(userId) && editUserContexts.get(userId).isWaiting()) {
            EditUserContext context = editUserContexts.get(userId);
            String newValue = message.getText();
            System.out.println(context);

            editUserContexts.remove(userId);
            editUserCommandHandler.setEditUserContexts(editUserContexts);

            return editUserCommandHandler.requestNewValue(context.getCallbackQuery(), context.getTelegramId(), context.getFieldName(), newValue);
        }

        var chatId = message.getChatId().toString();

        // Проверка, содержит ли обновление сообщение с новыми участниками
        if (!message.getNewChatMembers().isEmpty()) {

            var userName = message.getNewChatMembers().getFirst().getFirstName();

            String messageText = String.format("[*%s*](tg://user?id=%d), Добро пожаловать\\!", userName, userId);
            SendMessage sendMessageBuilder = SendMessage.builder()
                    .chatId(chatId)
                    .text(messageText)
                    .parseMode("MarkdownV2") // Указание parseMode для Markdown
                    .build();

            newMemberHandler.processNewChatMember(message);
            return sendMessageBuilder;
        }

        boolean isPrivateChat = message.getChat().isUserChat();
        boolean isGroupChat = message.getChat().isGroupChat() || message.getChat().isSuperGroupChat();

        boolean isMentionedText = message.hasText() && (isPrivateChat || (isGroupChat && telegramTextHandler.isBotMentioned(message)));
        boolean isMentionedVoice = message.hasVoice() && (isPrivateChat || (isGroupChat && telegramVoiceHandler.isBotVoiceCommand(message)));

        if (isMentionedText || isMentionedVoice) {
            telegramAsyncMessageSendler.sendMessageAsync(
                    chatId,
                    () -> {
                        try {
                            return handleMessageAsync(message);
                        } catch (MalformedURLException | TelegramApiException e) {
                            throw new RuntimeException("Произошла ошибка");
                        }
                    },
                    this::getErrorMessage,
                    message.getMessageThreadId()
            );
        }
        return null;
    }

    private SendMessage handleMessageAsync(Message message) throws MalformedURLException, TelegramApiException {
        SendMessage response;

        if (message.hasVoice()) {
            response = telegramVoiceHandler.processVoice(message);
        } else {
            response = telegramTextHandler.processTextMessage(message);
        }

        if (message.getMessageThreadId() != null) {
            response.setMessageThreadId(message.getMessageThreadId());
        }

        return response;
    }

    private SendMessage getErrorMessage(Throwable t) {
        log.error("Произошла ошибка", t);

        return SendMessage.builder()
                .text("Произошла ошибка")
                .build();
    }
}
