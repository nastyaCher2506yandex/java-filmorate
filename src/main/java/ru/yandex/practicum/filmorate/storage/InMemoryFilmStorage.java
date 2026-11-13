package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    //таблица данных всех фильмов
    private final Map<Long, Film> films = new HashMap<>();

    //для сортировки фильмов по популярности
    private final Comparator<Film> filmLikesComparator = Comparator.comparingInt(film -> film.getLikes().size());

    //получение списка всех фильмов
    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    //получение количества фильмов
    @Override
    public int sizeFilms() {
        return films.size();
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(),film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        Film oldFilm = films.get(film.getId());

        //проверяем, какие данных хотят изменить и меняем их
        if (film.getName() != null && !film.getName().isBlank()) {
            oldFilm.setName(film.getName());
        }

        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            oldFilm.setDescription(film.getDescription());
        }

        if (film.getReleaseDate() != null) {
            oldFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != null) {
            oldFilm.setDuration(film.getDuration());
        }

        return oldFilm;
    }

    @Override
    public boolean containsFilm(long id) {
        return films.containsKey(id);
    }

    @Override
    public Optional<Film> findByIdFilm(long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public void addLike(Film film, User user) {
        film.getLikes().add(user.getId());
    }

    @Override
    public Film removeLike(Film film, User user) {
        film.getLikes().remove(user.getId());

        return film;
    }

    @Override
    public int countLikesFilm(long id) {
        return films.get(id).getLikes().size();
    }

    @Override
    public Collection<Film> findPopularFilm(int size) {
        return films.values().stream()
                .sorted(filmLikesComparator.reversed())
                .limit(size)
                .toList();
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
