package com.together.domain.friendship;

import com.together.domain.activity.ActivityEvent;
import com.together.domain.activity.ActivityEventRepository;
import com.together.domain.activity.ActivityType;
import com.together.domain.list.TodoList;
import com.together.domain.list.TodoListRepository;
import com.together.domain.user.User;
import com.together.domain.user.UserRepository;
import com.together.dto.friendship.FriendshipDto;
import com.together.dto.friendship.SendFriendRequestDto;
import com.together.dto.friendship.UpdateFriendshipRequest;
import com.together.exception.ConflictException;
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
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final ActivityEventRepository activityEventRepository;
    private final TodoListRepository todoListRepository;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             ActivityEventRepository activityEventRepository,
                             TodoListRepository todoListRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.activityEventRepository = activityEventRepository;
        this.todoListRepository = todoListRepository;
    }

    @Transactional(readOnly = true)
    public List<FriendshipDto> getAcceptedFriendships(User currentUser) {
        return friendshipRepository.findAcceptedFriendships(currentUser)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<FriendshipDto> getPendingRequests(User currentUser) {
        return friendshipRepository.findByAddresseeAndStatus(currentUser, FriendshipStatus.PENDING)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<FriendshipDto> getSentRequests(User currentUser) {
        return friendshipRepository.findByRequesterOrderByCreatedAtDesc(currentUser)
                .stream().map(this::toDto).toList();
    }

    public FriendshipDto sendRequest(User requester, SendFriendRequestDto request) {
        User addressee = userRepository.findById(request.addresseeId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.addresseeId()));

        if (requester.getId().equals(addressee.getId())) {
            throw new ConflictException("Cannot send friend request to yourself");
        }

        friendshipRepository.findBetweenUsers(requester.getId(), addressee.getId())
                .ifPresent(f -> { throw new ConflictException("Friendship already exists"); });

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(Instant.now());

        friendshipRepository.save(friendship);
        return toDto(friendship);
    }

    public FriendshipDto updateFriendship(User currentUser, UUID friendshipId, UpdateFriendshipRequest request) {
        Friendship friendship = findById(friendshipId);

        if (!friendship.getAddressee().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the addressee can respond to a friend request");
        }

        friendship.setStatus(request.status());
        friendship.setUpdatedAt(Instant.now());
        friendshipRepository.save(friendship);

        if (request.status() == FriendshipStatus.ACCEPTED) {
            emitFriendJoinedEvents(currentUser, friendship.getRequester());
        }

        return toDto(friendship);
    }

    public void deleteFriendship(User currentUser, UUID friendshipId) {
        Friendship friendship = findById(friendshipId);
        boolean isParty = friendship.getRequester().getId().equals(currentUser.getId())
                || friendship.getAddressee().getId().equals(currentUser.getId());
        if (!isParty) {
            throw new UnauthorizedException("Not a party to this friendship");
        }
        friendshipRepository.delete(friendship);
    }

    private void emitFriendJoinedEvents(User actor, User other) {
        List<TodoList> sharedLists = todoListRepository.findByMember(actor).stream()
                .filter(l -> l.getMembers().contains(other))
                .toList();

        for (TodoList list : sharedLists) {
            ActivityEvent event = new ActivityEvent();
            event.setType(ActivityType.FRIEND_JOINED);
            event.setActor(actor);
            event.setList(list);
            event.setCreatedAt(Instant.now());
            activityEventRepository.save(event);
        }
    }

    private Friendship findById(UUID id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Friendship not found: " + id));
    }

    private FriendshipDto toDto(Friendship f) {
        return new FriendshipDto(
                f.getId(),
                UserMapper.toDto(f.getRequester()),
                UserMapper.toDto(f.getAddressee()),
                f.getStatus(),
                f.getCreatedAt(),
                f.getUpdatedAt()
        );
    }
}
