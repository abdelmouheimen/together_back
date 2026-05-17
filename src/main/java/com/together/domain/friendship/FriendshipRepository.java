package com.together.domain.friendship;

import com.together.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("user") User user);

    List<Friendship> findByAddresseeAndStatus(User addressee, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :u1 AND f.addressee.id = :u2) OR (f.requester.id = :u2 AND f.addressee.id = :u1)")
    Optional<Friendship> findBetweenUsers(@Param("u1") UUID userId1, @Param("u2") UUID userId2);
}
