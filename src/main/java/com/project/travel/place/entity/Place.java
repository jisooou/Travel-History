package com.project.travel.place.entity;

import com.project.travel.record.entity.Record;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "place",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_record_map_place",
                        columnNames = {"RECORD_NO", "MAP_SOURCE", "MAP_PLACE_ID"}
                )
        },
        indexes = {
                @Index(name = "idx_place_record", columnList = "RECORD_NO")
        }
)
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLACE_NO")
    private Integer placeNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "RECORD_NO",
            nullable = false,
            referencedColumnName = "RECORD_NO",
            foreignKey = @ForeignKey(name = "fk_place_record")
    )
    private Record record;

    @Column(name = "PLACE_NAME", length = 50, nullable = false)
    private String placeName;

    @Column(name = "PLACE_ADDRESS", length = 255)
    private String placeAddress;

    @Column(name = "LATITUDE", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", precision = 10, scale = 8, nullable = false)
    private BigDecimal longitude;

    @Column(name = "MAP_SOURCE", length = 30)
    private String mapSource;

    @Column(name = "MAP_PLACE_ID", length = 255)
    private String mapPlaceId;

    @Column(name = "IMAGE_URL", length = 500)
    private String imageUrl;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Place(
            Record record,
            String placeName,
            String placeAddress,
            BigDecimal latitude,
            BigDecimal longitude,
            String mapSource,
            String mapPlaceId,
            String imageUrl
    ) {
        this.record = record;
        this.placeName = placeName;
        this.placeAddress = placeAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.mapSource = mapSource;
        this.mapPlaceId = mapPlaceId;
        this.imageUrl = imageUrl;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
