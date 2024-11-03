package ru.aten.telegram_bot.command.handler;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.model.enums.Role;
import ru.aten.telegram_bot.service.UserService;

@Component
@AllArgsConstructor
public class NewMemberHandler {

    private final UserService userService;
    

    public void processNewChatMember(Message message) {
        Long groupId = userService.getGroupId();

        if (Objects.equals(message.getChatId(), groupId)) {
            for (User newMember : message.getNewChatMembers()) {
                Long userId = newMember.getId();
                String firstName = newMember.getFirstName();
    
                userService.addUser(ru.aten.telegram_bot.model.User.builder()
                        .telegramId(userId)
                        .firstName(firstName)
                        .role(Role.USER)
                        .build()
                );
            }
        }
    }
}
