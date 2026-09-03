package com.project.travel.schedule.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.place.entity.Place;
import com.project.travel.place.repository.PlaceRepository;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.entity.TimeSlot;
import com.project.travel.record.entity.TravelType;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.schedule.dto.request.ScheduleOrderRequestDto;
import com.project.travel.schedule.dto.request.ScheduleReorderRequestDto;
import com.project.travel.schedule.dto.request.ScheduleRequestDto;
import com.project.travel.schedule.dto.response.ScheduleResponseDto;
import com.project.travel.schedule.entity.SchedulePlace;
import com.project.travel.schedule.repository.ScheduleRepository;
import com.project.travel.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private RecordDayRepository recordDayRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private CollabAuthorityService collabAuthorityService;

    @InjectMocks
    private ScheduleService scheduleService;

    @Test
    @DisplayName("Schedule 생성에 성공한다")
    void create_schedule_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        Integer placeNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);
        Place place = createPlace(record, placeNo, "카페A");

        ScheduleRequestDto requestDto = createScheduleRequest(placeNo, TimeSlot.MORNING, 1);

        SchedulePlace savedSchedule = createSchedule(
                recordDay,
                place,
                1,
                TimeSlot.MORNING,
                1
        );

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(placeRepository.findById(placeNo))
                .thenReturn(Optional.of(place));
        when(scheduleRepository.save(any(SchedulePlace.class)))
                .thenReturn(savedSchedule);

//        when
        ScheduleResponseDto responseDto = scheduleService.createSchedule(userNo, dayNo, requestDto);

//        then
        assertThat(responseDto.getScheduleNo()).isEqualTo(1);
        assertThat(responseDto.getDayNo()).isEqualTo(dayNo);
        assertThat(responseDto.getPlaceNo()).isEqualTo(placeNo);
        assertThat(responseDto.getPlaceName()).isEqualTo("카페A");
        assertThat(responseDto.getTimeSlot()).isEqualTo(TimeSlot.MORNING);
        assertThat(responseDto.getSortOrder()).isEqualTo(1);

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(scheduleRepository).save(any(SchedulePlace.class));
    }

    @Test
    @DisplayName("존재하지 않는 RecordDay의 Schedule을 생성하면 예외가 발생한다")
    void create_schedule_fail() {
//        given
        Integer userNo = 1;
        Integer dayNo = 999;
        Integer placeNo = 1;

        ScheduleRequestDto requestDto = createScheduleRequest(placeNo, TimeSlot.MORNING, 1);

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                scheduleService.createSchedule(userNo, dayNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_DAY_NOT_FOUND.getMessage());

        verify(placeRepository, never()).findById(anyInt());
        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(scheduleRepository, never()).save(any(SchedulePlace.class));
    }

    @Test
    @DisplayName("회원 Schedule 조회에 성공한다")
    void get_user_schedule_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Place placeA = createPlace(record, 1, "카페A");
        Place placeB = createPlace(record, 2, "카페B");

        SchedulePlace scheduleA = createSchedule(recordDay, placeA, 1, TimeSlot.MORNING, 1);
        SchedulePlace scheduleB = createSchedule(recordDay, placeB, 2, TimeSlot.MORNING, 2);

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(scheduleRepository.findByDay_DayNoOrderByTimeSlotAscSortOrderAsc(dayNo))
                .thenReturn(List.of(scheduleA, scheduleB));

