package com.example.mate.domain.goods.vo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Location {

    private String addressName;
    private String placeName;
    private String roadAddressName;

    @Builder
    public Location(String addressName, String placeName, String roadAddressName) {
        this.addressName = addressName;
        this.placeName = placeName;
        this.roadAddressName = roadAddressName;
    }
}
