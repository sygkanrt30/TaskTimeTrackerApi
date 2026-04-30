package ru.cdek.tasktimetrackerapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.cdek.tasktimetrackerapi.ecxeption.EntityNotFoundException;
import ru.cdek.tasktimetrackerapi.model.TaskStatus;
import ru.cdek.tasktimetrackerapi.model.dto.request.TaskRequestDto;
import ru.cdek.tasktimetrackerapi.model.dto.request.UpdateTaskStatusRequest;
import ru.cdek.tasktimetrackerapi.model.dto.response.TaskResponseDto;
import ru.cdek.tasktimetrackerapi.service.task.TaskService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private static final String BASE_URL = "/api/v1/task";
    private static final String TASK_NAME = "Test Task";
    private static final String TASK_DESCRIPTION = "Test Description";
    private static final Long TASK_ID = 1L;

    @Test
    @DisplayName("Создание задачи - успешное создание")
    @WithMockUser
    void createTask_ShouldReturnSuccess() throws Exception {
        doNothing().when(taskService).createTask(any(TaskRequestDto.class));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "description": "%s"
                                }
                                """.formatted(TASK_NAME, TASK_DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Task created"));

        verify(taskService).createTask(any(TaskRequestDto.class));
    }

    @Test
    @DisplayName("Создание задачи - с пустым именем (должна быть ошибка валидации)")
    @WithMockUser
    void createTask_WhenNameIsBlank_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "description": "%s"
                                }
                                """.formatted(TASK_DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    @DisplayName("Создание задачи - с пустым описанием (должна быть ошибка валидации)")
    @WithMockUser
    void createTask_WhenDescriptionIsBlank_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "description": ""
                                }
                                """.formatted(TASK_NAME))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    @DisplayName("Создание задачи - с null именем (должна быть ошибка валидации)")
    @WithMockUser
    void createTask_WhenNameIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": null,
                                    "description": "%s"
                                }
                                """.formatted(TASK_DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    @DisplayName("Создание задачи - с null описанием (должна быть ошибка валидации)")
    @WithMockUser
    void createTask_WhenDescriptionIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "%s",
                                    "description": null
                                }
                                """.formatted(TASK_NAME))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    @DisplayName("Создание задачи - без тела запроса")
    @WithMockUser
    void createTask_WhenNoBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    @DisplayName("Обновление статуса задачи - успешное обновление на IN_PROGRESS")
    @WithMockUser
    void updateStatus_WhenUpdateToInProgress_ShouldReturnTaskResponse() throws Exception {
        TaskResponseDto responseDto = new TaskResponseDto(TASK_ID, TASK_NAME, TASK_DESCRIPTION, TaskStatus.IN_PROGRESS);
        when(taskService.updateTaskStatus(any(UpdateTaskStatusRequest.class))).thenReturn(responseDto);

        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "status": "IN_PROGRESS"
                                }
                                """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.name").value(TASK_NAME))
                .andExpect(jsonPath("$.description").value(TASK_DESCRIPTION))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(taskService).updateTaskStatus(any(UpdateTaskStatusRequest.class));
    }

    @Test
    @DisplayName("Обновление статуса задачи - успешное обновление на DONE")
    @WithMockUser
    void updateStatus_WhenUpdateToDone_ShouldReturnTaskResponse() throws Exception {
        TaskResponseDto responseDto = new TaskResponseDto(TASK_ID, TASK_NAME, TASK_DESCRIPTION, TaskStatus.DONE);
        when(taskService.updateTaskStatus(any(UpdateTaskStatusRequest.class))).thenReturn(responseDto);

        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "status": "DONE"
                                }
                                """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID))
                .andExpect(jsonPath("$.name").value(TASK_NAME))
                .andExpect(jsonPath("$.description").value(TASK_DESCRIPTION))
                .andExpect(jsonPath("$.status").value("DONE"));

        verify(taskService).updateTaskStatus(any(UpdateTaskStatusRequest.class));
    }

    @Test
    @DisplayName("Обновление статуса задачи - с null taskId (должна быть ошибка валидации)")
    @WithMockUser
    void updateStatus_WhenTaskIdIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": null,
                                    "status": "IN_PROGRESS"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).updateTaskStatus(any());
    }

    @Test
    @DisplayName("Обновление статуса задачи - с null статусом (должна быть ошибка валидации)")
    @WithMockUser
    void updateStatus_WhenStatusIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "status": null
                                }
                                """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).updateTaskStatus(any());
    }

    @Test
    @DisplayName("Обновление статуса задачи - с невалидным статусом")
    @WithMockUser
    void updateStatus_WhenStatusIsInvalid_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "status": "INVALID_STATUS"
                                }
                                """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).updateTaskStatus(any());
    }

    @Test
    @DisplayName("Обновление статуса задачи - попытка обновить на NEW")
    @WithMockUser
    void updateStatus_WhenUpdateToNew_ShouldReturnBadRequest() throws Exception {
        when(taskService.updateTaskStatus(any(UpdateTaskStatusRequest.class)))
                .thenThrow(new IllegalArgumentException("Can not update task status to NEW"));

        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "status": "NEW"
                                }
                                """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService).updateTaskStatus(any(UpdateTaskStatusRequest.class));
    }

    @Test
    @DisplayName("Обновление статуса задачи - задача не найдена")
    @WithMockUser
    void updateStatus_WhenTaskNotFound_ShouldReturnNotFound() throws Exception {
        when(taskService.updateTaskStatus(any(UpdateTaskStatusRequest.class)))
                .thenThrow(new EntityNotFoundException("Task with id " + TASK_ID + " not found"));

        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "status": "IN_PROGRESS"
                                }
                                """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(taskService).updateTaskStatus(any(UpdateTaskStatusRequest.class));
    }

    @Test
    @DisplayName("Обновление статуса задачи - попытка обновить на IN_PROGRESS при наличии time records")
    @WithMockUser
    void updateStatus_WhenUpdateToInProgressButTimeRecordsExist_ShouldReturnBadRequest() throws Exception {
        when(taskService.updateTaskStatus(any(UpdateTaskStatusRequest.class)))
                .thenThrow(new IllegalArgumentException("Can not update task status to IN_PROGRESS if time records exist"));

        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "status": "IN_PROGRESS"
                                }
                                """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService).updateTaskStatus(any(UpdateTaskStatusRequest.class));
    }

    @Test
    @DisplayName("Обновление статуса задачи - без тела запроса")
    @WithMockUser
    void updateStatus_WhenNoBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/update/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).updateTaskStatus(any());
    }
}