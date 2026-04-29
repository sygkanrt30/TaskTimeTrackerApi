package ru.cdek.tasktimetrackerapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.cdek.tasktimetrackerapi.ecxeption.RegistrationException;
import ru.cdek.tasktimetrackerapi.security.TokenCookieSessionAuthenticationStrategy;

@RequiredArgsConstructor
@Component
@Slf4j
class Authenticator {

    private final TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy;
    private final AuthenticationManager authenticationManager;

    void authenticateAndSetCookie(HttpServletRequest request, HttpServletResponse response,
                                  String username, byte[] password) {
        log.trace("trying to authenticate user with username ({}) after registration", username);
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, new String(password))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            tokenCookieSessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        } catch (Exception e) {
            throw new RegistrationException("Authentication failed after registration", e);
        }
        log.debug("successfully authenticated user with username ({}) after registration", username);
    }
}
