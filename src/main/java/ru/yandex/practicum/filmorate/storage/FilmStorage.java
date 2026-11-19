package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    //метод выводит данные о всех фильмах
    public Collection<Film> findAll();

    //метод возращает количество фильмов
    public int sizeFilms();

    //метод обновляет данные о фильме
    public Film updateFilm(Film film);

    //метод добавления фильма
    public Film addFilm(Film film);

    //метод поиска по id
    public Optional<Film> findByIdFilm(long filmId);

    //метод выявляет есть ли фильм в списке
    public boolean containsFilm(long id);

    //метод возращает количество лайков
    public int countLikesFilm(long id);

    //метод добавляет лайк фильму
    public void addLike(Film film, User user);

    //метод удаляет лайк у фильма
    public Film removeLike(Film film, User user);

    //вывод списка самых популярных фильмов (количество size)
    public Collection<Film> findPopularFilm(int size);
}
