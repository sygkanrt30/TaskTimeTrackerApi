package ru.cdek.tasktimetrackerapi.service.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.cdek.tasktimetrackerapi.ecxeption.RegistrationException;
import ru.cdek.tasktimetrackerapi.model.User;
import ru.cdek.tasktimetrackerapi.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";

    @Test
    @DisplayName("Поиск пользователя по имени - пользователь найден")
    void findByUsername_whenUserExists_shouldReturnUser() {
        User expectedUser = new User(USERNAME, ENCODED_PASSWORD);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(expectedUser));

        Optional<User> result = userService.findByUsername(USERNAME);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    @DisplayName("Поиск пользователя по имени - пользователь не найден")
    void findByUsername_whenUserDoesNotExist_shouldReturnEmpty() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername(USERNAME);

        assertFalse(result.isPresent());
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    @DisplayName("Сохранение пользователя - успешное сохранение")
    void save_whenValidData_shouldSaveUser() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(userRepository).save(any(User.class));

        assertDoesNotThrow(() -> userService.save(USERNAME, PASSWORD.getBytes()));

        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Сохранение пользователя - при сохранении возникает исключение")
    void save_whenRepositoryThrowsException_shouldThrowRegistrationException() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doThrow(new RuntimeException("Database error")).when(userRepository).save(any(User.class));

        assertThrows(RegistrationException.class, () -> userService.save(USERNAME, PASSWORD.getBytes()));

        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Сохранение пользователя - с пустым паролем")
    void save_whenEmptyPassword_shouldSaveUser() {
        String emptyPassword = "";
        when(passwordEncoder.encode(emptyPassword)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(userRepository).save(any(User.class));

        assertDoesNotThrow(() -> userService.save(USERNAME, emptyPassword.getBytes()));

        verify(passwordEncoder).encode(emptyPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Сохранение пользователя - с пустым именем")
    void save_whenEmptyUsername_shouldSaveUser() {
        String emptyUsername = "";
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(userRepository).save(any(User.class));

        assertDoesNotThrow(() -> userService.save(emptyUsername, PASSWORD.getBytes()));

        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Сохранение пользователя - многократный вызов")
    void save_whenCalledMultipleTimes_shouldSaveMultipleUsers() {
        String username2 = "testuser2";
        String password2 = "password456";
        String encodedPassword2 = "encodedPassword456";

        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(passwordEncoder.encode(password2)).thenReturn(encodedPassword2);
        doNothing().when(userRepository).save(any(User.class));

        assertDoesNotThrow(() -> userService.save(USERNAME, PASSWORD.getBytes()));
        assertDoesNotThrow(() -> userService.save(username2, password2.getBytes()));

        verify(passwordEncoder).encode(PASSWORD);
        verify(passwordEncoder).encode(password2);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    @DisplayName("Сохранение пользователя - при кодировании пароля возникает исключение")
    void save_whenPasswordEncoderThrowsException_shouldThrowRegistrationException() {
        when(passwordEncoder.encode(PASSWORD)).thenThrow(new RuntimeException("Encoding error"));

        assertThrows(RegistrationException.class, () -> userService.save(USERNAME, PASSWORD.getBytes()));

        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Сохранение пользователя - с очень длинным паролем")
    void save_whenVeryLongPassword_shouldSaveUser() {
        String longPassword = "a".repeat(1000);
        when(passwordEncoder.encode(longPassword)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(userRepository).save(any(User.class));

        assertDoesNotThrow(() -> userService.save(USERNAME, longPassword.getBytes()));

        verify(passwordEncoder).encode(longPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Сохранение пользователя - с спецсимволами в имени")
    void save_whenUsernameWithSpecialChars_shouldSaveUser() {
        String usernameWithSpecialChars = "user@123#test";
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        doNothing().when(userRepository).save(any(User.class));

        assertDoesNotThrow(() -> userService.save(usernameWithSpecialChars, PASSWORD.getBytes()));

        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(any(User.class));
    }
}