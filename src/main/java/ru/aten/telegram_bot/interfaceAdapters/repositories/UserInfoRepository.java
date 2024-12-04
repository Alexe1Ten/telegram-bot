package ru.aten.telegram_bot.interfaceAdapters.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.aten.telegram_bot.entities.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long>{

}
