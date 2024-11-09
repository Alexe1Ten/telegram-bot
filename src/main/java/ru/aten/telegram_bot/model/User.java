package ru.aten.telegram_bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.aten.telegram_bot.model.annotations.AdminOnly;
import ru.aten.telegram_bot.model.annotations.Displayable;
import ru.aten.telegram_bot.model.annotations.FieldDisplayName;
import ru.aten.telegram_bot.model.annotations.Modifiable;
import ru.aten.telegram_bot.model.enums.Position;
import ru.aten.telegram_bot.model.enums.Role;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Displayable(value = false)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private Long id;

    @Displayable(value = true)
    @Modifiable(value = false)
    @AdminOnly(value = true)
    @Column(unique = true)
    private Long telegramId;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value = "Имя")
    private String firstName;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value = "Фамилия")
    private String lastName;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value = "Отчество")
    private String patronymic;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value = "Должность")
    private Position position;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value = "Роль")
    private Role role;

    @OneToOne
    @Displayable(value = false)
    @JoinColumn(name = "user_info_id", referencedColumnName = "id")
    private UserInfo userInfo;

}
