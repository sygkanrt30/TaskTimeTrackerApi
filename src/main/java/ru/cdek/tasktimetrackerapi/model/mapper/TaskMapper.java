package ru.cdek.tasktimetrackerapi.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.cdek.tasktimetrackerapi.model.Task;
import ru.cdek.tasktimetrackerapi.model.dto.response.TaskResponseDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    TaskResponseDto toDto(Task task);
}
