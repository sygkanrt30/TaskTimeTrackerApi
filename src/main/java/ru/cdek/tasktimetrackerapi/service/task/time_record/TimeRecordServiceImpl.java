package ru.cdek.tasktimetrackerapi.service.task.time_record;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.cdek.tasktimetrackerapi.ecxeption.EntityNotFoundException;
import ru.cdek.tasktimetrackerapi.model.Task;
import ru.cdek.tasktimetrackerapi.model.TaskStatus;
import ru.cdek.tasktimetrackerapi.model.TimeRecord;
import ru.cdek.tasktimetrackerapi.model.User;
import ru.cdek.tasktimetrackerapi.model.dto.request.PeriodRequest;
import ru.cdek.tasktimetrackerapi.model.dto.request.TimeRecordRequestDto;
import ru.cdek.tasktimetrackerapi.model.dto.response.EmployeeWorkTimeResponse;
import ru.cdek.tasktimetrackerapi.repository.TaskRepository;
import ru.cdek.tasktimetrackerapi.repository.TimeRecordRepository;
import ru.cdek.tasktimetrackerapi.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeRecordServiceImpl implements TimeRecordService {

    private final TimeRecordRepository timeRecordRepository;
    private final UserService userService;
    private final TaskRepository taskRepository;

    @Override
    public void save(TimeRecordRequestDto request, String username) {
        User user = userService.getUserByUsername(username);
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        validateTaskStatus(task);
        validateTimeRange(request.startTime(), request.endTime());

        TimeRecord timeRecord = createTimeRecord(user, task, request);
        timeRecordRepository.save(timeRecord);
        log.info("Saved time record {}", timeRecord);
    }

    private void validateTaskStatus(Task task) {
        if (!task.getStatus().equals(TaskStatus.DONE)) {
            throw new IllegalStateException("Can not save time record if task isn't done");
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time is after end time");
        }
        if (startTime.equals(endTime)) {
            throw new IllegalArgumentException("Start time cannot be equals end time");
        }
    }

    private TimeRecord createTimeRecord(User user, Task task, TimeRecordRequestDto request) {
        return new TimeRecord(
                user.getId(),
                task.getId(),
                request.startTime(),
                request.endTime(),
                request.descriptionOfWork()
        );
    }

    @Override
    public Optional<TimeRecord> findByTaskId(Long taskId) {
        return timeRecordRepository.findTaskId(taskId);
    }

    @Override
    public EmployeeWorkTimeResponse getTimeOfWorkForPeriod(PeriodRequest periodRequest, String username) {
        User user = userService.getUserByUsername(username);
        LocalDateTime start = periodRequest.start();
        LocalDateTime end = periodRequest.end();

        List<TimeRecord> timeRecords = timeRecordRepository.findAllOnPeriod(start, end, user.getId());

        if (timeRecords.isEmpty()) {
            log.warn("No time records found for user {} within a given period", username);
            return EmployeeWorkTimeResponse.defaultResponse();
        }
        return calculateEmployeeWorkTimeResponse(username, timeRecords, start, end);
    }

    private EmployeeWorkTimeResponse calculateEmployeeWorkTimeResponse(String username,
                                                                                List<TimeRecord> timeRecords,
                                                                                LocalDateTime start, LocalDateTime end) {
        var calculator = new WorkTimeCalculator();
        int totalMinutes = calculator.calculateTotalMinutes(timeRecords, start, end);
        int totalHours = totalMinutes / 60;
        int minutesRemainder =  totalMinutes % 60;
        log.info("Calculated time records for user {} within a given period. Total hours: {}", username, totalHours);
        return EmployeeWorkTimeResponse.of(totalHours, minutesRemainder);
    }
}
