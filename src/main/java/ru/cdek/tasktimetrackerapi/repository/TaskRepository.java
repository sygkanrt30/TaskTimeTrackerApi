package ru.cdek.tasktimetrackerapi.repository;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import ru.cdek.tasktimetrackerapi.model.Task;
import ru.cdek.tasktimetrackerapi.model.TaskStatus;

import java.util.Optional;

@Mapper
@Repository
public interface TaskRepository {

    @Insert("INSERT INTO task (name, description, status) VALUES (#{name}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Task task);

    @Update("UPDATE task SET status = #{status} WHERE id = #{id} RETURNING *")
    void updateStatus(Long id, TaskStatus status);

    @Select("SELECT * FROM task WHERE id = #{id}")
    Optional<Task> findById(Long id);
}
