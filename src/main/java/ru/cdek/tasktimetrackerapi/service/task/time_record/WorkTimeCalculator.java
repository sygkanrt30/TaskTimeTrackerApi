package ru.cdek.tasktimetrackerapi.service.task.time_record;

import ru.cdek.tasktimetrackerapi.model.TimeRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

final class WorkTimeCalculator {

    int calculateTotalMinutes(List<TimeRecord> timeRecords, LocalDateTime startPeriod, LocalDateTime endPeriod) {
        long totalMinutes = 0L;
        for (var timeRecord : timeRecords) {
            LocalDateTime startWork = timeRecord.getStartTime();
            LocalDateTime endWork = timeRecord.getEndTime();
            if (hasIntersection(startPeriod, endPeriod, startWork, endWork)) {

                LocalDateTime intersectionStart = getIntersectionStart(startPeriod, startWork);
                LocalDateTime intersectionEnd = getIntersectionEnd(endPeriod, endWork);
                totalMinutes += Duration.between(intersectionStart, intersectionEnd).toMinutes();
            }
        }
        return (int) totalMinutes;
    }

    private boolean hasIntersection(LocalDateTime startPeriod, LocalDateTime endPeriod,
                                    LocalDateTime startWork, LocalDateTime endWork) {
        return !endWork.isBefore(startPeriod) && !startWork.isAfter(endPeriod);
    }

    private LocalDateTime getIntersectionStart(LocalDateTime startPeriod, LocalDateTime startWork) {
        return startWork.isBefore(startPeriod) ? startPeriod : startWork;
    }

    private LocalDateTime getIntersectionEnd(LocalDateTime endPeriod, LocalDateTime endWork) {
        return endWork.isAfter(endPeriod) ? endPeriod : endWork;
    }
}
