package com.project.travel.todo.dto.request;

import com.project.travel.todo.entity.Todo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TodoStatusUpdateRequestDto {
    @NotNull
    private Integer todoNo;

    @NotNull
    private Todo.CompletedStatus completedStatus;
}
