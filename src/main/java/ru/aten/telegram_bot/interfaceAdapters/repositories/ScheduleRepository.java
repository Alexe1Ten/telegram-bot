package ru.aten.telegram_bot.interfaceAdapters.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.aten.telegram_bot.entities.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>{

}
