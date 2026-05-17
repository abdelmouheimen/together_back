package com.together.domain.activity;

import com.together.dto.PageResponse;
import com.together.dto.activity.ActivityEventDto;
import com.together.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/activity")
@Tag(name = "Activity")
public class ActivityEventController {

    private final ActivityEventService activityEventService;

    public ActivityEventController(ActivityEventService activityEventService) {
        this.activityEventService = activityEventService;
    }

    @GetMapping
    @Operation(summary = "Get activity feed for current user")
    public PageResponse<ActivityEventDto> getActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return activityEventService.getForUser(SecurityUtils.currentUser(), page, size);
    }

    @GetMapping("/lists/{listId}")
    @Operation(summary = "Get activity feed for a specific list")
    public PageResponse<ActivityEventDto> getListActivity(
            @PathVariable UUID listId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return activityEventService.getForList(SecurityUtils.currentUser(), listId, page, size);
    }
}
