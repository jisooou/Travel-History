package com.project.travel.todo.controller;

import com.project.travel.auth.security.CustomUserDetails;
import com.project.travel.global.response.ApiResponse;
import com.project.travel.todo.dto.request.TodoCreateRequestDto;
import com.project.travel.todo.dto.request.TodoStatusUpdateRequestDto;
import com.project.travel.todo.dto.response.TodoResponseDto;
import com.project.travel.todo.dto.response.TodoStatusResponseDto;
import com.project.travel.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todo")
public class TodoController {
    private final TodoService todoService;

    //    특정 날짜에 투두 추가
    @PostMapping("/days/{dayNo}")
    public ApiResponse<TodoResponseDto> createTodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer dayNo,
            @Valid @RequestBody TodoCreateRequestDto createRequestDto
    ) {
        return ApiResponse.success(todoService.createTodo(userDetails.getUserNo(), dayNo, createRequestDto));
    }

    //    특정 날짜 투두 조회
    @GetMapping("/days/{dayNo}")
    public ApiResponse<List<TodoResponseDto>> getTodoOfDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer dayNo
    ) {
        return ApiResponse.success(todoService.getTodoOfDay(userDetails.getUserNo(), dayNo));
    }

    @PatchMapping("/{todoNo}")
    public ApiResponse<TodoResponseDto> updateTodoOfDay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer todoNo,
            @Valid @RequestBody TodoCreateRequestDto createRequestDto
    ) {
        return ApiResponse.success(todoService.updateTodoOfDay(userDetails.getUserNo(), todoNo, createRequestDto));
    }

    @PatchMapping("/{todoNo}/status")
    public ApiResponse<TodoStatusResponseDto> updateTodoStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer todoNo,
            @Valid @RequestBody TodoStatusUpdateRequestDto requestDto
    ) {
        return ApiResponse.success(todoService.updateTodoStatus(userDetails.getUserNo(), todoNo, requestDto));
    }

    @DeleteMapping("/{todoNo}")
    public ApiResponse<Void> deleteTodo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer todoNo
    ) {
        todoService.deleteTodo(userDetails.getUserNo(), todoNo);
        return ApiResponse.success(null);
    }
}
