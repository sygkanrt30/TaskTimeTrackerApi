package ru.cdek.tasktimetrackerapi.service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.cdek.tasktimetrackerapi.model.Token;
import ru.cdek.tasktimetrackerapi.service.user.UserService;

@RequiredArgsConstructor
public class TokenAuthenticationUserDetailsService implements
        AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final UserService userService;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken)
            throws UsernameNotFoundException {
        if (authenticationToken.getPrincipal() instanceof Token token) {
            return userService.findByUsername(token.username()).orElseThrow(
                    () ->  new UsernameNotFoundException("Username " + token.username() + " not found")
            );
        }
        throw new UsernameNotFoundException("Principal must be of type Token");
    }
}