//        when
        List<ScheduleResponseDto> responseDtos = scheduleService.getUserScheduleOfDay(userNo, dayNo);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(ScheduleResponseDto::getSortOrder)
                .containsExactly(1, 2);

        verify(collabAuthorityService).checkMemberViewer(recordNo, userNo);
        verify(scheduleRepository).findByDay_DayNoOrderByTimeSlotAscSortOrderAsc(dayNo);
    }

    @Test
    @DisplayName("존재하지 않는 RecordDay의 Schedule을 조회하면 예외가 발생한다")
    void get_schedule_fail() {
//        given
        Integer userNo = 1;
        Integer dayNo = 999;

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                scheduleService.getUserScheduleOfDay(userNo, dayNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.RECORD_DAY_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberViewer(anyInt(), anyInt());
        verify(scheduleRepository, never()).findByDay_DayNoOrderByTimeSlotAscSortOrderAsc(anyInt());
    }

    @Test
    @DisplayName("비회원 Schedule 조회에 성공한다")
    void get_guest_schedule_success() {
//        given
        Integer recordNo = 1;
        Integer dayNo = 1;
        String joinCode = "ABCD1234";

        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Place placeA = createPlace(record, 1, "카페A");
        Place placeB = createPlace(record, 2, "카페B");

        SchedulePlace scheduleA = createSchedule(recordDay, placeA, 1, TimeSlot.MORNING, 1);
        SchedulePlace scheduleB = createSchedule(recordDay, placeB, 2, TimeSlot.MORNING, 2);

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(scheduleRepository.findByDay_DayNoOrderByTimeSlotAscSortOrderAsc(dayNo))
                .thenReturn(List.of(scheduleA, scheduleB));

//        when
        List<ScheduleResponseDto> responseDtos = scheduleService.getGuestScheduleOfDay(recordNo, joinCode);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(ScheduleResponseDto::getSortOrder)
                .containsExactly(1, 2);

        verify(collabAuthorityService).checkGuest(recordNo, joinCode);
        verify(scheduleRepository).findByDay_DayNoOrderByTimeSlotAscSortOrderAsc(dayNo);
    }

    @Test
    @DisplayName("Schedule 수정에 성공한다")
    void update_schedule_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        Integer scheduleNo = 1;
        Integer newPlaceNo = 2;


        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Place oldPlace = createPlace(record, 1, "카페A");
        Place newPlace = createPlace(record, newPlaceNo, "식당B");

        SchedulePlace schedule = createSchedule(recordDay, oldPlace, scheduleNo, TimeSlot.MORNING, 1);
        ScheduleRequestDto requestDto = createScheduleRequest(newPlaceNo, TimeSlot.AFTERNOON, 1);

        when(scheduleRepository.findById(scheduleNo))
                .thenReturn(Optional.of(schedule));
        when(placeRepository.findById(newPlaceNo))
                .thenReturn(Optional.of(newPlace));

//        when
        ScheduleResponseDto responseDto = scheduleService.updateScheduleOfDay(userNo, scheduleNo, requestDto);

//        then
        assertThat(responseDto.getPlaceNo()).isEqualTo(newPlaceNo);
        assertThat(responseDto.getTimeSlot()).isEqualTo(TimeSlot.AFTERNOON);
        assertThat(responseDto.getSortOrder()).isEqualTo(1);

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
    }

    @Test
    @DisplayName("존재하지 않는 Schedule을 수정하면 예외가 발생한다")
    void update_schedule_fail() {
//        given
        Integer userNo = 1;
        Integer scheduleNo = 999;
        Integer placeNo = 1;

        ScheduleRequestDto requestDto = createScheduleRequest(placeNo, TimeSlot.MORNING, 1);

        when(scheduleRepository.findById(scheduleNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                scheduleService.updateScheduleOfDay(userNo, scheduleNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());

        verify(placeRepository, never()).findById(anyInt());
        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Schedule 삭제에 성공한다")
    void delete_schedule_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;
        Integer scheduleNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);
        Place place = createPlace(record, 1, "카페A");
        SchedulePlace schedule = createSchedule(recordDay, place, scheduleNo, TimeSlot.MORNING, 1);

        when(scheduleRepository.findById(scheduleNo))
                .thenReturn(Optional.of(schedule));

//        when
        scheduleService.deleteScheduleOfDay(userNo, scheduleNo);

//        then
        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(scheduleRepository).delete(schedule);
    }

    @Test
    @DisplayName("존재하지 않는 Schedule을 삭제하면 예외가 발생한다")
    void delete_schedule_fail() {
//        given
        Integer userNo = 1;
        Integer scheduleNo = 999;

        when(scheduleRepository.findById(scheduleNo))
                .thenReturn(Optional.empty());

//        when, then
        assertThatThrownBy(() ->
                scheduleService.deleteScheduleOfDay(userNo, scheduleNo))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());

        verify(collabAuthorityService, never()).checkMemberEditor(anyInt(), anyInt());
        verify(scheduleRepository, never()).delete(any(SchedulePlace.class));
    }

    @Test
    @DisplayName("Schedule 재정렬에 성공한다")
    void reorder_schedule_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Place placeA = createPlace(record, 1, "카페A");
        Place placeB = createPlace(record, 2, "카페B");
        Place placeC = createPlace(record, 3, "카페C");
        Place placeD = createPlace(record, 4, "카페D");

        SchedulePlace scheduleA = createSchedule(recordDay, placeA, 1, TimeSlot.MORNING, 1);
        SchedulePlace scheduleB = createSchedule(recordDay, placeB, 2, TimeSlot.MORNING, 2);
        SchedulePlace scheduleC = createSchedule(recordDay, placeC, 3, TimeSlot.MORNING, 3);
        SchedulePlace scheduleD = createSchedule(recordDay, placeD, 4, TimeSlot.MORNING, 4);

        ScheduleReorderRequestDto requestDto = createScheduleReorderRequest(
                TimeSlot.MORNING,
                List.of(
                        createScheduleOrderRequest(2, 4),
                        createScheduleOrderRequest(3, 1),
                        createScheduleOrderRequest(1, 2),
                        createScheduleOrderRequest(4, 3)
                )
        );

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(scheduleRepository.findByDayNoAndTimeSlotOrderBySortOrderAscForUpdate(dayNo, TimeSlot.MORNING))
                .thenReturn(List.of(scheduleA, scheduleB, scheduleC, scheduleD));

