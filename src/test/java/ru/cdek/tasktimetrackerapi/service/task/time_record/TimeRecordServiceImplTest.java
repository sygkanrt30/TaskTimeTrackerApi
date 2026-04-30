package ru.cdek.tasktimetrackerapi.service.task.time_record;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceImplTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private UserService userService;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TimeRecordServiceImpl timeRecordService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String USERNAME = "testuser";
    private static final Long USER_ID = 1L;
    private static final Long TASK_ID = 1L;
    private static final String DESCRIPTION = "Work description";

    @Test
    @DisplayName("Сохранение записи времени - успешное сохранение")
    void save_WhenValidData_ShouldSaveTimeRecord() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        Task task = new Task("Task name", "Task description");
        task.setId(TASK_ID);
        task.setStatus(TaskStatus.DONE);

        LocalDateTime startTime = LocalDateTime.parse("2026-04-25 09:00:00", FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse("2026-04-25 18:00:00", FORMATTER);

        TimeRecordRequestDto request = new TimeRecordRequestDto(
                TASK_ID,
                startTime,
                endTime,
                DESCRIPTION
        );

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        doNothing().when(timeRecordRepository).save(any(TimeRecord.class));

        assertDoesNotThrow(() -> timeRecordService.save(request, USERNAME));

        verify(userService).getUserByUsername(USERNAME);
        verify(taskRepository).findById(TASK_ID);
        verify(timeRecordRepository).save(any(TimeRecord.class));
    }

    @Test
    @DisplayName("Сохранение записи времени - пользователь не найден")
    void save_WhenUserNotFound_ShouldThrowException() {
        LocalDateTime startTime = LocalDateTime.parse("2026-04-25 09:00:00", FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse("2026-04-25 18:00:00", FORMATTER);

        TimeRecordRequestDto request = new TimeRecordRequestDto(
                TASK_ID,
                startTime,
                endTime,
                DESCRIPTION
        );

        when(userService.getUserByUsername(USERNAME)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class, () -> timeRecordService.save(request, USERNAME));

        verify(userService).getUserByUsername(USERNAME);
        verify(taskRepository, never()).findById(any());
        verify(timeRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Сохранение записи времени - задача не найдена")
    void save_WhenTaskNotFound_ShouldThrowEntityNotFoundException() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        LocalDateTime startTime = LocalDateTime.parse("2026-04-25 09:00:00", FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse("2026-04-25 18:00:00", FORMATTER);

        TimeRecordRequestDto request = new TimeRecordRequestDto(
                TASK_ID,
                startTime,
                endTime,
                DESCRIPTION
        );

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> timeRecordService.save(request, USERNAME));

        assertEquals("Task not found", exception.getMessage());
        verify(userService).getUserByUsername(USERNAME);
        verify(taskRepository).findById(TASK_ID);
        verify(timeRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Сохранение записи времени - статус задачи не DONE")
    void save_WhenTaskStatusIsNotDone_ShouldThrowIllegalStateException() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        Task task = new Task("Task name", "Task description");
        task.setId(TASK_ID);
        task.setStatus(TaskStatus.IN_PROGRESS);

        LocalDateTime startTime = LocalDateTime.parse("2026-04-25 09:00:00", FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse("2026-04-25 18:00:00", FORMATTER);

        TimeRecordRequestDto request = new TimeRecordRequestDto(
                TASK_ID,
                startTime,
                endTime,
                DESCRIPTION
        );

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> timeRecordService.save(request, USERNAME));

        assertEquals("Can not save time record if task isn't done", exception.getMessage());
        verify(userService).getUserByUsername(USERNAME);
        verify(taskRepository).findById(TASK_ID);
        verify(timeRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Сохранение записи времени - startTime после endTime")
    void save_WhenStartTimeIsAfterEndTime_ShouldThrowIllegalArgumentException() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        Task task = new Task("Task name", "Task description");
        task.setId(TASK_ID);
        task.setStatus(TaskStatus.DONE);

        LocalDateTime startTime = LocalDateTime.parse("2026-04-25 18:00:00", FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse("2026-04-25 09:00:00", FORMATTER);

        TimeRecordRequestDto request = new TimeRecordRequestDto(
                TASK_ID,
                startTime,
                endTime,
                DESCRIPTION
        );

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> timeRecordService.save(request, USERNAME));

        assertEquals("Start time is after end time", exception.getMessage());
        verify(userService).getUserByUsername(USERNAME);
        verify(taskRepository).findById(TASK_ID);
        verify(timeRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Сохранение записи времени - startTime равно endTime")
    void save_WhenStartTimeEqualsEndTime_ShouldSaveTimeRecord() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        Task task = new Task("Task name", "Task description");
        task.setId(TASK_ID);
        task.setStatus(TaskStatus.DONE);

        LocalDateTime startTime = LocalDateTime.parse("2026-04-25 09:00:00", FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse("2026-04-25 09:00:00", FORMATTER);

        TimeRecordRequestDto request = new TimeRecordRequestDto(
                TASK_ID,
                startTime,
                endTime,
                DESCRIPTION
        );

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        assertThrows(IllegalArgumentException.class, () -> timeRecordService.save(request, USERNAME));
    }

    @Test
    @DisplayName("Поиск по taskId - запись найдена")
    void findByTaskId_WhenRecordExists_ShouldReturnTimeRecord() {
        TimeRecord expectedRecord = new TimeRecord(USER_ID, TASK_ID,
                LocalDateTime.parse("2026-04-25 09:00:00", FORMATTER),
                LocalDateTime.parse("2026-04-25 18:00:00", FORMATTER),
                DESCRIPTION);

        when(timeRecordRepository.findTaskId(TASK_ID)).thenReturn(Optional.of(expectedRecord));

        Optional<TimeRecord> result = timeRecordService.findByTaskId(TASK_ID);

        assertTrue(result.isPresent());
        assertEquals(expectedRecord, result.get());
        verify(timeRecordRepository).findTaskId(TASK_ID);
    }

    @Test
    @DisplayName("Поиск по taskId - запись не найдена")
    void findByTaskId_WhenRecordDoesNotExist_ShouldReturnEmpty() {
        when(timeRecordRepository.findTaskId(TASK_ID)).thenReturn(Optional.empty());

        Optional<TimeRecord> result = timeRecordService.findByTaskId(TASK_ID);

        assertFalse(result.isPresent());
        verify(timeRecordRepository).findTaskId(TASK_ID);
    }

    @Test
    @DisplayName("Получение времени работы за период - успешный расчет")
    void getTimeOfWorkForPeriod_ShouldReturnCalculatedTime() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        LocalDateTime start = LocalDateTime.parse("2026-04-20 00:00:00", FORMATTER);
        LocalDateTime end = LocalDateTime.parse("2026-04-27 23:59:59", FORMATTER);
        PeriodRequest periodRequest = new PeriodRequest(start, end);

        TimeRecord record1 = new TimeRecord(USER_ID, TASK_ID,
                LocalDateTime.parse("2026-04-21 09:00:00", FORMATTER),
                LocalDateTime.parse("2026-04-25 18:00:00", FORMATTER),
                "Work 1");

        TimeRecord record2 = new TimeRecord(USER_ID, 2L,
                LocalDateTime.parse("2026-04-22 10:00:00", FORMATTER),
                LocalDateTime.parse("2026-04-23 17:00:00", FORMATTER),
                "Work 2");

        List<TimeRecord> timeRecords = List.of(record1, record2);

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(timeRecordRepository.findAllOnPeriod(start, end, USER_ID)).thenReturn(timeRecords);

        EmployeeWorkTimeResponse response = timeRecordService.getTimeOfWorkForPeriod(periodRequest, USERNAME);

        assertNotNull(response);
        verify(userService).getUserByUsername(USERNAME);
        verify(timeRecordRepository).findAllOnPeriod(start, end, USER_ID);
    }

    @Test
    @DisplayName("Получение времени работы за период - нет записей")
    void getTimeOfWorkForPeriod_WhenNoRecords_ShouldReturnDefaultResponse() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        LocalDateTime start = LocalDateTime.parse("2026-04-20 00:00:00", FORMATTER);
        LocalDateTime end = LocalDateTime.parse("2026-04-27 23:59:59", FORMATTER);
        PeriodRequest periodRequest = new PeriodRequest(start, end);

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(timeRecordRepository.findAllOnPeriod(start, end, USER_ID)).thenReturn(List.of());

        EmployeeWorkTimeResponse response = timeRecordService.getTimeOfWorkForPeriod(periodRequest, USERNAME);

        assertEquals(0, response.hours());
        assertEquals(0, response.minutes());
        verify(userService).getUserByUsername(USERNAME);
        verify(timeRecordRepository).findAllOnPeriod(start, end, USER_ID);
    }

    @Test
    @DisplayName("Получение времени работы за период - с минутами")
    void getTimeOfWorkForPeriod_WithMinutesRemainder_ShouldReturnCorrectHoursAndMinutes() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        LocalDateTime start = LocalDateTime.parse("2026-04-20 00:00:00", FORMATTER);
        LocalDateTime end = LocalDateTime.parse("2026-04-27 23:59:59", FORMATTER);
        PeriodRequest periodRequest = new PeriodRequest(start, end);

        // Запись на 8 часов 30 минут
        TimeRecord record = new TimeRecord(USER_ID, TASK_ID,
                LocalDateTime.parse("2026-04-21 09:00:00", FORMATTER),
                LocalDateTime.parse("2026-04-21 17:30:00", FORMATTER),
                "Work");

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(timeRecordRepository.findAllOnPeriod(start, end, USER_ID)).thenReturn(List.of(record));

        EmployeeWorkTimeResponse response = timeRecordService.getTimeOfWorkForPeriod(periodRequest, USERNAME);

        assertEquals(8, response.hours());
        assertEquals(30, response.minutes());
    }

    @Test
    @DisplayName("Получение времени работы за период - несколько записей с суммированием")
    void getTimeOfWorkForPeriod_WithMultipleRecords_ShouldSumTime() {
        User user = new User(USERNAME, "password");
        user.setId(USER_ID);

        LocalDateTime start = LocalDateTime.parse("2026-04-20 00:00:00", FORMATTER);
        LocalDateTime end = LocalDateTime.parse("2026-04-27 23:59:59", FORMATTER);
        PeriodRequest periodRequest = new PeriodRequest(start, end);

        TimeRecord record1 = new TimeRecord(USER_ID, 1L,
                LocalDateTime.parse("2026-04-21 09:00:00", FORMATTER),
                LocalDateTime.parse("2026-04-21 12:00:00", FORMATTER),
                "Work 1");

        TimeRecord record2 = new TimeRecord(USER_ID, 2L,
                LocalDateTime.parse("2026-04-22 14:00:00", FORMATTER),
                LocalDateTime.parse("2026-04-22 17:30:00", FORMATTER),
                "Work 2");

        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(timeRecordRepository.findAllOnPeriod(start, end, USER_ID)).thenReturn(List.of(record1, record2));

        EmployeeWorkTimeResponse response = timeRecordService.getTimeOfWorkForPeriod(periodRequest, USERNAME);

        // 3 часа + 3.5 часа = 6.5 часов = 6 часов 30 минут
        assertEquals(6, response.hours());
        assertEquals(30, response.minutes());
    }

    @Test
    @DisplayName("Получение времени работы за период - пользователь не найден")
    void getTimeOfWorkForPeriod_WhenUserNotFound_ShouldThrowException() {
        LocalDateTime start = LocalDateTime.parse("2026-04-20 00:00:00", FORMATTER);
        LocalDateTime end = LocalDateTime.parse("2026-04-27 23:59:59", FORMATTER);
        PeriodRequest periodRequest = new PeriodRequest(start, end);

        when(userService.getUserByUsername(USERNAME)).thenThrow(new EntityNotFoundException("User not found"));

        assertThrows(EntityNotFoundException.class,
                () -> timeRecordService.getTimeOfWorkForPeriod(periodRequest, USERNAME));

        verify(userService).getUserByUsername(USERNAME);
        verify(timeRecordRepository, never()).findAllOnPeriod(any(), any(), any());
    }
}