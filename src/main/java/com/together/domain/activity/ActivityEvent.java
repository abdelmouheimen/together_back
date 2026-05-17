package com.together.domain.activity;

import com.together.domain.item.TodoItem;
import com.together.domain.list.TodoList;
import com.together.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activity_events")
public class ActivityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actor_id")
    private User actor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "list_id")
    private TodoList list;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private TodoItem item;

    private String extraText;

    @Column(nullable = false)
    private Instant createdAt;

    public ActivityEvent() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ActivityType getType() { return type; }
    public void setType(ActivityType type) { this.type = type; }

    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }

    public TodoList getList() { return list; }
    public void setList(TodoList list) { this.list = list; }

    public TodoItem getItem() { return item; }
    public void setItem(TodoItem item) { this.item = item; }

    public String getExtraText() { return extraText; }
    public void setExtraText(String extraText) { this.extraText = extraText; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
