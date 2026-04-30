package ru.cdek.tasktimetrackerapi.service.task.time_record;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cdek.tasktimetrackerapi.model.TimeRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование калькулятора рабочего времени")
class WorkTimeCalculatorTest {

    private final WorkTimeCalculator calculator = new WorkTimeCalculator();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime PERIOD_START = LocalDateTime.parse("2026-04-20 00:00:00", formatter);
    private final LocalDateTime PERIOD_END = LocalDateTime.parse("2026-04-27 23:59:59", formatter);

    @Test
    @DisplayName("Задача полностью внутри периода - возвращает полную длительность задачи")
    void calculateTotalMinutes_whenTaskFullyInsidePeriod_shouldReturnFullTaskDuration() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-21 09:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-26 18:00:00", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Task inside period");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);
        long expected = MINUTES.between(taskStart, taskEnd);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Период полностью внутри задачи - возвращает длительность периода")
    void calculateTotalMinutes_whenPeriodFullyInsideTask_shouldReturnPeriodDuration() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-10 00:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-05-10 23:59:59", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Task covers full period");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);
        long expected = MINUTES.between(PERIOD_START, PERIOD_END);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Задача началась до периода, закончилась внутри - возвращает частичную длительность")
    void calculateTotalMinutes_whenTaskStartedBeforePeriodAndEndedInside_shouldReturnPartialDuration() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-15 10:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-25 18:00:00", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Started before, ended inside");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);
        long expected = MINUTES.between(PERIOD_START, taskEnd);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Задача началась внутри периода, закончилась после - возвращает частичную длительность")
    void calculateTotalMinutes_whenTaskStartedInsidePeriodAndEndedAfter_shouldReturnPartialDuration() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-23 09:30:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-30 17:00:00", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Started inside, ended after");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);
        long expected = MINUTES.between(taskStart, PERIOD_END);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Задача точно совпадает с периодом - возвращает длительность периода")
    void calculateTotalMinutes_whenTaskExactlyMatchesPeriod_shouldReturnPeriodDuration() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-20 00:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-27 23:59:59", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Task exactly matches period");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);
        long expected = MINUTES.between(PERIOD_START, PERIOD_END);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Задача закончилась до начала периода - возвращает 0")
    void calculateTotalMinutes_whenTaskEndedBeforePeriod_shouldReturnZero() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-10 08:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-15 17:00:00", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Task ended before period");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Задача началась после окончания периода - возвращает 0")
    void calculateTotalMinutes_whenTaskStartedAfterPeriod_shouldReturnZero() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-30 09:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-05-05 18:00:00", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Task started after period");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Несколько пересекающихся задач - возвращает сумму длительностей")
    void calculateTotalMinutes_whenMultipleTasksIntersect_shouldSumAllDurations() {
        TimeRecord task1 = new TimeRecord(1L, 1L,
                LocalDateTime.parse("2026-04-18 10:00:00", formatter),
                LocalDateTime.parse("2026-04-22 18:00:00", formatter),
                "Task 1 - started before, ended inside");

        TimeRecord task2 = new TimeRecord(1L, 2L,
                LocalDateTime.parse("2026-04-24 09:00:00", formatter),
                LocalDateTime.parse("2026-04-26 17:00:00", formatter),
                "Task 2 - fully inside");

        TimeRecord task3 = new TimeRecord(1L, 3L,
                LocalDateTime.parse("2026-04-15 08:00:00", formatter),
                LocalDateTime.parse("2026-05-15 20:00:00", formatter),
                "Task 3 - period inside task");

        long result = calculator.calculateTotalMinutes(List.of(task1, task2, task3), PERIOD_START, PERIOD_END);

        long expected = MINUTES.between(PERIOD_START, task1.getEndTime()) +
                MINUTES.between(task2.getStartTime(), task2.getEndTime()) +
                MINUTES.between(PERIOD_START, PERIOD_END);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Пустой список задач - возвращает 0")
    void calculateTotalMinutes_whenNoTasks_shouldReturnZero() {
        List<TimeRecord> emptyList = Collections.emptyList();

        long result = calculator.calculateTotalMinutes(emptyList, PERIOD_START, PERIOD_END);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Задача начинается точно в момент начала периода - включает задачу")
    void calculateTotalMinutes_whenTaskStartsExactlyAtPeriodStart_shouldIncludeTask() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-20 00:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-25 12:00:00", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Task starts exactly at period start");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);
        long expected = MINUTES.between(taskStart, taskEnd);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Задача заканчивается точно в момент окончания периода - включает задачу")
    void calculateTotalMinutes_whenTaskEndsExactlyAtPeriodEnd_shouldIncludeTask() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-22 14:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-27 23:59:59", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Task ends exactly at period end");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);
        long expected = MINUTES.between(taskStart, taskEnd);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Задача длится ровно 1 час внутри периода - возвращает 60 минут")
    void calculateTotalMinutes_whenTaskLastsExactlyOneHourInsidePeriod_shouldReturn60Minutes() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-22 14:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-22 15:00:00", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "One hour task");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);

        assertEquals(60, result);
    }

    @Test
    @DisplayName("Задача с нулевой длительностью - возвращает 0")
    void calculateTotalMinutes_whenTaskHasZeroDuration_shouldReturnZero() {
        LocalDateTime taskStart = LocalDateTime.parse("2026-04-22 14:00:00", formatter);
        LocalDateTime taskEnd = LocalDateTime.parse("2026-04-22 14:00:00", formatter);
        TimeRecord record = new TimeRecord(1L, 1L, taskStart, taskEnd, "Zero duration task");

        long result = calculator.calculateTotalMinutes(List.of(record), PERIOD_START, PERIOD_END);

        assertEquals(0, result);
    }
}