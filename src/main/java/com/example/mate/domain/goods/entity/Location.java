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

    @Column(name = "address_name", nullable = false, length = 100)
    private String addressName;

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column(name = "road_address_name", nullable = false, length = 100)
    private String roadAddressName;

    @Column(name = "longitude", nullable = false, length = 20)
    private String longitude;

    @Column(name = "latitude", nullable = false, length = 20)
    private String latitude;

    @Builder
    public Location(String addressName, String placeName, String roadAddressName, String longitude, String latitude) {
        this.addressName = addressName;
        this.placeName = placeName;
        this.roadAddressName = roadAddressName;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
