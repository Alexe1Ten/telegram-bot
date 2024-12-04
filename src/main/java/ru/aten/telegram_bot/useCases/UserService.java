package ru.aten.telegram_bot.useCases;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import ru.aten.telegram_bot.entities.Role;
import ru.aten.telegram_bot.entities.User;
import ru.aten.telegram_bot.entities.UserInfo;
import ru.aten.telegram_bot.interfaceAdapters.repositories.UserInfoRepository;
import ru.aten.telegram_bot.interfaceAdapters.repositories.UserRepository;
import ru.aten.telegram_bot.useCases.command.handler.edit.EditUserContext;
import ru.aten.telegram_bot.useCases.exceptions.UserNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private Map<Long, EditUserContext> editUserContext;
    private final Long groupId;

    public UserService(
            UserRepository userRepository,
            UserInfoRepository userInfoRepository,
            Map<Long, EditUserContext> editUserContext,
            @Value("${telegram.group-id}") Long groupId
    ) {
        this.userRepository = userRepository;
        this.userInfoRepository = userInfoRepository;
        this.editUserContext = editUserContext;
        this.groupId = groupId;
    }

    public void addUser(User user) {
        if (user.getUserInfo() == null) {
            UserInfo userInfo = new UserInfo();
            userInfoRepository.save(userInfo);
            user.setUserInfo(userInfo);
        }
        if (!userRepository.existsByTelegramId(user.getTelegramId())) {
            userRepository.save(user);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    @Transactional
    public void updateUser(User user) throws UserNotFoundException {
        if (!userRepository.existsByTelegramId(user.getTelegramId())) {
            throw new UserNotFoundException("Пользователь не найден с ID: " + user.getTelegramId());
        }
        userInfoRepository.save(user.getUserInfo());
        userRepository.save(user);
    }

    public boolean existsByTelegramId(Long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }

    public boolean removeContext(Long telegramid) {
        try {
            editUserContext.remove(telegramid);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PostConstruct
    public void initializeAdminUser() {
        if (!userRepository.existsByTelegramId(Long.valueOf(835880897))) {
            UserInfo userInfo = new UserInfo();
            userInfoRepository.save(userInfo);
            User adminUser = User.builder()
                    .telegramId(Long.valueOf(835880897))
                    .firstName("Алексей")
                    .lastName("Тен")
                    .role(Role.ADMIN)
                    .userInfo(userInfo)
                    .build();
            userRepository.save(adminUser);

        }
    }

    public boolean isAdmin(Long telegramId) {
        Optional<User> userOptional = userRepository.findByTelegramId(telegramId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return "ADMIN".equalsIgnoreCase(user.getRole().getValue());
        } else {
            return false;
        }
    }

    public Map<Long, EditUserContext> getEditUserContext() {
        return editUserContext;
    }

    public void setEditUserContext(Map<Long, EditUserContext> editUserContext) {
        this.editUserContext = editUserContext;
    }

    public Long getGroupId() {
        return groupId;
    }
}
