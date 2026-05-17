package com.together.domain.friendship;

import com.together.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friendships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "addressee_id"}))
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(optional = false)
    @JoinColumn(name = "addressee_id")
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    public Friendship() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public User getAddressee() { return addressee; }
    public void setAddressee(User addressee) { this.addressee = addressee; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
