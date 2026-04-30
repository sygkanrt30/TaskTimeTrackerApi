package ru.cdek.tasktimetrackerapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.cdek.tasktimetrackerapi.model.dto.request.TaskRequestDto;
import ru.cdek.tasktimetrackerapi.model.dto.request.UpdateTaskStatusRequest;
import ru.cdek.tasktimetrackerapi.model.dto.response.TaskResponseDto;
import ru.cdek.tasktimetrackerapi.service.task.TaskService;

@RestController
@RequestMapping("${server.base-url.task}")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<String> createTask(@Valid @RequestBody TaskRequestDto taskDto) {
        taskService.createTask(taskDto);
        return ResponseEntity.ok("Task created");
    }

    @PatchMapping("/update/status")
    public ResponseEntity<TaskResponseDto> updateStatus(@Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(taskService.updateTaskStatus(request));
    }
}