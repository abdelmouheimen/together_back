package com.together.domain.list;

import com.together.domain.activity.ActivityEvent;
import com.together.domain.activity.ActivityEventRepository;
import com.together.domain.activity.ActivityType;
import com.together.domain.item.TodoItemRepository;
import com.together.domain.user.User;
import com.together.domain.user.UserRepository;
import com.together.dto.list.AddMemberRequest;
import com.together.dto.list.CreateListRequest;
import com.together.dto.list.TodoListDto;
import com.together.dto.list.UpdateListRequest;
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
public class TodoListService {

    private final TodoListRepository todoListRepository;
    private final UserRepository userRepository;
    private final TodoItemRepository todoItemRepository;
    private final ActivityEventRepository activityEventRepository;

    public TodoListService(TodoListRepository todoListRepository,
                           UserRepository userRepository,
                           TodoItemRepository todoItemRepository,
                           ActivityEventRepository activityEventRepository) {
        this.todoListRepository = todoListRepository;
        this.userRepository = userRepository;
        this.todoItemRepository = todoItemRepository;
        this.activityEventRepository = activityEventRepository;
    }

    @Transactional(readOnly = true)
    public List<TodoListDto> getMyLists(User currentUser) {
        return todoListRepository.findByMember(currentUser).stream()
                .map(l -> toDto(l))
                .toList();
    }

    public TodoListDto create(User currentUser, CreateListRequest request) {
        TodoList list = new TodoList();
        list.setName(request.name());
        list.setEmoji(request.emoji());
        list.setAccentColor(request.accentColor());
        list.setProgressColor(request.progressColor());
        list.setCreatedBy(currentUser);
        list.setCreatedAt(Instant.now());

        if (request.parentId() != null) {
            TodoList parent = findAndCheckMembership(currentUser, request.parentId());
            list.setParent(parent);
            parent.getMembers().forEach(m -> addMemberIfAbsent(list, m));
        }

        addMemberIfAbsent(list, currentUser);
        var memberIds = request.memberIds() != null ? request.memberIds() : java.util.List.<UUID>of();
        for (UUID memberId : memberIds) {
            User member = userRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + memberId));
            addMemberIfAbsent(list, member);
        }

        todoListRepository.save(list);

        ActivityEvent event = new ActivityEvent();
        event.setType(ActivityType.LIST_CREATED);
        event.setActor(currentUser);
        event.setList(list);
        event.setCreatedAt(Instant.now());
        activityEventRepository.save(event);

        return toDto(list);
    }

    @Transactional(readOnly = true)
    public TodoListDto getById(User currentUser, UUID listId) {
        TodoList list = findAndCheckMembership(currentUser, listId);
        return toDto(list);
    }

    public TodoListDto update(User currentUser, UUID listId, UpdateListRequest request) {
        TodoList list = findAndCheckMembership(currentUser, listId);

        list.setName(request.name());
        list.setEmoji(request.emoji());
        list.setAccentColor(request.accentColor());
        list.setProgressColor(request.progressColor());
        list.setUpdatedAt(Instant.now());
        todoListRepository.save(list);

        return toDto(list);
    }

    public void delete(User currentUser, UUID listId) {
        TodoList list = findById(listId);
        if (!list.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the creator can delete this list");
        }
        todoListRepository.delete(list);
    }

    public TodoListDto addMember(User currentUser, UUID listId, AddMemberRequest request) {
        TodoList list = findAndCheckMembership(currentUser, listId);
        User newMember = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));
        addMemberIfAbsent(list, newMember);
        list.setUpdatedAt(Instant.now());
        todoListRepository.save(list);
        return toDto(list);
    }

    public void removeMember(User currentUser, UUID listId, UUID userId) {
        TodoList list = findById(listId);
        if (!list.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the creator can remove members");
        }
        list.getMembers().removeIf(m -> m.getId().equals(userId));
        list.setUpdatedAt(Instant.now());
        todoListRepository.save(list);
    }

    public TodoList findAndCheckMembership(User currentUser, UUID listId) {
        TodoList list = findById(listId);
        boolean isMember = list.getMembers().stream()
                .anyMatch(m -> m.getId().equals(currentUser.getId()));
        if (!isMember) {
            throw new UnauthorizedException("Not a member of this list");
        }
        return list;
    }

    // User has no equals/hashCode override, so Set<User> can hold two instances
    // of the same row — dedupe by id to avoid duplicate list_members inserts.
    private void addMemberIfAbsent(TodoList list, User user) {
        boolean present = list.getMembers().stream()
                .anyMatch(m -> m.getId().equals(user.getId()));
        if (!present) {
            list.getMembers().add(user);
        }
    }

    private TodoList findById(UUID id) {
        return todoListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("List not found: " + id));
    }

    private TodoListDto toDto(TodoList list) {
        int total = todoItemRepository.countByList(list);
        int done = todoItemRepository.countByListAndDoneTrue(list);
        double progress = total == 0 ? 0.0 : (double) done / total;

        return new TodoListDto(
                list.getId(),
                list.getName(),
                list.getEmoji(),
                list.getAccentColor(),
                list.getProgressColor(),
                list.getMembers().stream().map(UserMapper::toDto).toList(),
                total,
                done,
                progress,
                list.getParent() != null ? list.getParent().getId() : null,
                list.getCreatedAt()
        );
    }
}
