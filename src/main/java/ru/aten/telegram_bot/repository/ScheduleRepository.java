package ru.aten.telegram_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.aten.telegram_bot.model.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>{

}
