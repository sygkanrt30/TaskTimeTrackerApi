package ru.cdek.tasktimetrackerapi.model.dto.response;

import ru.cdek.tasktimetrackerapi.model.TaskStatus;

public record TaskResponseDto(
        Long id,
        String name,
        String description,
        TaskStatus status
) {
}
