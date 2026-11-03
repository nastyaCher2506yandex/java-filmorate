package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    //хранилище для всех пользователей
    private final Map<Long, User> users;

    //получаем список все пользователей
    public Collection<User> findAll() {
        log.info("Получение всех пользователей.  Всего: {}", users.size());
        return users.values();
    }

    //создаем нового пользователя
    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Отклонён запрос на создание пользователя: email пустой");
            throw new ValidationException("Email пустой");
        }

        //проверяем, чтобы почта содержала символ "@"
        if (!user.getEmail().contains("@")) {
            log.warn("Отклонён запрос на создание пользователя: некорретный email");
            throw new ValidationException("Email некорректный");
        }

        //проверяем, что пользователей с такой почтой нет
        if (users.containsValue(user)) {
            log.warn("Отклонён запрос на создание пользователя: email уже зарегистрирован — {}", user.getEmail());
            throw new DuplicatedDataException("Данный имейл уже используется");
        }

        //проверяем, что логин не пустой
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Отклонён запрос на создание пользователя: логин пустой");
            throw new ValidationException("логин пустой");
        }

        //проверяем, чтобы логин не содержал пробелом
        if (user.getLogin().contains(" ")) {
            log.warn("Отклонён запрос на создание пользователя: логин содержит пробелы");
            throw new ValidationException("Логин не может содержать пробел");
        }

        //проверяем пустое ли имя, если да, передаем в него логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        //проверяем, что день рождение не в будующем
        if (user.getBirthday() == null) {
            log.warn("Отклонён запрос на создание пользователя: день рождение не передан");
            throw new ValidationException("Не было передано день рождение");
        }

        //проверяем, что день рождение не в будующем
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Отклонён запрос на создание пользователя: день рождение указан в будующем");
            throw new ValidationException("День рождение не может быть в будующем");
        }

        user.setId(getNextId());
        users.put(user.getId(),user);
        log.info("Создан пользовать с id={}",user.getId());

        return user;
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.warn("Отклонён запрос на обновление пользователя: не указан id пользователя");
            throw new ValidationException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            //если пользователь хочет помять почту
            if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
                if (users.containsValue(newUser)) {
                    log.warn("Отклонён запрос на обновление пользователя: email уже зарегистрирован — {}", newUser.getEmail());
                    throw new DuplicatedDataException("Данный имейл уже используется");
                }

                if (!newUser.getEmail().contains("@")) {
                    log.warn("Отклонён запрос на обновление пользователя: email некорректный формат");
                    throw new ValidationException("Неправильно указан адресс электронной почты");
                }

                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
                if (newUser.getLogin().contains(" ")) {
                    log.warn("Отклонён запрос на обновление пользователя: логин содержит пробелы");
                    throw new ValidationException("Логин не может содержать пробел");
                }

                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getBirthday() != null) {
                if (newUser.getBirthday().isAfter(LocalDate.now())) {
                    log.warn("Отклонён запрос на обновление пользователя: день рождение указан в будующем");
                    throw new ValidationException("День рождение не может быть в будующем");
                }

                oldUser.setBirthday(newUser.getBirthday());
            }

            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
            }

            log.info("Обновлен пользовать с id={}",oldUser.getId());
            return oldUser;
        }

        log.warn("Отклонён запрос на обновление пользователя: пользователь с id={} не найден",newUser.getId());
        throw new NotFoundException("Пользователь с id=" + newUser.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
