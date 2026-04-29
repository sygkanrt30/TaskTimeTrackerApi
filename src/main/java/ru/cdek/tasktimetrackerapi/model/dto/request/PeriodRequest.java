package ru.cdek.tasktimetrackerapi.model.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PeriodRequest(
        @NotNull LocalDateTime start,
        @NotNull LocalDateTime end
) {
    public PeriodRequest {
        if (start.isAfter(end)) throw new IllegalArgumentException("Start date must be after end date");
    }
}
