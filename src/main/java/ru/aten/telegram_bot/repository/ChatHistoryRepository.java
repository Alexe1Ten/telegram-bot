package ru.aten.telegram_bot.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.aten.telegram_bot.model.ChatHistory;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long>{
    Optional<ChatHistory> findByUserId(Long userId);
}
