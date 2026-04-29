package ru.cdek.tasktimetrackerapi.ecxeption;

import org.springframework.http.HttpStatus;

public class RegistrationException extends TaskTimeTrackerApiException {

    public RegistrationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT);
    }
}
