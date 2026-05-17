package com.together.domain.list;

import com.together.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TodoListRepository extends JpaRepository<TodoList, UUID> {

    @Query("SELECT l FROM TodoList l WHERE :user MEMBER OF l.members")
    List<TodoList> findByMember(@Param("user") User user);
}
