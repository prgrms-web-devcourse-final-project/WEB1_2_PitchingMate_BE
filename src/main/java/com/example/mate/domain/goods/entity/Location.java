package com.example.mate.domain.goods.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Embeddable
public class Location {

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column(name = "longitude", nullable = false, length = 20)
    private String longitude;

    @Column(name = "latitude", nullable = false, length = 20)
    private String latitude;

    @Builder
    public Location(String placeName, String longitude, String latitude) {
        this.placeName = placeName;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
