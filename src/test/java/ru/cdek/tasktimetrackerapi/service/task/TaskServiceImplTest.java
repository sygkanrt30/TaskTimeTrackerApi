package ru.cdek.tasktimetrackerapi.service.task;

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
import ru.cdek.tasktimetrackerapi.model.dto.request.TaskRequestDto;
import ru.cdek.tasktimetrackerapi.model.dto.request.UpdateTaskStatusRequest;
import ru.cdek.tasktimetrackerapi.model.dto.response.TaskResponseDto;
import ru.cdek.tasktimetrackerapi.model.mapper.TaskMapper;
import ru.cdek.tasktimetrackerapi.repository.TaskRepository;
import ru.cdek.tasktimetrackerapi.service.task.time_record.TimeRecordService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование сервиса задач")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TimeRecordService timeRecordService;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private static final Long TASK_ID = 1L;
    private static final String TASK_NAME = "Test Task";
    private static final String TASK_DESCRIPTION = "Test Description";

    @Test
    @DisplayName("Создание задачи - успешное создание")
    void createTask_whenValidData_shouldSaveTask() {
        TaskRequestDto requestDto = new TaskRequestDto(TASK_NAME, TASK_DESCRIPTION);
        doNothing().when(taskRepository).save(any(Task.class));

        assertDoesNotThrow(() -> taskService.createTask(requestDto));

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Создание задачи - при сохранении возникает исключение")
    void createTask_whenRepositoryThrowsException_shouldThrowException() {
        TaskRequestDto requestDto = new TaskRequestDto(TASK_NAME, TASK_DESCRIPTION);
        doThrow(new RuntimeException("Database error")).when(taskRepository).save(any(Task.class));

        assertThrows(RuntimeException.class, () -> taskService.createTask(requestDto));

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("Обновление статуса задачи - успешное обновление на IN_PROGRESS")
    void updateTaskStatus_whenUpdateToInProgressAndNoTimeRecords_shouldUpdateStatus() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TASK_ID, TaskStatus.IN_PROGRESS);
        Task existingTask = new Task(TASK_NAME, TASK_DESCRIPTION);
        existingTask.setId(TASK_ID);
        existingTask.setStatus(TaskStatus.NEW);

        TaskResponseDto expectedResponse = new TaskResponseDto(TASK_ID, TASK_NAME, TASK_DESCRIPTION, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(timeRecordService.findByTaskId(TASK_ID)).thenReturn(Optional.empty());
        doNothing().when(taskRepository).updateStatus(TASK_ID, TaskStatus.IN_PROGRESS);
        when(taskMapper.toDto(existingTask)).thenReturn(expectedResponse);

        TaskResponseDto result = taskService.updateTaskStatus(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(TaskStatus.IN_PROGRESS, existingTask.getStatus());
        verify(taskRepository).findById(TASK_ID);
        verify(timeRecordService).findByTaskId(TASK_ID);
        verify(taskRepository).updateStatus(TASK_ID, TaskStatus.IN_PROGRESS);
        verify(taskMapper).toDto(existingTask);
    }

    @Test
    @DisplayName("Обновление статуса задачи - успешное обновление на DONE")
    void updateTaskStatus_whenUpdateToDONE_shouldUpdateStatus() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TASK_ID, TaskStatus.DONE);
        Task existingTask = new Task(TASK_NAME, TASK_DESCRIPTION);
        existingTask.setId(TASK_ID);
        existingTask.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponseDto expectedResponse = new TaskResponseDto(TASK_ID, TASK_NAME, TASK_DESCRIPTION, TaskStatus.DONE);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        doNothing().when(taskRepository).updateStatus(TASK_ID, TaskStatus.DONE);
        when(taskMapper.toDto(existingTask)).thenReturn(expectedResponse);

        TaskResponseDto result = taskService.updateTaskStatus(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(TaskStatus.DONE, existingTask.getStatus());
        verify(taskRepository).findById(TASK_ID);
        verify(taskRepository).updateStatus(TASK_ID, TaskStatus.DONE);
        verify(taskMapper).toDto(existingTask);
    }

    @Test
    @DisplayName("Обновление статуса задачи - попытка обновить на NEW")
    void updateTaskStatus_whenUpdateToNew_shouldThrowIllegalArgumentException() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TASK_ID, TaskStatus.NEW);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTaskStatus(request));

        assertEquals("Can not update task status to NEW", exception.getMessage());
        verify(taskRepository, never()).findById(anyLong());
        verify(timeRecordService, never()).findByTaskId(anyLong());
        verify(taskRepository, never()).updateStatus(anyLong(), any());
    }

    @Test
    @DisplayName("Обновление статуса задачи - задача не найдена")
    void updateTaskStatus_whenTaskNotFound_shouldThrowEntityNotFoundException() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TASK_ID, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> taskService.updateTaskStatus(request));

        assertEquals("Task with id " + TASK_ID + " not found", exception.getMessage());
        verify(taskRepository).findById(TASK_ID);
        verify(timeRecordService, never()).findByTaskId(anyLong());
        verify(taskRepository, never()).updateStatus(anyLong(), any());
    }

    @Test
    @DisplayName("Обновление статуса задачи - попытка обновить на IN_PROGRESS при наличии time records")
    void updateTaskStatus_whenUpdateToInProgressButTimeRecordsExist_shouldThrowIllegalArgumentException() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TASK_ID, TaskStatus.IN_PROGRESS);
        Task existingTask = new Task(TASK_NAME, TASK_DESCRIPTION);
        existingTask.setId(TASK_ID);
        existingTask.setStatus(TaskStatus.NEW);

        TimeRecord existingTimeRecord = new TimeRecord();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask));
        when(timeRecordService.findByTaskId(TASK_ID)).thenReturn(Optional.of(existingTimeRecord));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTaskStatus(request));

        assertEquals("Can not update task status to IN_PROGRESS if time records exist", exception.getMessage());
        verify(taskRepository).findById(TASK_ID);
        verify(timeRecordService).findByTaskId(TASK_ID);
        verify(taskRepository, never()).updateStatus(anyLong(), any());
    }

    @Test
    @DisplayName("Обновление статуса задачи - многократное обновление")
    void updateTaskStatus_whenCalledMultipleTimes_shouldUpdateStatusEachTime() {
        Long taskId2 = 2L;

        UpdateTaskStatusRequest request1 = new UpdateTaskStatusRequest(TASK_ID, TaskStatus.IN_PROGRESS);
        UpdateTaskStatusRequest request2 = new UpdateTaskStatusRequest(taskId2, TaskStatus.DONE);

        Task task1 = new Task(TASK_NAME, TASK_DESCRIPTION);
        task1.setId(TASK_ID);
        task1.setStatus(TaskStatus.NEW);

        Task task2 = new Task("Another Task", "Another Description");
        task2.setId(taskId2);
        task2.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponseDto expectedResponse1 = new TaskResponseDto(TASK_ID, TASK_NAME, TASK_DESCRIPTION, TaskStatus.IN_PROGRESS);
        TaskResponseDto expectedResponse2 = new TaskResponseDto(taskId2, "Another Task", "Another Description", TaskStatus.DONE);

        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task1));
        when(taskRepository.findById(taskId2)).thenReturn(Optional.of(task2));
        when(timeRecordService.findByTaskId(TASK_ID)).thenReturn(Optional.empty());
        doNothing().when(taskRepository).updateStatus(TASK_ID, TaskStatus.IN_PROGRESS);
        doNothing().when(taskRepository).updateStatus(taskId2, TaskStatus.DONE);
        when(taskMapper.toDto(task1)).thenReturn(expectedResponse1);
        when(taskMapper.toDto(task2)).thenReturn(expectedResponse2);

        TaskResponseDto result1 = taskService.updateTaskStatus(request1);
        TaskResponseDto result2 = taskService.updateTaskStatus(request2);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(expectedResponse1, result1);
        assertEquals(expectedResponse2, result2);

        verify(taskRepository, times(2)).findById(anyLong());
        verify(timeRecordService, times(1)).findByTaskId(TASK_ID);
        verify(taskRepository, times(1)).updateStatus(TASK_ID, TaskStatus.IN_PROGRESS);
        verify(taskRepository, times(1)).updateStatus(taskId2, TaskStatus.DONE);
    }

    @Test
    @DisplayName("Обновление статуса задачи - с null в запросе")
    void updateTaskStatus_whenRequestHasNullFields_shouldThrowException() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(null, null);

        assertThrows(Exception.class, () -> taskService.updateTaskStatus(request));

        verify(taskRepository, never()).findById(anyLong());
        verify(timeRecordService, never()).findByTaskId(anyLong());
        verify(taskRepository, never()).updateStatus(anyLong(), any());
    }
}