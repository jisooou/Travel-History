package com.project.travel.todo.service;

import com.project.travel.collab.service.CollabAuthorityService;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    private final CollabAuthorityService collabAuthorityService;

    //    날짜에 따라서 Todo를 작성한다.
    @Transactional
    public TodoResponseDto createTodo(Integer userNo, Integer dayNo, @Valid TodoCreateRequestDto createRequestDto) {
        RecordDay recordDay = getRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkMemberEditor(recordNo, userNo);

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

    public List<TodoResponseDto> getUserTodoOfDay(Integer userNo, Integer dayNo) {
        RecordDay recordDay = getRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkMemberViewer(recordNo, userNo);

        return todoRepository.findByDay_DayNoOrderByCreatedAtAsc(dayNo)
                .stream()
                .map(TodoResponseDto::from)
                .toList();
    }

    public List<TodoResponseDto> getGuestTodoOfDay(Integer dayNo, String joinCode) {
        RecordDay recordDay = getRecordDay(dayNo);
        Integer recordNo = recordDay.getRecord().getRecordNo();

        collabAuthorityService.checkGuest(recordNo, joinCode);

        return todoRepository.findByDay_DayNoOrderByCreatedAtAsc(dayNo)
                .stream()
                .map(TodoResponseDto::from)
                .toList();
    }

    @Transactional
    public TodoResponseDto updateTodoOfDay(Integer userNo, Integer todoNo, @Valid TodoCreateRequestDto createRequestDto) {
        try {
            Todo todo = getAccessTodo(todoNo);
            Integer recordNo = todo.getDay().getRecord().getRecordNo();

            collabAuthorityService.checkMemberEditor(recordNo, userNo);

            todo.updateContent(createRequestDto.getTodoContent());
            todoRepository.flush();
            return TodoResponseDto.from(todo);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.TODO_CONFLICT);
        }
    }

    @Transactional
    public TodoStatusResponseDto updateTodoStatus(Integer userNo, Integer todoNo, @Valid TodoStatusUpdateRequestDto requestDto) {
        try {
            Todo todo = getAccessTodo(todoNo);
            Integer recordNo = todo.getDay().getRecord().getRecordNo();

            collabAuthorityService.checkMemberEditor(recordNo, userNo);

            todo.updateStatus(requestDto.getCompletedStatus());
            todoRepository.flush();
            return TodoStatusResponseDto.from(todo);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.TODO_CONFLICT);
        }
    }

    @Transactional
    public void deleteTodo(Integer userNo, Integer todoNo) {
        try {
            Todo todo = getAccessTodo(todoNo);
            Integer recordNo = todo.getDay().getRecord().getRecordNo();

            collabAuthorityService.checkMemberEditor(recordNo, userNo);

            todoRepository.delete(todo);
            todoRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.TODO_CONFLICT);
        }
    }

    private RecordDay getRecordDay(Integer dayNo) {
        return recordDayRepository.findById(dayNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_DAY_NOT_FOUND));
    }

    private Todo getAccessTodo(Integer todoNo) {
        return todoRepository.findById(todoNo)
                .orElseThrow(() -> new CustomException(ErrorCode.TODO_NOT_FOUND));
    }
}
