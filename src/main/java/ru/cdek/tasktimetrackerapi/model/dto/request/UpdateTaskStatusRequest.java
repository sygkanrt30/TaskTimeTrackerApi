package ru.cdek.tasktimetrackerapi.model.dto.request;

import jakarta.validation.constraints.NotNull;
import ru.cdek.tasktimetrackerapi.model.TaskStatus;

public record UpdateTaskStatusRequest(
        @NotNull Long taskId,
        @NotNull TaskStatus status
) {
}
