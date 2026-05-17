package com.together.domain.activity;

import com.together.domain.list.TodoList;
import com.together.domain.list.TodoListService;
import com.together.domain.user.User;
import com.together.dto.PageResponse;
import com.together.dto.activity.ActivityEventDto;
import com.together.util.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ActivityEventService {

    private final ActivityEventRepository activityEventRepository;
    private final TodoListService todoListService;

    public ActivityEventService(ActivityEventRepository activityEventRepository,
                                 TodoListService todoListService) {
        this.activityEventRepository = activityEventRepository;
        this.todoListService = todoListService;
    }

    public PageResponse<ActivityEventDto> getForUser(User currentUser, int page, int size) {
        Page<ActivityEvent> result = activityEventRepository.findForUser(currentUser, PageRequest.of(page, size));
        return toPageResponse(result, page, size);
    }

    public PageResponse<ActivityEventDto> getForList(User currentUser, UUID listId, int page, int size) {
        TodoList list = todoListService.findAndCheckMembership(currentUser, listId);
        Page<ActivityEvent> result = activityEventRepository.findByListOrderByCreatedAtDesc(list, PageRequest.of(page, size));
        return toPageResponse(result, page, size);
    }

    private PageResponse<ActivityEventDto> toPageResponse(Page<ActivityEvent> page, int pageNum, int size) {
        List<ActivityEventDto> content = page.getContent().stream().map(this::toDto).toList();
        return new PageResponse<>(content, pageNum, size, page.getTotalElements(),
                page.getTotalPages(), page.hasNext());
    }

    private ActivityEventDto toDto(ActivityEvent event) {
        return new ActivityEventDto(
                event.getId(),
                event.getType(),
                UserMapper.toDto(event.getActor()),
                event.getList().getId(),
                event.getList().getName(),
                event.getItem() != null ? event.getItem().getId() : null,
                event.getItem() != null ? event.getItem().getText() : null,
                event.getExtraText(),
                event.getCreatedAt()
        );
    }
}
