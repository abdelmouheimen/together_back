package com.together.domain.list;

import com.together.dto.list.AddMemberRequest;
import com.together.dto.list.CreateListRequest;
import com.together.dto.list.TodoListDto;
import com.together.dto.list.UpdateListRequest;
import com.together.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lists")
@Tag(name = "Lists")
public class TodoListController {

    private final TodoListService todoListService;

    public TodoListController(TodoListService todoListService) {
        this.todoListService = todoListService;
    }

    @GetMapping
    @Operation(summary = "Get my lists")
    public List<TodoListDto> getMyLists() {
        return todoListService.getMyLists(SecurityUtils.currentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new list")
    public TodoListDto createList(@Valid @RequestBody CreateListRequest request) {
        return todoListService.create(SecurityUtils.currentUser(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a list by ID")
    public TodoListDto getList(@PathVariable UUID id) {
        return todoListService.getById(SecurityUtils.currentUser(), id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a list")
    public TodoListDto updateList(@PathVariable UUID id, @Valid @RequestBody UpdateListRequest request) {
        return todoListService.update(SecurityUtils.currentUser(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a list (creator only)")
    public void deleteList(@PathVariable UUID id) {
        todoListService.delete(SecurityUtils.currentUser(), id);
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add a member to a list")
    public TodoListDto addMember(@PathVariable UUID id, @Valid @RequestBody AddMemberRequest request) {
        return todoListService.addMember(SecurityUtils.currentUser(), id, request);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a member from a list (creator only)")
    public void removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        todoListService.removeMember(SecurityUtils.currentUser(), id, userId);
    }
}
