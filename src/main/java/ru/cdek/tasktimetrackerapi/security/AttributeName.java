package ru.cdek.tasktimetrackerapi.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttributeName {
    USER_ID("userId");

    private final String value;
}
