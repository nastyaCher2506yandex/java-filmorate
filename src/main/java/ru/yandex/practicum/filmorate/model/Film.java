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

    //содержит список id пользователей, которые лайкнули фотографию
    private Set<Long> likes = new HashSet<>();
}
