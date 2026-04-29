package ru.cdek.tasktimetrackerapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TaskRequestDto(
        @NotBlank String name,
        @NotBlank String description
) {
}
