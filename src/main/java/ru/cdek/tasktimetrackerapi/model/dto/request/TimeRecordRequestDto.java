package ru.cdek.tasktimetrackerapi.model.dto.request;

import java.time.LocalDateTime;

public record TimeRecordRequestDto (
        Long taskId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String descriptionOfWork
) {
}
