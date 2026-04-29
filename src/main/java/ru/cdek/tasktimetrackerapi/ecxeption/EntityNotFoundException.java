package ru.cdek.tasktimetrackerapi.ecxeption;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends TaskTimeTrackerApiException {

    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
