package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

// добавим проверку на пустые объекты
// Spring автоматически залогирует некоректные данные
@Data
@EqualsAndHashCode(of = { "email" })
@RequiredArgsConstructor
public class User {

    private Long id;

    @Email(message = "Отклонён запрос на создание пользователя: некорректный формат email")
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    //содержит список id пользователей, которые являются друзьями
    private Set<Long> friends = new HashSet<>();
}
