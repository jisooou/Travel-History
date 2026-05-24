package com.project.travel.place.service;

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

    @Transactional
    public PlaceResponseDto addPlaceToRecord(Integer userNo, Integer recordNo, @Valid PlaceRequestDto requestDto) {
        Record record = getMyRecord(userNo, recordNo);

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
        getMyRecord(userNo, recordNo);
        return placeRepository.findByRecord_RecordNo(recordNo)
                .stream()
                .map(PlaceResponseDto::from)
                .toList();
    }

    @Transactional
    public void deletePlace(Integer userNo, Integer placeNo) {
        Place place = getMyPlace(userNo, placeNo);
        placeRepository.delete(place);
    }

    private Record getMyRecord(Integer userNo, Integer recordNo) {
        Record record = recordRepository.findById(recordNo)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        if (!record.getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.RECORD_ACCESS_DENIED);
        }
        return record;
    }

    private Place getMyPlace(Integer userNo, Integer placeNo) {
        Place place = placeRepository.findById(placeNo)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));
        if (!place.getRecord().getOwner().getUserNo().equals(userNo)) {
            throw new CustomException(ErrorCode.PLACE_ACCESS_DENIED);
        }
        return place;
    }
}
