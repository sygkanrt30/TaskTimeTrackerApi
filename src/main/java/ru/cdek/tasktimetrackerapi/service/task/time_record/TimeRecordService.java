package ru.cdek.tasktimetrackerapi.service.task.time_record;

import ru.cdek.tasktimetrackerapi.model.TimeRecord;
import ru.cdek.tasktimetrackerapi.model.dto.request.PeriodRequest;
import ru.cdek.tasktimetrackerapi.model.dto.request.TimeRecordRequestDto;
import ru.cdek.tasktimetrackerapi.model.dto.response.EmployeeWorkTimeResponse;

import java.util.Optional;

public interface TimeRecordService {

    void save(TimeRecordRequestDto request, String username);

    Optional<TimeRecord> findByTaskId(Long taskId);

    EmployeeWorkTimeResponse getTimeOfWorkForPeriod(PeriodRequest periodRequest, String username);
}
