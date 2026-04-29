package ru.cdek.tasktimetrackerapi.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.cdek.tasktimetrackerapi.model.Token;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class TokenCookieAuthenticationConverter implements AuthenticationConverter {

    private final static String FIND_IN_DEACTIVATED_TOKEN =
            "SELECT COUNT(*) FROM deactivated_token WHERE id = ?";

    private final Function<String, Token> tokenCookieStringDeserializer;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Stream.of(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(CookieName.HOST_AUTH_TOKEN.name()))
                    .findFirst()
                    .map(cookie -> {
                        var token = tokenCookieStringDeserializer.apply(cookie.getValue());
                        Long rowsCount = jdbcTemplate.queryForObject(FIND_IN_DEACTIVATED_TOKEN, Long.class, token.id());
                        if (Objects.nonNull(rowsCount) && rowsCount > 0) {
                            return null;
                        }
                        request.setAttribute(AttributeName.USER_ID.getValue(), token.userId());
                        return new PreAuthenticatedAuthenticationToken(token, cookie.getValue());
                    })
                    .orElse(null);
        }
        return null;
    }
}
