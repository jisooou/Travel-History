package com.project.travel.todo.service;

import com.project.travel.todo.dto.request.TodoCreateRequestDto;
import com.project.travel.todo.dto.request.TodoStatusUpdateRequestDto;
import com.project.travel.todo.dto.response.TodoResponseDto;
import com.project.travel.todo.dto.response.TodoStatusResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public class TodoService {
    public TodoResponseDto createTodo(Integer userNo, Integer dayNo, @Valid TodoCreateRequestDto createRequestDto) {
    }

    public List<TodoResponseDto> getTodoOfDay(Integer userNo, Integer dayNo) {
        return null;
    }

    public TodoResponseDto updateTodoOfDay(Integer userNo, Integer todoNo, @Valid TodoCreateRequestDto createRequestDto) {
    }

    public TodoStatusResponseDto updateTodoStatus(Integer userNo, Integer todoNo, @Valid TodoStatusUpdateRequestDto requestDto) {
    }

    public void deleteTodo(Integer userNo, Integer todoNo) {

    }
}