//        when
        List<ScheduleResponseDto> responseDtos = scheduleService.reorderSchedules(userNo, dayNo, requestDto);

//        then
        assertThat(scheduleB.getSortOrder()).isEqualTo(4);
        assertThat(scheduleC.getSortOrder()).isEqualTo(1);
        assertThat(scheduleA.getSortOrder()).isEqualTo(2);
        assertThat(scheduleD.getSortOrder()).isEqualTo(3);

        assertThat(responseDtos)
                .extracting(ScheduleResponseDto::getScheduleNo)
                .containsExactly(3, 1, 4, 2);

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(scheduleRepository).findByDayNoAndTimeSlotOrderBySortOrderAscForUpdate(dayNo, TimeSlot.MORNING);
    }

    @Test
    @DisplayName("Schedule 재정렬 요청시 올바르지 않은 ScheduleNo가 있으면 예외를 발생한다")
    void reorder_schedule_scheduleNo_fail() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Place placeA = createPlace(record, 1, "카페A");
        Place placeB = createPlace(record, 2, "카페B");

        SchedulePlace scheduleA = createSchedule(recordDay, placeA, 1, TimeSlot.MORNING, 1);
        SchedulePlace scheduleB = createSchedule(recordDay, placeB, 2, TimeSlot.MORNING, 2);

        ScheduleReorderRequestDto requestDto = createScheduleReorderRequest(
                TimeSlot.MORNING,
                List.of(
                        createScheduleOrderRequest(1, 1),
                        createScheduleOrderRequest(999, 2)
                )
        );

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(scheduleRepository.findByDayNoAndTimeSlotOrderBySortOrderAscForUpdate(dayNo, TimeSlot.MORNING))
                .thenReturn(List.of(scheduleA, scheduleB));

//        when, then
        assertThatThrownBy(() ->
                scheduleService.reorderSchedules(userNo, dayNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.SCHEDULE_INVALID_REORDER.getMessage());

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
    }

    @Test
    @DisplayName("Schedule 재정렬 요청시 중복되는 ScheduleNo가 있으면 예외가 발생한다")
    void reorder_schedule_distinct_scheduleNo_fail() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Place placeA = createPlace(record, 1, "카페A");
        Place placeB = createPlace(record, 2, "카페B");

        SchedulePlace scheduleA = createSchedule(recordDay, placeA, 1, TimeSlot.MORNING, 1);
        SchedulePlace scheduleB = createSchedule(recordDay, placeB, 2, TimeSlot.MORNING, 2);

        ScheduleReorderRequestDto requestDto = createScheduleReorderRequest(
                TimeSlot.MORNING,
                List.of(
                        createScheduleOrderRequest(1, 1),
                        createScheduleOrderRequest(1, 2)
                )
        );

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(scheduleRepository.findByDayNoAndTimeSlotOrderBySortOrderAscForUpdate(dayNo, TimeSlot.MORNING))
                .thenReturn(List.of(scheduleA, scheduleB));

