package com.project.travel.todo.dto.response;

import com.project.travel.todo.entity.Todo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoStatusResponseDto {
    private Integer todoNo;
    private Todo.CompletedStatus completedStatus;

    public static TodoStatusResponseDto from(Todo todo) {
        return TodoStatusResponseDto.builder()
                .todoNo(todo.getTodoNo())
                .completedStatus(todo.getIsCompleted())
                .build();
    }
}
