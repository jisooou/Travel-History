package com.project.travel.todo.dto.response;

import com.project.travel.todo.entity.Todo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TodoResponseDto {
    private Integer todoNo;
    private Integer dayNo;
    private Integer writerNo;
    private String writerName;
    private String todoContent;
    private LocalDateTime createdAt;

    public static TodoResponseDto from(Todo todo) {
        return TodoResponseDto.builder()
                .todoNo(todo.getTodoNo())
                .dayNo(todo.getDay().getDayNo())
                .writerNo(todo.getWriter().getUserNo())
                .writerName(todo.getWriter().getUserName())
                .todoContent(todo.getTodoContent())
                .createdAt(todo.getCreatedAt())
                .build();
    }
}
