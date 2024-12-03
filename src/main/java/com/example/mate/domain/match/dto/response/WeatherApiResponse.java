package com.example.mate.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WeatherApiResponse {
    private Float temperature;    // 온도
    private Float pop;           // 강수확률
    private Integer cloudiness;  // 구름량
    private LocalDateTime wtTime; // 날씨 예보 시간
}