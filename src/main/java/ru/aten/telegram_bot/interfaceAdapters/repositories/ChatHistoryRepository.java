package ru.aten.telegram_bot.interfaceAdapters.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.aten.telegram_bot.entities.ChatHistory;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long>{
    Optional<ChatHistory> findByUserId(Long userId);
}
