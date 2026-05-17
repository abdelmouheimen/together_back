package com.together.domain.comment;

import com.together.domain.activity.ActivityEvent;
import com.together.domain.activity.ActivityEventRepository;
import com.together.domain.activity.ActivityType;
import com.together.domain.item.TodoItem;
import com.together.domain.item.TodoItemRepository;
import com.together.domain.list.TodoList;
import com.together.domain.list.TodoListService;
import com.together.domain.user.User;
import com.together.dto.comment.CommentDto;
import com.together.dto.comment.CreateCommentRequest;
import com.together.exception.ResourceNotFoundException;
import com.together.exception.UnauthorizedException;
import com.together.util.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TodoItemRepository todoItemRepository;
    private final TodoListService todoListService;
    private final ActivityEventRepository activityEventRepository;

    public CommentService(CommentRepository commentRepository,
                          TodoItemRepository todoItemRepository,
                          TodoListService todoListService,
                          ActivityEventRepository activityEventRepository) {
        this.commentRepository = commentRepository;
        this.todoItemRepository = todoItemRepository;
        this.todoListService = todoListService;
        this.activityEventRepository = activityEventRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(User currentUser, UUID listId, UUID itemId) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        TodoItem item = findItemInList(itemId, list);
        return commentRepository.findByItemOrderByCreatedAtAsc(item).stream()
                .map(this::toDto)
                .toList();
    }

    public CommentDto create(User currentUser, UUID listId, UUID itemId, CreateCommentRequest request) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        TodoItem item = findItemInList(itemId, list);

        Comment comment = new Comment();
        comment.setItem(item);
        comment.setAuthor(currentUser);
        comment.setText(request.text());
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);

        ActivityEvent event = new ActivityEvent();
        event.setType(ActivityType.COMMENT_ADDED);
        event.setActor(currentUser);
        event.setList(list);
        event.setItem(item);
        event.setExtraText(request.text());
        event.setCreatedAt(Instant.now());
        activityEventRepository.save(event);

        return toDto(comment);
    }

    public void delete(User currentUser, UUID listId, UUID itemId, UUID commentId) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        findItemInList(itemId, list);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the author can delete this comment");
        }
        commentRepository.delete(comment);
    }

    private TodoItem findItemInList(UUID itemId, TodoList list) {
        TodoItem item = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        if (!item.getList().getId().equals(list.getId())) {
            throw new UnauthorizedException("Item does not belong to this list");
        }
        return item;
    }

    private CommentDto toDto(Comment c) {
        return new CommentDto(c.getId(), UserMapper.toDto(c.getAuthor()), c.getText(), c.getCreatedAt());
    }
}
