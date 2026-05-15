package com.project.travel.todo.dto.request;

import com.project.travel.todo.entity.Todo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class TodoCreateRequestDto {
    @NotNull
    private Integer dayNo;

    @NotBlank(message = "할일을 작성해 주세요.")
    @Size(max = 255)
    private String todoContent;
}
