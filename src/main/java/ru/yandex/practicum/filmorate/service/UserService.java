package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

/*данный класс описывает бизнес-логику:
    1. Добавление пользователя в друзья (добавление у одного, добавляется другому)
    2. Удаление пользователя из друзей
    3. Вывод списка общих друзей
    4. Добавить пользователя
    5. Посмотреть всех пользователей
    6. Обновить данные о пользователе
 */

@Service
@Slf4j
public class UserService {

    //хранилище данных о пользователях и основая работа с ним
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    //получаем список все пользователей
    public Collection<User> findAll() {
        log.info("Получение всех пользователей.  Всего: {}", userStorage.sizeUser());
        return userStorage.findAll();
    }

    //создаем нового пользователя
    public User create(User user) {
        //проверяем пустой ли email
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Отклонён запрос на создание пользователя: email пустой");
            throw new ValidationException("Email пустой");
        }

        //проверяем, чтобы почта содержала символ "@"
        if (!user.getEmail().contains("@")) {
            log.warn("Отклонён запрос на создание пользователя: некорретный email");
            throw new ValidationException("Email некорректный");
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

        User result = userStorage.addUser(user);

        log.info("Создан пользовать с id={}",result.getId());
        return result;
    }

    //обновляем пользователя
    public User update(User user) {
        if (user.getId() == null) {
            log.warn("Отклонён запрос на обновление пользователя: не указан id пользователя");
            throw new ValidationException("Id должен быть указан");
        }

        if (userStorage.containsUser(user.getId())) {
            //если пользователь хочет поменять почту
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                //проверям корректная ли почта
                if (!user.getEmail().contains("@")) {
                    log.warn("Отклонён запрос на обновление пользователя: email некорректный формат");
                    throw new ValidationException("Неправильно указан адресс электронной почты");
                }
            }

            //если пользователь хочет поменять логин
            if (user.getLogin() != null && !user.getLogin().isBlank()) {
                if (user.getLogin().contains(" ")) {
                    log.warn("Отклонён запрос на обновление пользователя: логин содержит пробелы");
                    throw new ValidationException("Логин не может содержать пробел");
                }
            }

            //если пользователь хочет поменять почту
            if (user.getBirthday() != null) {
                if (user.getBirthday().isAfter(LocalDate.now())) {
                    log.warn("Отклонён запрос на обновление пользователя: день рождение указан в будующем");
                    throw new ValidationException("День рождение не может быть в будующем");
                }
            }

            User result = userStorage.updateUser(user);

            log.info("Обновлен пользовать с id={}",result.getId());
            return result;
        }

        log.warn("Отклонён запрос на обновление пользователя: пользователь с id={} не найден",user.getId());
        throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
    }

    //метод добавляет пользователя в друзья
    public User addFriends(long id, long idFriends) {
        //проверяем, что не указаны два одинаковый id
        if (id == idFriends) {
            log.warn("Отклонён запрос на добавления в друзья: указан один id");
            throw new DuplicatedDataException("Указаны для одинаковы id");
        }

        Optional<User> user = userStorage.findByIdUser(id);
        Optional<User> userFriends = userStorage.findByIdUser(idFriends);

        //проверка, что такие пользователи существуют
        if (user.isEmpty()) {
            log.warn("Отклонён запрос на добавления в друзья: пользователь с id={} не найден",id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        if (userFriends.isEmpty()) {
            log.warn("Отклонён запрос на добавления в друзья: пользователь с id={} не найден",idFriends);
            throw new NotFoundException("Пользователь с id=" + idFriends + " не найден");
        }

        //добавляем пользователей в друзья
        userStorage.addFriend(user.get(), userFriends.get());
        log.info("Пользователь с id={} добавлен в друзья пользователю с id={}",id,idFriends);
        return user.get();
    }

    //метод удаляет пользователя из друзей
    public User removeFriends(long id, long idFriends) {
        //проверяем, что не указаны два одинаковый id
        if (id == idFriends) {
            throw new DuplicatedDataException("Указаны для одинаковы id");
        }

        Optional<User> user = userStorage.findByIdUser(id);
        Optional<User> userFriends = userStorage.findByIdUser(idFriends);

        //проверка, что такие пользователи существуют
        if (user.isEmpty()) {
            log.warn("Отклонён запрос на удаление из друзей: пользователь с id={} не найден",id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        if (userFriends.isEmpty()) {
            log.warn("Отклонён запрос на удаление из друзей: пользователь с id={} не найден",idFriends);
            throw new NotFoundException("Пользователь с id=" + idFriends + " не найден");
        }

        //удаляем из друзей
        userStorage.removeFriend(user.get(), userFriends.get());

        log.info("Пользователь с id={} удален из друзьей пользователя с id={}",id,userFriends);
        return user.get();
    }

    //метод выводит список друзей пользователя
    public Collection<User> findFriedsUser(long id) {
        Optional<User> user = userStorage.findByIdUser(id);

        //проверка, что такие пользователь существует
        if (user.isEmpty()) {
            log.warn("Отклонён запрос на получение списка друзей: пользователь с id={} не найден",id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }

        log.info("Вывод друзей пользователя");
        return userStorage.findListFriendsUser(user.get());
    }

    //метод выводит общих друзей пользователя
    public Collection<User> findCommonFriends(long id, long otherId) {
        //проверяем, что не указаны два одинаковый id
        if (id == otherId) {
            throw new DuplicatedDataException("Указаны для одинаковы id");
        }

        Optional<User> user = userStorage.findByIdUser(id);
        Optional<User> userOther = userStorage.findByIdUser(otherId);

        //проверка, что такие пользователи существуют
        if (user.isEmpty()) {
            log.warn("Отклонён запрос на получение списка общих друзей: пользователь с id={} не найден",id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        if (userOther.isEmpty()) {
            log.warn("Отклонён запрос на получение списка общих друзей: пользователь с id={} не найден",otherId);
            throw new NotFoundException("Пользователь с id=" + otherId + " не найден");
        }

        log.info("Вывод общих друзей с пользователем");
        return userStorage.findCommonFriends(user.get(), userOther.get());
    }
}
