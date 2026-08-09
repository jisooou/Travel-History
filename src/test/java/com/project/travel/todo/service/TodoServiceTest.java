package com.project.travel.todo.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.entity.TravelType;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.todo.dto.request.TodoCreateRequestDto;
import com.project.travel.todo.dto.request.TodoStatusUpdateRequestDto;
import com.project.travel.todo.dto.response.TodoResponseDto;
import com.project.travel.todo.dto.response.TodoStatusResponseDto;
import com.project.travel.todo.entity.Todo;
import com.project.travel.todo.repository.TodoRepository;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private RecordDayRepository recordDayRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CollabAuthorityService collabAuthorityService;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("Todo 생성에 성공한다")
    void create_todo_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        Integer todoNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        TodoCreateRequestDto requestDto = createTodoCreateRequest("모자 챙기기");
        Todo savedTodo = createTodo(todoNo, recordDay, user, "모자 챙기기", Todo.CompletedStatus.NOT_DONE);

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(userRepository.findById(userNo))
                .thenReturn(Optional.of(user));
        when(todoRepository.save(any(Todo.class)))
                .thenReturn(savedTodo);

//        when
        TodoResponseDto responseDto = todoService.createTodo(userNo, dayNo, requestDto);

//        then
        assertThat(responseDto.getTodoNo()).isEqualTo(todoNo);
        assertThat(responseDto.getDayNo()).isEqualTo(dayNo);
        assertThat(responseDto.getWriterNo()).isEqualTo(userNo);
        assertThat(responseDto.getTodoContent()).isEqualTo("모자 챙기기");

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("존재하지 않는 RecordDay의 Todo를 생성하면 예외가 발생한다")
    void create_todo_fail() {
//        given
        Integer userNo = 1;
        Integer dayNo = 999;

        TodoCreateRequestDto requestDto = createTodoCreateRequest("모자 챙기기");

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                todoService.createTodo(userNo, dayNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_DAY_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(userRepository, never()).findById(anyInt());
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    @DisplayName("회원 Todo 조회에 성공한다")
    void get_user_todo_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Todo todo1 = createTodo(1, recordDay, user, "모자 챙기기", Todo.CompletedStatus.NOT_DONE);
        Todo todo2 = createTodo(2, recordDay, user, "여권 챙기기", Todo.CompletedStatus.NOT_DONE);

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(todoRepository.findByDay_DayNoOrderByCreatedAtAsc(dayNo))
                .thenReturn(List.of(todo1, todo2));

//        when
        List<TodoResponseDto> responseDtos = todoService.getUserTodoOfDay(userNo, dayNo);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(TodoResponseDto::getTodoContent)
                .containsExactly("모자 챙기기", "여권 챙기기");

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(todoRepository).findByDay_DayNoOrderByCreatedAtAsc(dayNo);
    }

    @Test
    @DisplayName("존재하지 않는 RecordDay의 Todo를 조회하면 예외가 발생한다")
    void get_todo_fail() {
//        given
        Integer userNo = 1;
        Integer dayNo = 999;

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                todoService.getUserTodoOfDay(userNo, dayNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_DAY_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(todoRepository, never()).findByDay_DayNoOrderByCreatedAtAsc(anyInt());
    }

    @Test
    @DisplayName("비회원 Todo 조회에 성공한다")
    void get_guest_todo_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        String joinCode = "ABCD1234";

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Todo todo1 = createTodo(1, recordDay, user, "모자 챙기기", Todo.CompletedStatus.NOT_DONE);
        Todo todo2 = createTodo(2, recordDay, user, "여권 챙기기", Todo.CompletedStatus.NOT_DONE);

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(todoRepository.findByDay_DayNoOrderByCreatedAtAsc(dayNo))
                .thenReturn(List.of(todo1, todo2));

//        when
        List<TodoResponseDto> responseDtos = todoService.getGuestTodoOfDay(dayNo, joinCode);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(TodoResponseDto::getTodoContent)
                .containsExactly("모자 챙기기", "여권 챙기기");

        verify(collabAuthorityService).checkGuest(recordNo, joinCode);
        verify(todoRepository).findByDay_DayNoOrderByCreatedAtAsc(dayNo);
    }

    @Test
    @DisplayName("Todo 수정에 성공한다")
    void update_todo_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        Integer todoNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);
        Todo todo = createTodo(todoNo, recordDay, user, "모자 챙기기", Todo.CompletedStatus.NOT_DONE);

        TodoCreateRequestDto requestDto = createTodoCreateRequest("여권 챙기기");

        when(todoRepository.findById(todoNo))
                .thenReturn(Optional.of(todo));

//        when
        TodoResponseDto responseDto = todoService.updateTodoOfDay(userNo, todoNo, requestDto);

