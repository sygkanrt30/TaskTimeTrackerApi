package ru.cdek.tasktimetrackerapi.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.cdek.tasktimetrackerapi.ecxeption.RegistrationException;
import ru.cdek.tasktimetrackerapi.model.User;
import ru.cdek.tasktimetrackerapi.repository.UserRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserByUsername(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public void save(String username, byte[] password) {
        try {
            var user = new User(username, passwordEncoder.encode(new String(password)));
            userRepository.save(user);
            log.info("Saved user: {}", user);
        } catch (Exception e) {
            throw new RegistrationException(e.getMessage(), e);
        }
    }
}
