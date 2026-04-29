package ru.cdek.tasktimetrackerapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TimeRecord {

    private Long id;
    private Long userId;
    private Long taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String descriptionOfWork;
    private Instant createdAt;

    public TimeRecord(Long userId, Long taskId,
                      LocalDateTime startTime, LocalDateTime endTime, String descriptionOfWork) {
        this.userId = userId;
        this.taskId = taskId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.descriptionOfWork = descriptionOfWork;
    }
}
