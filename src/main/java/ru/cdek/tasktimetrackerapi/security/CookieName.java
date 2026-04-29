package ru.cdek.tasktimetrackerapi.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
enum CookieName {
    HOST_AUTH_TOKEN("__Host-auth-token");

    private final String name;
}
