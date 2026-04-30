package ru.cdek.tasktimetrackerapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TimeRecordRequestDto (
        @NotNull Long taskId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotBlank String descriptionOfWork
) {
}
