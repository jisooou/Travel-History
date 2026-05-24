package com.project.travel.place.repository;

import com.project.travel.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Integer> {
    List<Place> findByRecord_RecordNo(Integer recordNo);

    Optional<Place> findByRecord_RecordNoAndMapSourceAndMapPlaceId(Integer recordNo, String mapSource, String mapPlaceId);
}
