package ru.cdek.tasktimetrackerapi.model.dto.response;

public record EmployeeWorkTimeResponse (
        int hours,
        int minutes
) {

    public static EmployeeWorkTimeResponse defaultResponse() {
        return new EmployeeWorkTimeResponse(0, 0);
    }

    public static EmployeeWorkTimeResponse of(int hours, int minutes) {
        return new EmployeeWorkTimeResponse(hours, minutes);
    }
}
