package ru.aten.telegram_bot.interfaceAdapters.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.aten.telegram_bot.entities.User;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findUserById(Long userId);
    boolean existsByTelegramId(Long telegramId);
    Optional<User> findByTelegramId(Long telegramId);
}
