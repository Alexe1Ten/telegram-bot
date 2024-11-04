package ru.aten.telegram_bot.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.aten.telegram_bot.model.annotations.AdminOnly;
import ru.aten.telegram_bot.model.annotations.Displayable;
import ru.aten.telegram_bot.model.annotations.FieldDisplayName;
import ru.aten.telegram_bot.model.annotations.Modifiable;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    @Id
    @Displayable(value = false)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private Long id;

    @OneToOne(mappedBy= "userInfo")
    private User user;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value="Дата приема")
    private LocalDate dateOfAdmission;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value="Вахта")
    private String watch;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value="Номер телефона")
    private String phoneNumber;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value="Размер обуви")
    private Integer shoeSize;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value="Марка авто")
    private String carModel;

    @Displayable(value = true)
    @Modifiable(value = true)
    @AdminOnly(value = true)
    @FieldDisplayName(value="Гос номер")
    private String stateNumber;
}
