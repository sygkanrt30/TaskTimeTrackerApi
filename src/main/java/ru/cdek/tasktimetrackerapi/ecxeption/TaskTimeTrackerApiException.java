package ru.cdek.tasktimetrackerapi.ecxeption;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
public class TaskTimeTrackerApiException extends RuntimeException {

    private final HttpStatus responseStatus;

    protected TaskTimeTrackerApiException(String message, Throwable cause, HttpStatus responseStatus) {
        super(message, cause);
        this.responseStatus = responseStatus;
    }

    protected TaskTimeTrackerApiException(String message, HttpStatus responseStatus) {
        super(message);
        this.responseStatus = responseStatus;
    }
}
