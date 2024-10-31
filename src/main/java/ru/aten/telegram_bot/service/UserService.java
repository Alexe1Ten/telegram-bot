package ru.aten.telegram_bot.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import ru.aten.telegram_bot.command.handler.edit.EditUserContext;
import ru.aten.telegram_bot.exceptions.UserNotFoundException;
import ru.aten.telegram_bot.model.User;
import ru.aten.telegram_bot.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private Map<Long, EditUserContext> editUserContext;

    public void addUser(User user) {
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
        // Если пользователь не найден, вы можете выбросить исключение или обработать это иначе
        if (!userRepository.existsByTelegramId(user.getTelegramId())) {
            throw new UserNotFoundException("Пользователь не найден с ID: " + user.getTelegramId());
        }
        userRepository.save(user); // Этот метод сохранит изменения в базе данных
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

    public boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole().getValue());
    }

    public Map<Long, EditUserContext> getEditUserContext() {
        return editUserContext;
    }

    public void setEditUserContext(Map<Long, EditUserContext> editUserContext) {
        this.editUserContext = editUserContext;
    }
}
