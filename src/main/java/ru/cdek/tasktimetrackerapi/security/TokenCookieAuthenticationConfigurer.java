package ru.cdek.tasktimetrackerapi.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;
import ru.cdek.tasktimetrackerapi.model.Token;
import ru.cdek.tasktimetrackerapi.model.User;
import ru.cdek.tasktimetrackerapi.service.token.TokenAuthenticationUserDetailsService;
import ru.cdek.tasktimetrackerapi.service.user.UserService;

import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

@Setter
@Accessors(chain = true, fluent = true)
public class TokenCookieAuthenticationConfigurer
        extends AbstractHttpConfigurer<TokenCookieAuthenticationConfigurer, HttpSecurity> {

    private static final String MAKE_TOKEN_DEACTIVATED_QUERY =
            "INSERT INTO deactivated_token (id, keep_until) VALUES (?, ?)";

    private JdbcTemplate jdbcTemplate;
    private UserService userService;
    private Function<String, Token> tokenCookieJweStringDeserializer;
    private String authUrl;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        builder.logout(logout -> logout
                .addLogoutHandler(this::handleLogout)
                .logoutUrl(authUrl + "/logout")
                .logoutSuccessHandler((request, response, authentication) ->
                        response.setStatus(HttpServletResponse.SC_NO_CONTENT)));
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Token token = extractTokenFromCookies(request);

        if (Objects.nonNull(token) && Objects.nonNull(authentication) &&
                authentication.getPrincipal() instanceof User) {
            jdbcTemplate.update(MAKE_TOKEN_DEACTIVATED_QUERY, token.id(), Date.from(token.expiresAt()));
        }
        new CookieClearingLogoutHandler(CookieName.HOST_AUTH_TOKEN.name())
                .logout(request, response, authentication);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private Token extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (Objects.nonNull(cookies)) {
            for (var cookie : cookies) {
                if (CookieName.HOST_AUTH_TOKEN.name().equals(cookie.getName())) {
                    return tokenCookieJweStringDeserializer.apply(cookie.getValue());
                }
            }
        }
        return null;
    }

    @Override
    public void configure(HttpSecurity builder) {
        var cookieAuthenticationFilter = new AuthenticationFilter(
                builder.getSharedObject(AuthenticationManager.class),
                new TokenCookieAuthenticationConverter(tokenCookieJweStringDeserializer, jdbcTemplate));

        cookieAuthenticationFilter.setSuccessHandler(
                (request, response, authentication) -> {
                });

        cookieAuthenticationFilter.setFailureHandler(
                new AuthenticationEntryPointFailureHandler(
                        new Http403ForbiddenEntryPoint()
                )
        );

        var authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(
                new TokenAuthenticationUserDetailsService(userService));

        builder.addFilterAfter(cookieAuthenticationFilter, CsrfFilter.class)
                .authenticationProvider(authenticationProvider);
    }
}
