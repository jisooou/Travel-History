package com.project.travel.place.service;

import com.project.travel.collab.service.CollabAuthorityService;
import com.project.travel.global.exception.CustomException;
import com.project.travel.global.exception.ErrorCode;
import com.project.travel.place.dto.request.PlaceRequestDto;
import com.project.travel.place.dto.response.PlaceResponseDto;
import com.project.travel.place.entity.Place;
import com.project.travel.place.repository.PlaceRepository;
import com.project.travel.record.entity.Record;
import com.project.travel.record.repository.RecordRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final RecordRepository recordRepository;
    private final CollabAuthorityService collabAuthorityService;

    @Transactional
    public PlaceResponseDto addPlaceToRecord(Integer userNo, Integer recordNo, @Valid PlaceRequestDto requestDto) {
        Record record = getAccessRecord(recordNo);
        collabAuthorityService.checkEditable(recordNo, userNo);

        Place place = placeRepository
                .findByRecord_RecordNoAndMapSourceAndMapPlaceId(
                        recordNo,
                        requestDto.getMapSource(),
                        requestDto.getMapPlaceId()
                )
                .orElseGet(() -> placeRepository.save(
                                Place.builder()
                                        .record(record)
                                        .placeName(requestDto.getPlaceName())
                                        .placeAddress(requestDto.getPlaceAddress())
                                        .latitude(requestDto.getLatitude())
                                        .longitude(requestDto.getLongitude())
                                        .mapSource(requestDto.getMapSource())
                                        .mapPlaceId(requestDto.getMapPlaceId())
                                        .imageUrl(requestDto.getImageUrl())
                                        .build()
                        )
                );
        return PlaceResponseDto.from(place);
    }

    public List<PlaceResponseDto> getPlaceOfRecord(Integer userNo, Integer recordNo) {
        getAccessRecord(recordNo);
        collabAuthorityService.checkViewable(recordNo, userNo);

        return placeRepository.findByRecord_RecordNo(recordNo)
                .stream()
                .map(PlaceResponseDto::from)
                .toList();
    }

    @Transactional
    public void deletePlace(Integer userNo, Integer placeNo) {
        Place place = getAccessPlace(placeNo);
        Integer recordNo = place.getRecord().getRecordNo();

        collabAuthorityService.checkEditable(recordNo, userNo);

        placeRepository.delete(place);
    }

    private Record getAccessRecord(Integer recordNo) {
        return recordRepository.findById(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
    }

    private Place getAccessPlace(Integer placeNo) {
        return placeRepository.findById(placeNo)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
    }
}
