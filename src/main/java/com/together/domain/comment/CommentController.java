package com.together.domain.comment;

import com.together.dto.comment.CommentDto;
import com.together.dto.comment.CreateCommentRequest;
import com.together.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lists/{listId}/items/{itemId}/comments")
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(summary = "Get comments for an item")
    public List<CommentDto> getComments(@PathVariable("listId") UUID listId, @PathVariable("itemId") UUID itemId) {
        return commentService.getComments(SecurityUtils.currentUser(), listId, itemId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a comment to an item")
    public CommentDto createComment(@PathVariable("listId") UUID listId,
                                    @PathVariable("itemId") UUID itemId,
                                    @Valid @RequestBody CreateCommentRequest request) {
        return commentService.create(SecurityUtils.currentUser(), listId, itemId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a comment (author only)")
    public void deleteComment(@PathVariable("listId") UUID listId,
                               @PathVariable("itemId") UUID itemId,
                               @PathVariable("id") UUID id) {
        commentService.delete(SecurityUtils.currentUser(), listId, itemId, id);
    }
}
