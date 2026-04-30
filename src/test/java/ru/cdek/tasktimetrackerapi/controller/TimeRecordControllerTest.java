package ru.cdek.tasktimetrackerapi.controller;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.cdek.tasktimetrackerapi.config.JacksonConfig;
import ru.cdek.tasktimetrackerapi.model.dto.request.PeriodRequest;
import ru.cdek.tasktimetrackerapi.model.dto.request.TimeRecordRequestDto;
import ru.cdek.tasktimetrackerapi.model.dto.response.EmployeeWorkTimeResponse;
import ru.cdek.tasktimetrackerapi.service.task.time_record.TimeRecordService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimeRecordController.class)
@Import(JacksonConfig.class)
@DisplayName("Тестирование контроллера записей времени")
class TimeRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeRecordService timeRecordService;

    private static final String BASE_URL = "/api/v1/task/time_record";
    private static final String USERNAME = "testuser";
    private static final Long TASK_ID = 1L;
    private static final String DESCRIPTION = "Work description";

    @Test
    @DisplayName("Создание записи времени - успешное создание")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_ShouldReturnSuccess() throws Exception {
        doNothing().when(timeRecordService).save(any(TimeRecordRequestDto.class), eq(USERNAME));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "startTime": "2026-04-25 09:00:00",
                                    "endTime": "2026-04-25 18:00:00",
                                    "descriptionOfWork": "%s"
                                }
                                """.formatted(TASK_ID, DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Time record created"));

        verify(timeRecordService).save(any(TimeRecordRequestDto.class), eq(USERNAME));
    }

    @Test
    @DisplayName("Создание записи времени - с null taskId (ошибка валидации)")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenTaskIdIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": null,
                                    "startTime": "2026-04-25 09:00:00",
                                    "endTime": "2026-04-25 18:00:00",
                                    "descriptionOfWork": "%s"
                                }
                                """.formatted(DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).save(any(), any());
    }

    @Test
    @DisplayName("Создание записи времени - с null startTime (ошибка валидации)")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenStartTimeIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "startTime": null,
                                    "endTime": "2026-04-25 18:00:00",
                                    "descriptionOfWork": "%s"
                                }
                                """.formatted(TASK_ID, DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).save(any(), any());
    }

    @Test
    @DisplayName("Создание записи времени - с null endTime (ошибка валидации)")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenEndTimeIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "startTime": "2026-04-25 09:00:00",
                                    "endTime": null,
                                    "descriptionOfWork": "%s"
                                }
                                """.formatted(TASK_ID, DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).save(any(), any());
    }

    @Test
    @DisplayName("Создание записи времени - с null description (ошибка валидации)")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenDescriptionIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "startTime": "2026-04-25 09:00:00",
                                    "endTime": "2026-04-25 18:00:00",
                                    "descriptionOfWork": null
                                }
                                """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).save(any(), any());
    }

    @Test
    @DisplayName("Создание записи времени - с пустым description (@NotBlank не допускает пустую строку)")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenDescriptionIsEmpty_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "taskId": %d,
                                "startTime": "2026-04-25T09:00:00",
                                "endTime": "2026-04-25T18:00:00",
                                "descriptionOfWork": ""
                            }
                            """.formatted(TASK_ID))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).save(any(), any());
    }

    @Test
    @DisplayName("Создание записи времени - все поля null (ошибка валидации)")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenAllFieldsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": null,
                                    "startTime": null,
                                    "endTime": null,
                                    "descriptionOfWork": null
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).save(any(), any());
    }

    @Test
    @DisplayName("Создание записи времени - отсутствует taskId в запросе")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenTaskIdMissing_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "startTime": "2026-04-25 09:00:00",
                                    "endTime": "2026-04-25 18:00:00",
                                    "descriptionOfWork": "%s"
                                }
                                """.formatted(DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).save(any(), any());
    }

    @Test
    @DisplayName("Создание записи времени - при ошибке сервиса")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Task not found")).when(timeRecordService)
                .save(any(TimeRecordRequestDto.class), eq(USERNAME));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "startTime": "2026-04-25 09:00:00",
                                    "endTime": "2026-04-25 18:00:00",
                                    "descriptionOfWork": "%s"
                                }
                                """.formatted(TASK_ID, DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService).save(any(TimeRecordRequestDto.class), eq(USERNAME));
    }

    @Test
    @DisplayName("Создание записи времени - без тела запроса")
    @WithMockUser(username = USERNAME)
    void createTimeRecord_WhenNoBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).save(any(), any());
    }

    @Test
    @DisplayName("Получение времени работы за период - успешный запрос")
    @WithMockUser(username = USERNAME)
    void getTimeOfWorkForPeriod_ShouldReturnEmployeeWorkTimeResponse() throws Exception {
        EmployeeWorkTimeResponse response = EmployeeWorkTimeResponse.of(8, 30);
        when(timeRecordService.getTimeOfWorkForPeriod(any(PeriodRequest.class), eq(USERNAME)))
                .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "start": "2026-04-20 00:00:00",
                                    "end": "2026-04-27 23:59:59"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours").value(8))
                .andExpect(jsonPath("$.minutes").value(30));

        verify(timeRecordService).getTimeOfWorkForPeriod(any(PeriodRequest.class), eq(USERNAME));
    }

    @Test
    @DisplayName("Получение времени работы за период - нулевой результат")
    @WithMockUser(username = USERNAME)
    void getTimeOfWorkForPeriod_WhenNoWorkTime_ShouldReturnDefaultResponse() throws Exception {
        EmployeeWorkTimeResponse response = EmployeeWorkTimeResponse.defaultResponse();
        when(timeRecordService.getTimeOfWorkForPeriod(any(PeriodRequest.class), eq(USERNAME)))
                .thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "start": "2026-04-20 00:00:00",
                                    "end": "2026-04-27 23:59:59"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hours").value(0))
                .andExpect(jsonPath("$.minutes").value(0));

        verify(timeRecordService).getTimeOfWorkForPeriod(any(PeriodRequest.class), eq(USERNAME));
    }

    @Test
    @DisplayName("Получение времени работы за период - с null start (ошибка валидации)")
    @WithMockUser(username = USERNAME)
    void getTimeOfWorkForPeriod_WhenStartIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "start": null,
                                    "end": "2026-04-27 23:59:59"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).getTimeOfWorkForPeriod(any(), any());
    }

    @Test
    @DisplayName("Получение времени работы за период - с null end (ошибка валидации)")
    @WithMockUser(username = USERNAME)
    void getTimeOfWorkForPeriod_WhenEndIsNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "start": "2026-04-20 00:00:00",
                                    "end": null
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).getTimeOfWorkForPeriod(any(), any());
    }

    @Test
    @DisplayName("Получение времени работы за период - start после end (ошибка валидации в конструкторе)")
    @WithMockUser(username = USERNAME)
    void getTimeOfWorkForPeriod_WhenStartIsAfterEnd_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "start": "2026-04-27 23:59:59",
                                    "end": "2026-04-20 00:00:00"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).getTimeOfWorkForPeriod(any(), any());
    }

    @Test
    @DisplayName("Получение времени работы за период - оба поля null")
    @WithMockUser(username = USERNAME)
    void getTimeOfWorkForPeriod_WhenBothStartAndEndAreNull_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "start": null,
                                    "end": null
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).getTimeOfWorkForPeriod(any(), any());
    }

    @Test
    @DisplayName("Получение времени работы за период - пустой запрос")
    @WithMockUser(username = USERNAME)
    void getTimeOfWorkForPeriod_WhenEmptyBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(timeRecordService, never()).getTimeOfWorkForPeriod(any(), any());
    }

    @Test
    @DisplayName("Получение времени работы за период - при ошибке сервиса")
    @WithMockUser(username = USERNAME)
    void getTimeOfWorkForPeriod_WhenServiceThrowsException_ShouldReturnBadRequest() throws Exception {
        when(timeRecordService.getTimeOfWorkForPeriod(any(PeriodRequest.class), eq(USERNAME)))
                .thenThrow(new IllegalArgumentException("Invalid period"));

        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "start": "2026-04-20 00:00:00",
                                    "end": "2026-04-27 23:59:59"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение времени работы за период - без аутентификации")
    void getTimeOfWorkForPeriod_WhenUnauthorized_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/time_of_work/for_period")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "start": "2026-04-20 00:00:00",
                                    "end": "2026-04-27 23:59:59"
                                }
                                """)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(timeRecordService, never()).getTimeOfWorkForPeriod(any(), any());
    }

    @Test
    @DisplayName("Создание записи времени - без аутентификации")
    void createTimeRecord_WhenUnauthorized_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskId": %d,
                                    "startTime": "2026-04-25 09:00:00",
                                    "endTime": "2026-04-25 18:00:00",
                                    "descriptionOfWork": "%s"
                                }
                                """.formatted(TASK_ID, DESCRIPTION))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(timeRecordService, never()).save(any(), any());
    }
}