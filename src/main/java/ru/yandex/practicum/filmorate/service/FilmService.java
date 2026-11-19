package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

/*данный класс описывает бизнес-логику:
    1. Поставить лайк на фильм (1 пользователь - 1 лайк)
    2. Удаление лайка
    3. Вывод 10 фильмов наиболее популярных фильмов по количеству лайков
    4. Получение списка всех фильмов
    5. Создание фильма
    6. Обновление данных о фильме
 */

@Service
@Slf4j
public class FilmService {

    //хранилище данных о фильмах и основные действия с ними
    private final FilmStorage filmStorage;

    //хранилище данных о пользователях
    //используется для работы с лайками
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    //получение списка всех фильмов
    public Collection<Film> findAll() {
        log.info("Получение всех фильмов.  Всего: {}", filmStorage.sizeFilms());
        return filmStorage.findAll();
    }

    //добавление нового фильма
    public Film create(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Отклонён запрос на создание фильма: название пустое");
            throw new ValidationException("Название фильма не должно быть пустым");
        }

        if (film.getDescription() == null || film.getDescription().isBlank()) {
            film.setDescription("");
        }

        if (film.getDescription().length() > 200) {
            log.warn("Отклонён запрос на создание фильма: превышена максимальная длина описания фильма (200 символов)");
            throw new ValidationException("Превышена максимальная длина описания фильма (200 символов)");
        }

        if (film.getReleaseDate() == null) {
            log.warn("Отклонён запрос на создание фильма: не указана дата релиза");
            throw new ValidationException("Не указана дата релиза");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
            log.warn("Отклонён запрос на создание фильма: дата реализации фильма раньше 28 декабря 1895 года");
            throw new ValidationException("Фильм не должен быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null) {
            log.warn("Отклонён запрос на создание фильма: не указана продолжительность фильмов");
            throw new ValidationException("Не указана продолжительность фильма");
        }

        if (film.getDuration() <= 0) {
            log.warn("Отклонён запрос на создание фильма: продолжительность фильма не положительное число");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        Film result = filmStorage.addFilm(film);

        log.info("Создан фильм с id={}",result.getId());
        return result;
    }

    public Film update(Film newFilm) {
        //проверка, что id фильма, который хотят обновить, был указан
        if (newFilm.getId() == null) {
            log.warn("Отклонён запрос на обновление фильма: не указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }

        //проверяем, есть ли фильм с таким id
        if (filmStorage.containsFilm(newFilm.getId())) {
            //проверяем, если хотят изменить описание, чтобы оно соответствовало всем требованиям
            if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
                if (newFilm.getDescription().length() > 200) {
                    log.warn("Отклонён запрос на обновление фильма: превышена максимальная длина описания фильма (200 символов)");
                    throw new ValidationException("Превышена максимальная длина описания фильма (200 символов)");
                }
            }

            //проверяем, если хотят обновить данные о дате резила, чтобы оно соответствовало требованиями
            if (newFilm.getReleaseDate() != null) {
                if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                    log.warn("Отклонён запрос на обновление фильма: дата реализации фильма раньше 28 декабря 1895 года");
                    throw new ValidationException("Фильм не должен быть раньше 28 декабря 1895 года");
                }
            }

            //проверяем, если хотят обновить данные о продолжительности фильма, чтобы оно соответствовало требованиями
            if (newFilm.getDuration() != null) {
                if (newFilm.getDuration() <= 0) {
                    log.warn("Отклонён запрос на обновление фильма: продолжительность фильма не положительное число");
                    throw new ValidationException("Продолжительность фильма должна быть положительным числом");
                }
            }

            Film result = filmStorage.updateFilm(newFilm);
            log.info("Обновлен фильм с id={}",result.getId());

            return result;
        }

        log.warn("Отклонён запрос на обновление фильма: фильма с id={} не найден",newFilm.getId());
        throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
    }

    //добавление лайка
    public Film addLike(long id, long idUser) {
        //получаем фильм и пользователя
        Optional<Film> film = filmStorage.findByIdFilm(id);
        Optional<User> user = userStorage.findByIdUser(idUser);

        //проверяем есть ли фильм и пользователь
        if (film.isEmpty()) {
            log.warn("Отклонён запрос на добавления лайка: фильма с id={} не найден",id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        if (user.isEmpty()) {
            log.warn("Отклонён запрос на добавления лайка: пользователь с id={} не найден",idUser);
            throw new NotFoundException("Пользователь с id=" + idUser + " не найден");
        }

        log.info("Пользователь с id={} добавил лайк фильму с id={}", idUser, id);
        filmStorage.addLike(film.get(),user.get());
        return film.get();
    }

    //удаление лайка
    public Film removeLike(long id, long idUser) {
        //получаем фильм и пользователя
        Optional<Film> film = filmStorage.findByIdFilm(id);
        Optional<User> user = userStorage.findByIdUser(idUser);

        if (film.isEmpty()) {
            log.warn("Отклонён запрос на удаление лайка: фильма с id={} не найден",id);
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        if (user.isEmpty()) {
            log.warn("Отклонён запрос на удаление лайка: пользователь с id={} не найден",idUser);
            throw new NotFoundException("Пользователь с id=" + idUser + " не найден");
        }

        log.info("Пользователь с id={} удалил лайк у фильма с id={}", idUser, id);
        return filmStorage.removeLike(film.get(), user.get());
    }

    //вывод популярных фильмов (размер списка size)
    public Collection<Film> findPopularFilm(int size) {
        if (size <= 0) {
            log.warn("Отклонён запрос на получение популярных фильмов: размер должен быть больше 0");
            throw new ValidationException("Размер должен быть больше нуля");
        }

        Collection<Film> result = filmStorage.findPopularFilm(size);

        log.info("Было получено {} популярных фильмов по лайкам", result.size());
        return result;
    }
}
