package com.project.travel.todo.entity;

import com.project.travel.record.entity.RecordDay;
import com.project.travel.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "todo",
        indexes = {
                @Index(name = "idx_todo_day", columnList = "DAY_NO")
        }
)
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer todoNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "DAY_NO",
            nullable = false,
            referencedColumnName = "DAY_NO",
            foreignKey = @ForeignKey(name = "fk_todo_day")
    )
    private RecordDay day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "WRITER_NO",
            nullable = false,
            referencedColumnName = "USER_NO",
            foreignKey = @ForeignKey(name = "fk_todo_writer")
    )
    private User writer;

    @Column(name = "TODO_CONTENT", length = 255, nullable = false)
    private String todoContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "TODO_CONTENT", length = 10, nullable = false, comment = "DONE or NOT_DONE")
    private CompletedStatus isCompleted;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public void Todo(RecordDay day, User writer, String todoContent, CompletedStatus isCompleted) {
        this.day = day;
        this.writer = writer;
        this.todoContent = todoContent;
        this.isCompleted = isCompleted;
    }

    @PrePersist
    public void prePersist() {
        if (this.isCompleted == null) {
            this.isCompleted = CompletedStatus.NOT_DONE;
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public enum CompletedStatus {
        DONE,
        NOT_DONE
    }
}
