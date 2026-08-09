package com.project.travel.place.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.place.dto.request.PlaceRequestDto;
import com.project.travel.place.dto.response.PlaceResponseDto;
import com.project.travel.place.entity.Place;
import com.project.travel.place.repository.PlaceRepository;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.TravelType;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PlaceServiceTest {
    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private CollabAuthorityService collabAuthorityService;

    @InjectMocks
    private PlaceService placeService;

    @Test
    @DisplayName("검색한 장소를 Record에 추가하는 데에 성공한다")
    void add_place_to_record_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer placeNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        PlaceRequestDto requestDto = createPlaceRequest(
                "에버랜드",
                "용인",
                new BigDecimal("37.566610"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "1234",
                null
        );

        Place savedPlace = createPlace(
                record,
                placeNo,
                "에버랜드",
                "용인",
                new BigDecimal("37.566610"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "1234",
                null
        );

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(placeRepository.findByRecord_RecordNoAndMapSourceAndMapPlaceId(
                recordNo,
                "KAKAO",
                "1234"
        )).thenReturn(Optional.empty());

        when(placeRepository.save(any(Place.class)))
                .thenReturn(savedPlace);

//        when
        PlaceResponseDto responseDto = placeService.addPlaceToRecord(userNo, recordNo, requestDto);

//        then
        assertThat(responseDto.getPlaceNo()).isEqualTo(placeNo);
        assertThat(responseDto.getRecordNo()).isEqualTo(recordNo);
        assertThat(responseDto.getPlaceName()).isEqualTo("에버랜드");
        assertThat(responseDto.getPlaceAddress()).isEqualTo("용인");
        assertThat(responseDto.getLatitude()).isEqualTo(new BigDecimal("37.566610"));
        assertThat(responseDto.getLongitude()).isEqualTo(new BigDecimal("126.978403"));
        assertThat(responseDto.getMapSource()).isEqualTo("KAKAO");
        assertThat(responseDto.getMapPlaceId()).isEqualTo("1234");

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(placeRepository).save(any(Place.class));
    }

    @Test
    @DisplayName("이미 Record에 저장된 장소이면 기존 장소를 반환하는 데에 성공한다")
    void add_place_to_record_exist_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer placeNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);
        PlaceRequestDto requestDto = createPlaceRequest(
                "에버랜드",
                "용인",
                new BigDecimal("37.566610"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "1234",
                null
        );

        Place existPlace = createPlace(
                record,
                placeNo,
                "에버랜드",
                "용인",
                new BigDecimal("37.566610"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "1234",
                null
        );

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(placeRepository.findByRecord_RecordNoAndMapSourceAndMapPlaceId(
                recordNo,
                "KAKAO",
                "1234"
        )).thenReturn(Optional.of(existPlace));

//        when
        PlaceResponseDto responseDto = placeService.addPlaceToRecord(userNo, recordNo, requestDto);

//        then
        assertThat(responseDto.getPlaceNo()).isEqualTo(placeNo);
        assertThat(responseDto.getRecordNo()).isEqualTo(recordNo);
        assertThat(responseDto.getPlaceName()).isEqualTo("에버랜드");
        assertThat(responseDto.getPlaceAddress()).isEqualTo("용인");
        assertThat(responseDto.getLatitude()).isEqualTo(new BigDecimal("37.566610"));
        assertThat(responseDto.getLongitude()).isEqualTo(new BigDecimal("126.978403"));
        assertThat(responseDto.getMapSource()).isEqualTo("KAKAO");
        assertThat(responseDto.getMapPlaceId()).isEqualTo("1234");

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(placeRepository, never()).save(any(Place.class));
    }

    @Test
    @DisplayName("회원 Record의 장소를 조회하는 데에 성공한다")
    void get_user_place_of_record_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);

        Place placeA = createPlace(
                record,
                1,
                "카페A",
                "용인",
                new BigDecimal("37.566610"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "1234",
                null
        );
        Place placeB = createPlace(
                record,
                2,
                "카페B",
                "용인",
                new BigDecimal("37.457110"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "5678",
                null
        );

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(placeRepository.findByRecord_RecordNo(recordNo))
                .thenReturn(List.of(placeA, placeB));

//        when
        List<PlaceResponseDto> responseDtos = placeService.getUserPlaceOfRecord(userNo, recordNo);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(PlaceResponseDto::getPlaceName)
                .containsExactly("카페A", "카페B");

        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(placeRepository).findByRecord_RecordNo(recordNo);
    }

    @Test
    @DisplayName("비회원 Record의 장소를 조회하는 데에 성공한다")
    void get_guest_place_of_record_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        String joinCode = "ABCD1234";

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);

        Place placeA = createPlace(
                record,
                1,
                "카페A",
                "용인",
                new BigDecimal("37.566610"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "1234",
                null
        );
        Place placeB = createPlace(
                record,
                2,
                "카페B",
                "용인",
                new BigDecimal("37.457110"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "5678",
                null
        );

        when(recordRepository.findById(recordNo))
                .thenReturn(Optional.of(record));
        when(placeRepository.findByRecord_RecordNo(recordNo))
                .thenReturn(List.of(placeA, placeB));

//        when
        List<PlaceResponseDto> responseDtos = placeService.getGuestPlaceOfRecord(recordNo, joinCode);

//        then
        assertThat(responseDtos).hasSize(2);
        assertThat(responseDtos)
                .extracting(PlaceResponseDto::getPlaceName)
                .containsExactly("카페A", "카페B");

        verify(collabAuthorityService).checkGuest(recordNo, joinCode);
        verify(placeRepository).findByRecord_RecordNo(recordNo);
    }

    @Test
    @DisplayName("Record에 넣은 장소를 삭제하는 데에 성공한다")
    void delete_place_of_record_success() {
//        given
        Integer userNo = 1;
        Integer recordNo = 1;
        Integer placeNo = 1;

        User user = createUser(userNo);
        Record record = createRecord(user, recordNo);

        Place place = createPlace(
                record,
                placeNo,
                "에버랜드",
                "용인",
                new BigDecimal("37.566610"),
                new BigDecimal("126.978403"),
                "KAKAO",
                "1234",
                null
        );

        when(placeRepository.findById(placeNo))
                .thenReturn(Optional.of(place));

//        when
        placeService.deletePlace(userNo, placeNo);

//        then
        verify(collabAuthorityService).checkMemberEditor(recordNo, userNo);
        verify(placeRepository).delete(place);
    }

    private User createUser(Integer userNo) {
        User user = User.builder()
                .email("user@test.com")
                .userName("user")
                .password("abcd1234")
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

    private Place createPlace(
            Record record,
            Integer placeNo,
            String placeName,
            String placeAddress,
            BigDecimal latitude,
            BigDecimal longitude,
            String mapSource,
            String mapPlaceId,
            String imageUrl
    ) {
        Place place = Place.builder()
                .record(record)
                .placeName(placeName)
                .placeAddress(placeAddress)
                .latitude(latitude)
                .longitude(longitude)
                .mapSource(mapSource)
                .mapPlaceId(mapPlaceId)
                .imageUrl(imageUrl)
                .build();
        ReflectionTestUtils.setField(place, "placeNo", placeNo);
        return place;
    }

    private PlaceRequestDto createPlaceRequest(
            String placeName,
            String placeAddress,
            BigDecimal latitude,
            BigDecimal longitude,
            String mapSource,
            String mapPlaceId,
            String imageUrl
    ) {
        PlaceRequestDto requestDto = new PlaceRequestDto();

        ReflectionTestUtils.setField(requestDto, "placeName", placeName);
        ReflectionTestUtils.setField(requestDto, "placeAddress", placeAddress);
        ReflectionTestUtils.setField(requestDto, "latitude", latitude);
        ReflectionTestUtils.setField(requestDto, "longitude", longitude);
        ReflectionTestUtils.setField(requestDto, "mapSource", mapSource);
        ReflectionTestUtils.setField(requestDto, "mapPlaceId", mapPlaceId);
        ReflectionTestUtils.setField(requestDto, "imageUrl", imageUrl);
        return requestDto;
    }

}
