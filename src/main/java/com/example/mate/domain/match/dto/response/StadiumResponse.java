package com.example.mate.domain.match.dto.response;

import com.example.mate.domain.constant.StadiumInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StadiumResponse {
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Info {
        private Long id;
        private String stadiumName;
        private String location;
        private String longitude;
        private String latitude;

        @Builder
        private Info(StadiumInfo.Stadium stadium) {
            this.id = stadium.id;
            this.stadiumName = stadium.name;
            this.location = stadium.location;
            this.longitude = stadium.longitude;
            this.latitude = stadium.latitude;
        }

        public static Info from(StadiumInfo.Stadium stadium) {
            return Info.builder()
                    .stadium(stadium)
                    .build();
        }
    }
}