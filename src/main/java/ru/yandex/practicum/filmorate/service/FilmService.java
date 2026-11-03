package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final Map<Long, Film> films;

    public Collection<Film> findAll() {
        log.info("Получение всех фильмов.  Всего: {}", films.size());
        return films.values();
    }
    /*
    название не может быть пустым;
    максимальная длина описания — 200 символов;
    дата релиза — не раньше 28 декабря 1895 года;
    продолжительность фильма должна быть положительным числом.
     */

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

        film.setId(getNextId());
        films.put(film.getId(),film);
        log.info("Создан фильм с id={}",film.getId());

        return film;
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Отклонён запрос на обновление фильма: не указан id фильма");
            throw new ValidationException("Id должен быть указан");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                oldFilm.setName(newFilm.getName());
            }

            if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
                if (newFilm.getDescription().length() > 200) {
                    log.warn("Отклонён запрос на обновление фильма: превышена максимальная длина описания фильма (200 символов)");
                    throw new ValidationException("Превышена максимальная длина описания фильма (200 символов)");
                }

                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null) {
                if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895,12,28))) {
                    log.warn("Отклонён запрос на обновление фильма: дата реализации фильма раньше 28 декабря 1895 года");
                    throw new ValidationException("Фильм не должен быть раньше 28 декабря 1895 года");
                }

                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() != null) {
                if (newFilm.getDuration() <= 0) {
                    log.warn("Отклонён запрос на обновление фильма: продолжительность фильма не положительное число");
                    throw new ValidationException("Продолжительность фильма должна быть положительным числом");
                }

                oldFilm.setDuration(newFilm.getDuration());
            }
            log.info("Обновлен фильм с id={}",oldFilm.getId());

            return oldFilm;
        }

        log.warn("Отклонён запрос на обновление фильма: фильма с id={} не найден",newFilm.getId());
        throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
