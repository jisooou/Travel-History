package com.project.travel.todo.service;

import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.todo.dto.request.TodoCreateRequestDto;
import com.project.travel.todo.dto.request.TodoStatusUpdateRequestDto;
import com.project.travel.todo.dto.response.TodoResponseDto;
import com.project.travel.todo.dto.response.TodoStatusResponseDto;
import com.project.travel.todo.entity.Todo;
import com.project.travel.todo.repository.TodoRepository;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository todoRepository;
    private final RecordDayRepository recordDayRepository;
    private final UserRepository userRepository;

    //    날짜에 따라서 Todo를 작성한다.
    @Transactional
    public TodoResponseDto createTodo(Integer userNo, Integer dayNo, @Valid TodoCreateRequestDto createRequestDto) {
        RecordDay recordDay = getRecordDay(userNo, dayNo);

        User writer = userRepository.findById(userNo)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Todo todo = Todo.builder()
                .day(recordDay)
                .writer(writer)
                .todoContent(createRequestDto.getTodoContent())
                .isCompleted(Todo.CompletedStatus.NOT_DONE)
                .build();

        Todo savedTodo = todoRepository.save(todo);
        return TodoResponseDto.from(savedTodo);
    }

    public List<TodoResponseDto> getTodoOfDay(Integer userNo, Integer dayNo) {
        getRecordDay(userNo, dayNo);

        return todoRepository.findByDay_DayNoOrderByCreatedAtAsc(dayNo)
                .stream()
                .map(TodoResponseDto::from)
                .toList();
    }

    @Transactional
    public TodoResponseDto updateTodoOfDay(Integer userNo, Integer todoNo, @Valid TodoCreateRequestDto createRequestDto) {
        Todo todo = getMyTodo(userNo, todoNo);
        todo.updateContent(createRequestDto.getTodoContent());
        return TodoResponseDto.from(todo);
    }

    @Transactional
    public TodoStatusResponseDto updateTodoStatus(Integer userNo, Integer todoNo, @Valid TodoStatusUpdateRequestDto requestDto) {
        Todo todo = getMyTodo(userNo, todoNo);
        todo.updateStatus(requestDto.getCompletedStatus());
        return TodoStatusResponseDto.from(todo);
    }

    @Transactional
    public void deleteTodo(Integer userNo, Integer todoNo) {
        Todo todo = getMyTodo(userNo, todoNo);
        todoRepository.delete(todo);
    }

    private RecordDay getRecordDay(Integer userNo, Integer dayNo) {
        RecordDay recordDay = recordDayRepository.findById(dayNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_DAY_NOT_FOUND));

        if (!recordDay.getRecord().getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.RECORD_DAY_ACCESS_DENIED);
        }
        return recordDay;
    }

    private Todo getMyTodo(Integer userNo, Integer todoNo) {
        Todo todo = todoRepository.findById(todoNo)
                .orElseThrow(() -> new CustomException(ErrorCode.TODO_NOT_FOUND));

        if (!todo.getDay().getRecord().getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.TODO_ACCESS_DENIED);
        }
        return todo;
    }
}
