package com.together.domain.item;

import com.together.dto.item.CreateItemRequest;
import com.together.dto.item.ReorderItemRequest;
import com.together.dto.item.TodoItemDto;
import com.together.dto.item.ToggleItemRequest;
import com.together.dto.item.UpdateItemRequest;
import com.together.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lists/{listId}/items")
@Tag(name = "Items")
public class TodoItemController {

    private final TodoItemService todoItemService;

    public TodoItemController(TodoItemService todoItemService) {
        this.todoItemService = todoItemService;
    }

    @GetMapping
    @Operation(summary = "Get all items for a list")
    public List<TodoItemDto> getItems(@PathVariable("listId") UUID listId) {
        return todoItemService.getItems(SecurityUtils.currentUser(), listId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an item to a list")
    public TodoItemDto createItem(@PathVariable("listId") UUID listId,
                                  @Valid @RequestBody CreateItemRequest request) {
        return todoItemService.create(SecurityUtils.currentUser(), listId, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update item text")
    public TodoItemDto updateItem(@PathVariable("listId") UUID listId,
                                  @PathVariable("id") UUID id,
                                  @Valid @RequestBody UpdateItemRequest request) {
        return todoItemService.update(SecurityUtils.currentUser(), listId, id, request);
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle item done/undone")
    public TodoItemDto toggleItem(@PathVariable("listId") UUID listId,
                                  @PathVariable("id") UUID id,
                                  @RequestBody ToggleItemRequest request) {
        return todoItemService.toggle(SecurityUtils.currentUser(), listId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an item")
    public void deleteItem(@PathVariable("listId") UUID listId, @PathVariable("id") UUID id) {
        todoItemService.delete(SecurityUtils.currentUser(), listId, id);
    }

    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reorder items")
    public void reorderItems(@PathVariable("listId") UUID listId,
                              @RequestBody List<ReorderItemRequest> requests) {
        todoItemService.reorder(SecurityUtils.currentUser(), listId, requests);
    }
}
