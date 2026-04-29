package ru.cdek.tasktimetrackerapi.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

@RequiredArgsConstructor
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TimeRecordService TimeRecordService;
    private final TaskMapper taskMapper;

    @Override
    public void createTask(TaskRequestDto taskDto) {
        Task task = new Task(taskDto.name(), taskDto.description());
        taskRepository.save(task);
        log.info("Created task {}", task);
    }

    @Override
    public TaskResponseDto updateTaskStatus(UpdateTaskStatusRequest request) {
        TaskStatus newStatus = request.status();
        if (newStatus.equals(TaskStatus.NEW)) {
            throw new IllegalArgumentException("Can not update task status to NEW");
        }
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new EntityNotFoundException("Task with id " + request.taskId() + " not found"));

        Optional<TimeRecord> timeRecords = TimeRecordService.findByTaskId(task.getId());
        if (timeRecords.isPresent() && newStatus.equals(TaskStatus.IN_PROGRESS)) {
            throw new IllegalArgumentException("Can not update task status to IN_PROGRESS if time records exist");
        }
        task.setStatus(newStatus);
        taskRepository.updateStatus(task.getId(), newStatus);

        log.info("Updated task {}", task);
        return taskMapper.toDto(task);
    }
}
