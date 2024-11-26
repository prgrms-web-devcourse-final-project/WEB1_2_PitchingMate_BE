package com.example.mate.domain.goods.vo;

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

    @Builder
    public Location(String addressName, String placeName, String roadAddressName) {
        this.addressName = addressName;
        this.placeName = placeName;
        this.roadAddressName = roadAddressName;
    }
}
