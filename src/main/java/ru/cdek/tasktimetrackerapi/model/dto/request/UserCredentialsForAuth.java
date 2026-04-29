package ru.cdek.tasktimetrackerapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserCredentialsForAuth(
        @Pattern(regexp = "^[A-Za-zА-Яа-яЁё][A-Za-zА-Яа-яЁё0-9_]{0,24}$",
                message = "Incorrect username")
        @NotBlank
        String username,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)" +
                "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*[a-zA-Z].*$",
                message = "Incorrect password")
        @NotBlank
        String password
) {
}
