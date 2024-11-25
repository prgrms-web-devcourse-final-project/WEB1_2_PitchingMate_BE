package com.example.mate.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StadiumResponse {
    @Getter
    @Builder
    public static class Info {
        private Long id;
        private String stadiumName;
        private String location;
        private String longitude;
        private String latitude;
    }
}