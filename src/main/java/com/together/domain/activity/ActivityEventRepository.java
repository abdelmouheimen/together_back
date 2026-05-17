package com.together.domain.activity;

import com.together.domain.list.TodoList;
import com.together.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ActivityEventRepository extends JpaRepository<ActivityEvent, UUID> {

    @Query("SELECT a FROM ActivityEvent a WHERE a.list IN (SELECT l FROM TodoList l WHERE :user MEMBER OF l.members) ORDER BY a.createdAt DESC")
    Page<ActivityEvent> findForUser(@Param("user") User user, Pageable pageable);

    Page<ActivityEvent> findByListOrderByCreatedAtDesc(TodoList list, Pageable pageable);
}