//        when, then
        assertThatThrownBy(() ->
                scheduleService.reorderSchedules(userNo, dayNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.SCHEDULE_INVALID_REORDER.getMessage());

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
    }

    @Test
    @DisplayName("Schedule 재정렬 요청시 중복되는 Sort가 있으면 예외가 발생한다")
    void reorder_schedule_distinct_sort_fail() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer dayNo = 1;

        User user = createUser();
        Record record = createRecord(user, recordNo);
        RecordDay recordDay = createRecordDay(record, dayNo);

        Place placeA = createPlace(record, 1, "카페A");
        Place placeB = createPlace(record, 2, "카페B");

        SchedulePlace scheduleA = createSchedule(recordDay, placeA, 1, TimeSlot.MORNING, 1);
        SchedulePlace scheduleB = createSchedule(recordDay, placeB, 2, TimeSlot.MORNING, 2);

        ScheduleReorderRequestDto requestDto = createScheduleReorderRequest(
                TimeSlot.MORNING,
                List.of(
                        createScheduleOrderRequest(1, 1),
                        createScheduleOrderRequest(2, 1)
                )
        );

        when(recordDayRepository.findById(dayNo))
                .thenReturn(Optional.of(recordDay));
        when(scheduleRepository.findByDayNoAndTimeSlotOrderBySortOrderAscForUpdate(dayNo, TimeSlot.MORNING))
                .thenReturn(List.of(scheduleA, scheduleB));

//        when, then
        assertThatThrownBy(() ->
                scheduleService.reorderSchedules(userNo, dayNo, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.SCHEDULE_INVALID_REORDER.getMessage());

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
    }

    private User createUser() {
        return User.builder()
                .email("test@test.com")
                .userName("test")
                .password("test12345")
                .build();
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

    private Place createPlace(Record record, Integer placeNo, String placeName) {
        Place place = Place.builder()
                .record(record)
                .placeName(placeName)
                .placeAddress("제주도")
                .latitude(new BigDecimal("37.566610"))
                .longitude(new BigDecimal("126.978403"))
                .mapSource("KAKAO")
                .mapPlaceId("12345")
                .imageUrl("image.jpg")
                .build();
        ReflectionTestUtils.setField(place, "placeNo", placeNo);
        return place;
    }

    private SchedulePlace createSchedule(RecordDay recordDay, Place place, Integer scheduleNo, TimeSlot timeSlot, Integer sortOrder) {
        SchedulePlace schedulePlace = SchedulePlace.builder()
                .day(recordDay)
                .place(place)
                .timeSlot(timeSlot)
                .sortOrder(sortOrder)
                .build();
        ReflectionTestUtils.setField(schedulePlace, "scheduleNo", scheduleNo);
        return schedulePlace;
    }

    private ScheduleRequestDto createScheduleRequest(Integer placeNo, TimeSlot timeSlot, Integer sortOrder) {
        ScheduleRequestDto requestDto = new ScheduleRequestDto();

        ReflectionTestUtils.setField(requestDto, "placeNo", placeNo);
        ReflectionTestUtils.setField(requestDto, "timeSlot", timeSlot);
        ReflectionTestUtils.setField(requestDto, "sortOrder", sortOrder);
        return requestDto;
    }

    private ScheduleOrderRequestDto createScheduleOrderRequest(Integer scheduleNo, Integer sortOrder) {
        ScheduleOrderRequestDto requestDto = new ScheduleOrderRequestDto();

        ReflectionTestUtils.setField(requestDto, "scheduleNo", scheduleNo);
        ReflectionTestUtils.setField(requestDto, "sortOrder", sortOrder);
        return requestDto;
    }

    private ScheduleReorderRequestDto createScheduleReorderRequest(TimeSlot timeSlot, List<ScheduleOrderRequestDto> schedules) {
        ScheduleReorderRequestDto requestDto = new ScheduleReorderRequestDto();

        ReflectionTestUtils.setField(requestDto, "timeSlot", timeSlot);
        ReflectionTestUtils.setField(requestDto, "schedules", schedules);
        return requestDto;
    }
}
