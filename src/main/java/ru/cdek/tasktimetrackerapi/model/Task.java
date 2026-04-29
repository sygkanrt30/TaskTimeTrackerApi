package ru.cdek.tasktimetrackerapi.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class Task {

    private Long id;
    private String name;
    private String description;
    private TaskStatus status;
    private Instant createdAt;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }
}
