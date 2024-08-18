package com.zolotarev.tms.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.Id;


import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks")
@Schema(description = "Сущность задачи")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column
    private String description;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    @Column(name = "author_id", insertable = false, updatable = false)
    private Long authorId;

    @ManyToOne
    @JoinColumn(name = "performer_id")
    @JsonIgnore
    private User performer;

    @Column(name = "performer_id", insertable = false, updatable = false)
    private Long performerId;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public enum TaskStatus {
        ON_HOLD,
        IN_PROGRESS,
        COMPLETED
    }

    public enum TaskPriority {
        LOW,
        MIDDLE,
        HIGH
    }

    public void setAuthor(User author) {
        this.author = author;
        author.getAuthorTasks().add(this);
    }

    public void setPerformer(User performer) {
        if (performer == null) {
            if (this.performer == null) return;
            this.performer.getPerformerTasks().remove(this);
            this.performer = null;
            return;
        }
        performer.getPerformerTasks().add(this);
        this.performer = performer;
    }
}