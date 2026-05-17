package com.together.domain.comment;

import com.together.domain.item.TodoItem;
import com.together.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id")
    private TodoItem item;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false)
    private Instant createdAt;

    public Comment() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public TodoItem getItem() { return item; }
    public void setItem(TodoItem item) { this.item = item; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
