package ru.cdek.tasktimetrackerapi.service.user;

import ru.cdek.tasktimetrackerapi.model.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByUsername(String username);

    User getUserByUsername(String username);

    void save(String username, byte[] password);
}
