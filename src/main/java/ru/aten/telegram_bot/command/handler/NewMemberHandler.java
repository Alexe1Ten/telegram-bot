package ru.aten.telegram_bot.command.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.service.UserService;

@Component
@AllArgsConstructor
public class NewMemberHandler {

    private final UserService userService;

    public void processNewChatMember(Message message) {
        
            Long userId = message.getNewChatMembers().getFirst().getId();
            String firstName = message.getNewChatMembers().getFirst().getFirstName();
            // Long groupId = update.getChatMember().getChat().getId();

            userService.addUser(User.builder()
                .telegramId(userId)
                .firstName(firstName)
                // .groupId(groupId)
                .build()
            );
    }
}
