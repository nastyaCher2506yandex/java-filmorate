package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// добавим проверку на пустые объекты
// Spring автоматически залогирует некоректные данные
@Data
@EqualsAndHashCode(of = { "email" })
public class User {

    private Long id;

    @Email(message = "Отклонён запрос на создание пользователя: некорректный формат email")
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
