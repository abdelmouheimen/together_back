package com.together.domain.comment;

import com.together.domain.item.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByItemOrderByCreatedAtAsc(TodoItem item);
}
