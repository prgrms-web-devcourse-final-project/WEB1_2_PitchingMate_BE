package com.example.mate.domain.match.dto.response;

import com.example.mate.domain.match.entity.Weather;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeatherResponse {
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Info {
        private Float temperature;
        private Float pop;
        private Integer cloudiness;
        private LocalDateTime wtTime;

        @Builder
        private Info(Weather weather) {
            this.temperature = weather.getTemperature();
            this.pop = weather.getPop();
            this.cloudiness = weather.getCloudiness();
            this.wtTime = weather.getWtTime();
        }

        public static Info from(Weather weather) {
            if (weather == null) return null;
            return new Info(weather);
        }
    }
}