//        then
        assertThat(responseDto.getTodoContent()).isEqualTo("여권 챙기기");

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(todoRepository).flush();
    }

    @Test
    @DisplayName("존재하지 않는 Todo를 수정하면 예외가 발생한다")
    void update_todo_fail() {
//        given
        Integer userNo = 1;
        Integer todoNo = 999;

        TodoCreateRequestDto requestDto = createTodoCreateRequest("여권 챙기기");

        when(todoRepository.findById(todoNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                todoService.updateTodoOfDay(userNo, todoNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(todoRepository, never()).flush();
    }

    @Test
    @DisplayName("Todo 완료여부 상태 업데이트에 성공한다")
    void update_todo_status_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        Integer todoNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);
        Todo todo = createTodo(todoNo, recordDay, user, "여권 챙기기", Todo.CompletedStatus.NOT_DONE);

        TodoStatusUpdateRequestDto requestDto = createTodoStatusUpdateRequest(todoNo, Todo.CompletedStatus.DONE);

        when(todoRepository.findById(todoNo))
                .thenReturn(Optional.of(todo));

//        when
        TodoStatusResponseDto responseDto = todoService.updateTodoStatus(userNo, todoNo, requestDto);

//        then
        assertThat(responseDto.getTodoNo()).isEqualTo(todoNo);
        assertThat(responseDto.getCompletedStatus()).isEqualTo(Todo.CompletedStatus.DONE);

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(todoRepository).flush();
    }

    @Test
    @DisplayName("존재하지 않는 Todo의 완료여부 상태 업데이트를 하면 예외가 발생한다")
    void update_todo_status_fail() {
//        given
        Integer userNo = 1;
        Integer todoNo = 999;

        TodoStatusUpdateRequestDto requestDto = createTodoStatusUpdateRequest(todoNo, Todo.CompletedStatus.DONE);

        when(todoRepository.findById(todoNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                todoService.updateTodoStatus(userNo, todoNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(todoRepository, never()).flush();
    }

    @Test
    @DisplayName("Todo 삭제에 성공한다")
    void delete_todo_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        Integer todoNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);
        Todo todo = createTodo(todoNo, recordDay, user, "여권 챙기기", Todo.CompletedStatus.NOT_DONE);

        when(todoRepository.findById(todoNo))
                .thenReturn(Optional.of(todo));

//        when
        todoService.deleteTodo(userNo, todoNo);

//        then
        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(todoRepository).delete(todo);
        verify(todoRepository).flush();
    }

    @Test
    @DisplayName("존재하지 않는 Todo를 삭제하면 예외가 발생한다")
    void delete_todo_fail() {
//        given
        Integer userNo = 1;
        Integer todoNo = 999;

        when(todoRepository.findById(todoNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                todoService.deleteTodo(userNo, todoNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(todoRepository, never()).delete(any(Todo.class));
        verify(todoRepository, never()).flush();
    }

    @Test
    @DisplayName("Todo 수정 중 낙관적 락 충돌이 발생하면 예외가 발생한다")
    void update_todo_conflict_fail() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        Integer todoNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);
        Todo todo = createTodo(todoNo, recordDay, user, "여권 챙기기", Todo.CompletedStatus.NOT_DONE);

        TodoCreateRequestDto requestDto = createTodoCreateRequest("모자 챙기기");

        when(todoRepository.findById(todoNo))
                .thenReturn(Optional.of(todo));

        doThrow(new ObjectOptimisticLockingFailureException(Todo.class, todoNo))
                .when(todoRepository).flush();

//        when, then
        assertThatThrownBy(() ->
                todoService.updateTodoOfDay(userNo, todoNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TODO_CONFLICT.getMessage());

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(todoRepository).flush();
    }

    private User createUser(Integer userNo) {
        User user = User.builder()
                .email("test@test.com")
                .userName("test")
                .password("test12345")
                .build();
        ReflectionTestUtils.setField(user, "userNo", userNo);
        return user;
    }

    private Record createRecord(User user, Integer recordNo) {
        Record record = Record.builder()
                .owner(user)
                .recordName("제주 여행")
                .travelType(TravelType.DOMESTIC)
                .build();
        ReflectionTestUtils.setField(record, "recordNo", recordNo);
        return record;
    }

    private RecordDay createRecordDay(Record record, Integer dayNo) {
        RecordDay recordDay = RecordDay.builder()
                .record(record)
                .travelDate(LocalDate.of(2026, 1, 1))
                .build();
        ReflectionTestUtils.setField(recordDay, "dayNo", dayNo);
        return recordDay;
    }

    private Todo createTodo(
            Integer todoNo,
            RecordDay recordDay,
            User writer,
            String todoContent,
            Todo.CompletedStatus completedStatus
    ) {
        Todo todo = Todo.builder()
                .day(recordDay)
                .writer(writer)
                .todoContent(todoContent)
                .isCompleted(completedStatus)
                .build();
        ReflectionTestUtils.setField(todo, "todoNo", todoNo);
        return todo;
    }

    private TodoCreateRequestDto createTodoCreateRequest(String todoContent) {
        TodoCreateRequestDto requestDto = new TodoCreateRequestDto();

        ReflectionTestUtils.setField(requestDto, "todoContent", todoContent);
        return requestDto;
    }

    private TodoStatusUpdateRequestDto createTodoStatusUpdateRequest(Integer todoNo, Todo.CompletedStatus completedStatus) {
        TodoStatusUpdateRequestDto requestDto = new TodoStatusUpdateRequestDto();

        ReflectionTestUtils.setField(requestDto, "todoNo", todoNo);
        ReflectionTestUtils.setField(requestDto, "completedStatus", completedStatus);
        return requestDto;
    }
}