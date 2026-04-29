package ru.cdek.tasktimetrackerapi.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import ru.cdek.tasktimetrackerapi.model.TimeRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
@Repository
public interface TimeRecordRepository {

    @Insert("INSERT INTO time_record (user_id, task_id, start_time, end_time, description_of_work) " +
            "VALUES (#{userId}, #{taskId}, #{startTime}, #{endTime}, #{descriptionOfWork})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(TimeRecord timeRecord);

    @Select("SELECT * FROM time_record WHERE task_id = #{taskId}")
    Optional<TimeRecord> findTaskId(Long taskId);

    @Select("SELECT * FROM time_record WHERE user_id = #{userId} " +
            "AND NOT (end_time < #{start} OR start_time > #{end})")
    List<TimeRecord> findAllOnPeriod(LocalDateTime start, LocalDateTime end, Long userId);
}
