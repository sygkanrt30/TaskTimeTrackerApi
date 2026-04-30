package ru.cdek.tasktimetrackerapi.service.token;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.cdek.tasktimetrackerapi.model.Token;
import ru.cdek.tasktimetrackerapi.model.User;
import ru.cdek.tasktimetrackerapi.service.user.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationUserDetailsServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private TokenAuthenticationUserDetailsService tokenAuthenticationUserDetailsService;

    @Test
    void loadUserDetails_shouldReturnUserDetails_userExists() {
        var token = Instancio.create(Token.class);
        var user = Instancio.create(User.class);
        var authenticationToken = new PreAuthenticatedAuthenticationToken(token, "test");
        when(userService.findByUsername(any())).thenReturn(Optional.of(user));

        var result = tokenAuthenticationUserDetailsService.loadUserDetails(authenticationToken);

        assertDoesNotThrow(() -> tokenAuthenticationUserDetailsService.loadUserDetails(authenticationToken));
        assertEquals(user, result);
    }

    @Test
    void loadUserDetails_shouldTrowException_userNotExists() {
        var token = Instancio.create(Token.class);
        var authenticationToken = new PreAuthenticatedAuthenticationToken(token, "test");
        when(userService.findByUsername(any())).thenReturn(Optional.empty());

        var exception =  assertThrows(UsernameNotFoundException.class,
                () -> tokenAuthenticationUserDetailsService.loadUserDetails(authenticationToken));
        assertEquals("Username " + token.username() + " not found", exception.getMessage());
    }

    @Test
    void loadUserDetails_shouldTrowException_principalIstToken() {
        var authenticationToken = new PreAuthenticatedAuthenticationToken("test", "test");

        var exception =  assertThrows(UsernameNotFoundException.class,
                () -> tokenAuthenticationUserDetailsService.loadUserDetails(authenticationToken));
        assertEquals("Principal must be of type Token", exception.getMessage());
    }
}
