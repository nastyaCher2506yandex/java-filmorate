package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {

    //хранилище для всех пользователей
    private final Map<Long, User> users;

    //переопределения метода, который возращает количество пользователей
    @Override
    public int sizeUser() {
        return users.size();
    }

    //переопределения метода, который возращает всех пользователей
    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    //переопределение метода на создание пользователя
    @Override
    public User addUser(User user) {
        //проверяем, что пользователей с такой почтой нет
        if (users.containsValue(user)) {
            log.warn("Отклонён запрос на создание пользователя: email уже зарегистрирован — {}", user.getEmail());
            throw new DuplicatedDataException("Данный имейл уже используется");
        }

        //проверяем пустое ли имя, если да, передаем в него логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(),user);

        return user;
    }

    //переопределение метода на обновление данных о пользователе
    @Override
    public User updateUser(User newUser) {
        User oldUser = users.get(newUser.getId());

        //проверяем, какие данных хотят изменить и меняем их
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            //проверяем, что это не почта текущего пользователя и нет еще одного пользователя с такой почтой
            if (users.containsValue(newUser)
                    && (!oldUser.getId().equals(newUser.getId()))) {
                log.warn("Отклонён запрос на обновление пользователя: email уже зарегистрирован — {}", newUser.getEmail());
                throw new DuplicatedDataException("Данный имейл уже используется");
            }

            oldUser.setEmail(newUser.getEmail());
        }

        if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
            oldUser.setLogin(newUser.getLogin());
        }

        if (newUser.getBirthday() != null) {
            oldUser.setBirthday(newUser.getBirthday());
        }

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            oldUser.setName(newUser.getName());
        }

        return oldUser;
    }

    //переопределение метода, который возращает пользователя по id
    @Override
    public Optional<User> findByIdUser(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void addFriend(User user, User userFriend) {
        Set<Long> userListFriends = user.getFriends();
        Set<Long> userFriendListFriends = userFriend.getFriends();

        userListFriends.add(userFriend.getId());
        userFriendListFriends.add(user.getId());
    }

    @Override
    public void removeFriend(User user, User userFriend) {
        Set<Long> userListFriends = user.getFriends();
        Set<Long> userFriendListFriends = userFriend.getFriends();

        userListFriends.remove(userFriend.getId());
        userFriendListFriends.remove(user.getId());
    }

    @Override
    public Collection<User> findListFriendsUser(User user) {
        return findUserByList(user.getFriends());
    }

    @Override
    public boolean containsUser(long id) {
        return users.containsKey(id);
    }

    @Override
    public Collection<User> findCommonFriends(User user, User otherUser) {
        Collection<Long> commonFriendById = findCommon(user, otherUser);

        return findUserByList(commonFriendById);
    }

    //возращает
    public Collection<User> findUserByList(Collection<Long> list) {
        return users.values().stream()
                .filter(user -> list.contains(user.getId()))
                .toList();
    }

    public Collection<Long> findCommon(User user, User otherUser) {
        return user.getFriends().stream()
                .filter(friend -> otherUser.getFriends().contains(friend))
                .toList();
    }

    //всмопогательный метод, который возращает следующий id для пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
