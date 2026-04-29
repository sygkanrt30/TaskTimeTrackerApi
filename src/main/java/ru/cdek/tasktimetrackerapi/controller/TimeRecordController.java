package ru.cdek.tasktimetrackerapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.cdek.tasktimetrackerapi.model.dto.request.PeriodRequest;
import ru.cdek.tasktimetrackerapi.model.dto.request.TimeRecordRequestDto;
import ru.cdek.tasktimetrackerapi.model.dto.response.EmployeeWorkTimeResponse;
import ru.cdek.tasktimetrackerapi.service.task.time_record.TimeRecordService;

@RestController
@RequestMapping("${server.base-url.task}/time_record")
@RequiredArgsConstructor
@Validated
public class TimeRecordController {

    private final TimeRecordService timeRecordService;

    @PostMapping
    public ResponseEntity<String> createTimeRecord(@RequestBody @Valid TimeRecordRequestDto timeRecordRequestDto,
                                                   @AuthenticationPrincipal UserDetails user) {
        timeRecordService.save(timeRecordRequestDto, user.getUsername());
        return ResponseEntity.ok("Time record created");
    }

    @GetMapping("/time_of_work/for_period")
    public ResponseEntity<EmployeeWorkTimeResponse> getTimeOfWorkForPeriod(@AuthenticationPrincipal UserDetails user,
                                                                           @RequestBody @Valid PeriodRequest periodRequest) {
        return ResponseEntity.ok(timeRecordService.getTimeOfWorkForPeriod(periodRequest, user.getUsername()));
    }
}