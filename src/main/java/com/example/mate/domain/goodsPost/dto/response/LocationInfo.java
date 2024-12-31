package com.example.mate.domain.goodsPost.dto.response;

import com.example.mate.domain.goodsPost.entity.Location;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class LocationInfo {

    @NotEmpty(message = "장소 이름은 필수 입력 값입니다.")
    private String placeName;

    @NotEmpty(message = "경도는 필수 입력 값입니다.")
    private String longitude;

    @NotEmpty(message = "위도는 필수 입력 값입니다.")
    private String latitude;

    @Builder
    public LocationInfo(String placeName, String longitude, String latitude) {
        this.placeName = placeName;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static Location toEntity(LocationInfo locationInfo) {
        return Location.builder()
                .placeName(locationInfo.getPlaceName())
                .longitude(locationInfo.getLongitude())
                .latitude(locationInfo.getLatitude())
                .build();
    }

    public static LocationInfo from(Location location) {
        return LocationInfo.builder()
                .placeName(location.getPlaceName())
                .longitude(location.getLongitude())
                .latitude(location.getLatitude())
                .build();
    }
}
