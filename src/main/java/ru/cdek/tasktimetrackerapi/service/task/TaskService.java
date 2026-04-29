package ru.cdek.tasktimetrackerapi.service.task;

import ru.cdek.tasktimetrackerapi.model.dto.request.TaskRequestDto;
import ru.cdek.tasktimetrackerapi.model.dto.request.UpdateTaskStatusRequest;
import ru.cdek.tasktimetrackerapi.model.dto.response.TaskResponseDto;

public interface TaskService {

    void createTask(TaskRequestDto taskDto);

    TaskResponseDto updateTaskStatus(UpdateTaskStatusRequest request);
}
