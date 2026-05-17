package com.together.domain.user;

import com.together.dto.PageResponse;
import com.together.dto.user.UpdateUserRequest;
import com.together.dto.user.UserDto;
import com.together.util.AvatarUtils;
import com.together.util.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserDto getMe(User currentUser) {
        return UserMapper.toDto(currentUser);
    }

    public UserDto updateMe(User currentUser, UpdateUserRequest request) {
        currentUser.setName(request.name());
        currentUser.setInitials(AvatarUtils.generateInitials(request.name()));
        currentUser.setAvatarColor(request.avatarColor());
        currentUser.setAvatarTextColor(AvatarUtils.computeTextColor(request.avatarColor()));
        userRepository.save(currentUser);
        return UserMapper.toDto(currentUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> search(String q, int page, int size) {
        Page<User> result = userRepository.searchByNameOrEmail(q, PageRequest.of(page, size));
        List<UserDto> content = result.getContent().stream().map(UserMapper::toDto).toList();
        return new PageResponse<>(content, page, size, result.getTotalElements(),
                result.getTotalPages(), result.hasNext());
    }
}
