package ru.yandex.practicum.filmorate.model;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@RequiredArgsConstructor
public class Film {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    private String genre;
    /*
    G — у фильма нет возрастных ограничений,
    PG — детям рекомендуется смотреть фильм с родителями,
    PG-13 — детям до 13 лет просмотр не желателен,
    R — лицам до 17 лет просматривать фильм можно только в присутствии взрослого,
    NC-17 — лицам до 18 лет просмотр запрещён.
     */
    private String range;

    //содержит список id пользователей, которые лайкнули фотографию
    private Set<Long> likes = new HashSet<>();
}
