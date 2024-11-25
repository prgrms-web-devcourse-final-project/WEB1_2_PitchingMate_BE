package com.example.mate.domain.match.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class WeatherResponse {
    @Getter
    @Builder
    public static class Info {
        private Float temperature;
        private Float pop;
        private Integer cloudiness;
        private LocalDateTime wtTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        public LocalDateTime getWtTime() {
            return wtTime;
        }
    }
}