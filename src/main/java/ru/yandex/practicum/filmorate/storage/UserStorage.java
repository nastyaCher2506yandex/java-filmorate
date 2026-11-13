package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    //метод возвращает количество пользователей
    public int sizeUser();

    //метод возращает всех пользователей
    public Collection<User> findAll();

    //метод возращает пользователя по id
    public Optional<User> findByIdUser(long userId);

    //метод для добавления элемента
    public User addUser(User user);

    //метод обновления данных о пользователе\
    public User updateUser(User user);

    //метод добавления в друзья
    public void addFriend(User user, User userFriend);

    //метод удаления из друзей
    public void removeFriend(User user, User userFriend);

    //метод возращает список друзей
    public Collection<User> findListFriendsUser(User user);

    //метод возращает есть ли элемент в списке
    public boolean containsUser(long id);

    //метод возращает список общих друзей
    public Collection<User> findCommonFriends(User user, User otherUser);
}
