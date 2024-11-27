package com.example.mate.domain.goods.dto;

import com.example.mate.domain.goods.entity.Location;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class LocationInfo {

    @NotEmpty(message = "지번 주소는 필수 입력 값입니다.")
    private String addressName;

    @NotEmpty(message = "장소 이름은 필수 입력 값입니다.")
    private String placeName;

    @NotEmpty(message = "도로명 주소는 필수 입력 값입니다.")
    private String roadAddressName;

    @NotEmpty(message = "경도는 필수 입력 값입니다.")
    private String longitude;

    @NotEmpty(message = "위도는 필수 입력 값입니다.")
    private String latitude;

    @Builder
    public LocationInfo(String addressName, String placeName, String roadAddressName, String longitude, String latitude) {
        this.addressName = addressName;
        this.placeName = placeName;
        this.roadAddressName = roadAddressName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static Location toEntity(LocationInfo locationInfo) {
        return Location.builder()
                .addressName(locationInfo.getAddressName())
                .placeName(locationInfo.getPlaceName())
                .roadAddressName(locationInfo.getRoadAddressName())
                .longitude(locationInfo.getLongitude())
                .latitude(locationInfo.getLatitude())
                .build();
    }

    public static LocationInfo from(Location location) {
        return LocationInfo.builder()
                .addressName(location.getAddressName())
                .placeName(location.getPlaceName())
                .roadAddressName(location.getRoadAddressName())
                .longitude(location.getLongitude())
                .latitude(location.getLatitude())
                .build();
    }
}
