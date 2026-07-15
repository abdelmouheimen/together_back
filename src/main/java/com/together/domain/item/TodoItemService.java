package com.together.domain.item;

import com.together.domain.activity.ActivityEvent;
import com.together.domain.activity.ActivityEventRepository;
import com.together.domain.activity.ActivityType;
import com.together.domain.comment.Comment;
import com.together.domain.comment.CommentRepository;
import com.together.domain.list.TodoList;
import com.together.domain.list.TodoListService;
import com.together.domain.user.User;
import com.together.dto.comment.CommentDto;
import com.together.dto.item.CreateItemRequest;
import com.together.dto.item.ReorderItemRequest;
import com.together.dto.item.TodoItemDto;
import com.together.dto.item.ToggleItemRequest;
import com.together.dto.item.UpdateItemRequest;
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
public class TodoItemService {

    private final TodoItemRepository todoItemRepository;
    private final TodoListService todoListService;
    private final CommentRepository commentRepository;
    private final ActivityEventRepository activityEventRepository;

    public TodoItemService(TodoItemRepository todoItemRepository,
                           TodoListService todoListService,
                           CommentRepository commentRepository,
                           ActivityEventRepository activityEventRepository) {
        this.todoItemRepository = todoItemRepository;
        this.todoListService = todoListService;
        this.commentRepository = commentRepository;
        this.activityEventRepository = activityEventRepository;
    }

    @Transactional(readOnly = true)
    public List<TodoItemDto> getItems(User currentUser, UUID listId) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        return todoItemRepository.findByListOrderByPosition(list).stream()
                .map(this::toDto)
                .toList();
    }

    public TodoItemDto create(User currentUser, UUID listId, CreateItemRequest request) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);

        int maxPosition = todoItemRepository.findByListOrderByPosition(list).stream()
                .mapToInt(TodoItem::getPosition)
                .max()
                .orElse(-1);

        TodoItem item = new TodoItem();
        item.setList(list);
        item.setText(request.text());
        item.setCategory(request.category() != null ? request.category() : ItemCategory.OTHER);
        item.setPosition(maxPosition + 1);
        item.setCreatedBy(currentUser);
        item.setCreatedAt(Instant.now());
        todoItemRepository.save(item);

        ActivityEvent event = new ActivityEvent();
        event.setType(ActivityType.ITEM_ADDED);
        event.setActor(currentUser);
        event.setList(list);
        event.setItem(item);
        event.setExtraText(item.getText());
        event.setCreatedAt(Instant.now());
        activityEventRepository.save(event);

        return toDto(item);
    }

    public TodoItemDto update(User currentUser, UUID listId, UUID itemId, UpdateItemRequest request) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        TodoItem item = findAndCheckList(itemId, list);
        item.setText(request.text());
        todoItemRepository.save(item);
        return toDto(item);
    }

    public TodoItemDto toggle(User currentUser, UUID listId, UUID itemId, ToggleItemRequest request) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        TodoItem item = findAndCheckList(itemId, list);

        item.setDone(request.done());
        if (request.done()) {
            item.setCheckedBy(currentUser);
            item.setCheckedAt(Instant.now());
        } else {
            item.setCheckedBy(null);
            item.setCheckedAt(null);
        }
        todoItemRepository.save(item);

        ActivityEvent event = new ActivityEvent();
        event.setType(ActivityType.ITEM_CHECKED);
        event.setActor(currentUser);
        event.setList(list);
        event.setItem(item);
        event.setCreatedAt(Instant.now());
        activityEventRepository.save(event);

        return toDto(item);
    }

    public void delete(User currentUser, UUID listId, UUID itemId) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        TodoItem item = findAndCheckList(itemId, list);
        todoItemRepository.delete(item);
    }

    public void reorder(User currentUser, UUID listId, List<ReorderItemRequest> requests) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        for (ReorderItemRequest req : requests) {
            TodoItem item = findAndCheckList(req.id(), list);
            item.setPosition(req.position());
            todoItemRepository.save(item);
        }
    }

    private TodoItem findAndCheckList(UUID itemId, TodoList list) {
        TodoItem item = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
        if (!item.getList().getId().equals(list.getId())) {
            throw new UnauthorizedException("Item does not belong to this list");
        }
        return item;
    }

    TodoItemDto toDto(TodoItem item) {
        List<Comment> comments = commentRepository.findByItemOrderByCreatedAtAsc(item);
        List<CommentDto> commentDtos = comments.stream()
                .map(c -> new CommentDto(c.getId(), UserMapper.toDto(c.getAuthor()), c.getText(), c.getCreatedAt()))
                .toList();
        return new TodoItemDto(
                item.getId(),
                item.getText(),
                item.isDone(),
                item.getCategory(),
                item.getCheckedBy() != null ? UserMapper.toDto(item.getCheckedBy()) : null,
                item.getCheckedAt(),
                item.getPosition(),
                commentDtos,
                item.getCreatedAt()
        );
    }
}
