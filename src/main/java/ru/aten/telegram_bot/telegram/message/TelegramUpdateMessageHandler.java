package ru.aten.telegram_bot.telegram.message;

import java.net.MalformedURLException;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.extern.slf4j.Slf4j;
import ru.aten.telegram_bot.command.TelegramCommandDispatcher;
import ru.aten.telegram_bot.command.handler.NewMemberHandler;
import ru.aten.telegram_bot.command.handler.edit.EditUserCommandHandler;
import ru.aten.telegram_bot.command.handler.edit.EditUserContext;
import ru.aten.telegram_bot.service.UserService;
import ru.aten.telegram_bot.telegram.TelegramAsyncMessageSendler;
import ru.aten.telegram_bot.telegram.TelegramBot;

@Slf4j
@Service
// @AllArgsConstructor
public class TelegramUpdateMessageHandler {

    private final TelegramCommandDispatcher telegramCommandDispatcher;
    private final TelegramAsyncMessageSendler telegramAsyncMessageSendler;
    private final TelegramTextHandler telegramTextHandler;
    private final TelegramVoiceHandler telegramVoiceHandler;
    private final NewMemberHandler newMemberHandler;
    private final EditUserCommandHandler editUserCommandHandler;
    private final UserService usersService;
    private final TelegramBot telegramBot;

    public TelegramUpdateMessageHandler(
            EditUserCommandHandler editUserCommandHandler,
            NewMemberHandler newMemberHandler,
            TelegramAsyncMessageSendler telegramAsyncMessageSendler,
            @Lazy TelegramBot telegramBot,
            TelegramCommandDispatcher telegramCommandDispatcher,
            TelegramTextHandler telegramTextHandler,
            TelegramVoiceHandler telegramVoiceHandler,
            UserService usersService) {
        this.editUserCommandHandler = editUserCommandHandler;
        this.newMemberHandler = newMemberHandler;
        this.telegramAsyncMessageSendler = telegramAsyncMessageSendler;
        this.telegramBot = telegramBot;
        this.telegramCommandDispatcher = telegramCommandDispatcher;
        this.telegramTextHandler = telegramTextHandler;
        this.telegramVoiceHandler = telegramVoiceHandler;
        this.usersService = usersService;
    }

    public BotApiMethod<?> handleMessage(Message message) throws TelegramApiException {
        Map<Long, EditUserContext> editUserContexts = usersService.getEditUserContext();
        Long userId = message.getFrom().getId();

        if (telegramCommandDispatcher.isCommand(message)) {
            return telegramCommandDispatcher.processCommand(message);
        }

        if (editUserContexts.containsKey(userId) && editUserContexts.get(userId).isWaiting()) {
            EditUserContext context = editUserContexts.get(userId);
            String newValue = message.getText();

            editUserContexts.remove(userId);
            usersService.setEditUserContext(editUserContexts);

            return editUserCommandHandler.requestNewValue(context.getCallbackQuery(), context.getTelegramId(), context.getFieldName(), newValue);
        }

        var chatId = message.getChatId().toString();

        if (!message.getNewChatMembers().isEmpty()) {
            for (User newMember : message.getNewChatMembers()) {
                Long telegramId = newMember.getId();
                String userName = newMember.getFirstName();
                String messageText = String.format("[*%s*](tg://user?id=%d), Добро пожаловать\\!", userName, telegramId);

                SendMessage sendMessageBuilder = SendMessage.builder()
                        .chatId(chatId)
                        .text(messageText)
                        .parseMode("MarkdownV2") // Указание parseMode для Markdown
                        .build();


                telegramBot.execute(sendMessageBuilder);
            }
            newMemberHandler.processNewChatMember(message);
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
