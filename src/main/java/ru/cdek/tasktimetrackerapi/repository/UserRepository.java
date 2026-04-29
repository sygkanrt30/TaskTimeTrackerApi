package ru.cdek.tasktimetrackerapi.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import ru.cdek.tasktimetrackerapi.model.User;

import java.util.Optional;

@Mapper
@Repository
public interface UserRepository {

    @Select("SELECT * FROM app_user WHERE username = #{username}")
    Optional<User> findByUsername(String username);

    @Insert("INSERT INTO app_user(username, password, role) VALUES (#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);
}
