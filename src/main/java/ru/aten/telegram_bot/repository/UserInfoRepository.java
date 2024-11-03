package ru.aten.telegram_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.aten.telegram_bot.model.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long>{

}
