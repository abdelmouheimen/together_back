package com.together.domain.user;

import com.together.dto.PageResponse;
import com.together.dto.user.UpdateUserRequest;
import com.together.dto.user.UserDto;
import com.together.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public UserDto getMe() {
        return userService.getMe(SecurityUtils.currentUser());
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public UserDto updateMe(@Valid @RequestBody UpdateUserRequest request) {
        return userService.updateMe(SecurityUtils.currentUser(), request);
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name or email")
    public PageResponse<UserDto> search(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return userService.search(q, page, size);
    }
}
