package com.together.domain.item;

import com.together.domain.list.TodoList;
import com.together.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "todo_items")
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "list_id")
    private TodoList list;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private boolean done = false;

    @ManyToOne
    @JoinColumn(name = "checked_by_id")
    private User checkedBy;

    private Instant checkedAt;

    @Column(nullable = false)
    private int position;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false)
    private Instant createdAt;

    public TodoItem() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public TodoList getList() { return list; }
    public void setList(TodoList list) { this.list = list; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public User getCheckedBy() { return checkedBy; }
    public void setCheckedBy(User checkedBy) { this.checkedBy = checkedBy; }

    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
