package com.together.domain.item;

import com.together.domain.list.TodoList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TodoItemRepository extends JpaRepository<TodoItem, UUID> {

    List<TodoItem> findByListOrderByPosition(TodoList list);

    int countByList(TodoList list);

    int countByListAndDoneTrue(TodoList list);
}
