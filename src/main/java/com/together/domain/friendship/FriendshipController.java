package com.together.domain.friendship;

import com.together.dto.friendship.FriendshipDto;
import com.together.dto.friendship.SendFriendRequestDto;
import com.together.dto.friendship.UpdateFriendshipRequest;
import com.together.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friendships")
@Tag(name = "Friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping
    @Operation(summary = "Get accepted friends")
    public List<FriendshipDto> getFriends() {
        return friendshipService.getAcceptedFriendships(SecurityUtils.currentUser());
    }

    @GetMapping("/requests")
    @Operation(summary = "Get pending friend requests received")
    public List<FriendshipDto> getPendingRequests() {
        return friendshipService.getPendingRequests(SecurityUtils.currentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Send a friend request")
    public FriendshipDto sendRequest(@Valid @RequestBody SendFriendRequestDto request) {
        return friendshipService.sendRequest(SecurityUtils.currentUser(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Accept or decline a friend request")
    public FriendshipDto updateFriendship(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateFriendshipRequest request) {
        return friendshipService.updateFriendship(SecurityUtils.currentUser(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a friendship")
    public void deleteFriendship(@PathVariable UUID id) {
        friendshipService.deleteFriendship(SecurityUtils.currentUser(), id);
    }
}
