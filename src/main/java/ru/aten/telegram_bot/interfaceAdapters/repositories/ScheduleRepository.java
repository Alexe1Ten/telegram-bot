package ru.aten.telegram_bot.interfaceAdapters.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.aten.telegram_bot.entities.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long>{

}
