package com.example.mate.domain.match.api;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.match.dto.response.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherApiClient {
    private final RestTemplate restTemplate;

    @Value("${openweather.api.key}")
    private String apiKey;

    public WeatherApiResponse getWeather(String latitude, String longitude, LocalDateTime dateTime) {
        try {
            log.debug("Fetching weather for lat: {}, lon: {}, time: {}", latitude, longitude, dateTime);
            
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.openweathermap.org/data/2.5/forecast")
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .build()
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("list")) {
                throw new CustomException(ErrorCode.WEATHER_API_ERROR);
            }

            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("list");
            Map<String, Object> weatherData = findClosestWeatherData(weatherList, dateTime);
            Map<String, Object> main = (Map<String, Object>) weatherData.get("main");
            Map<String, Object> clouds = (Map<String, Object>) weatherData.get("clouds");

            long timestamp = ((Number) weatherData.get("dt")).longValue();
            LocalDateTime forecastTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp),
                    ZoneId.of("Asia/Seoul")
            );

            log.debug("Weather data fetched successfully. Forecast time: {}", forecastTime);
            return WeatherApiResponse.builder()
                    .temperature(((Number) main.get("temp")).floatValue())
                    .pop(((Number) weatherData.get("pop")).floatValue() * 100)
                    .cloudiness((Integer) clouds.get("all"))
                    .wtTime(forecastTime)
                    .build();
        } catch (Exception e) {
            log.error("Weather API call failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.WEATHER_API_ERROR);
        }
    }

    private Map<String, Object> findClosestWeatherData(List<Map<String, Object>> weatherList, LocalDateTime targetTime) {
        if (weatherList == null || weatherList.isEmpty()) {
            throw new CustomException(ErrorCode.WEATHER_DATA_NOT_FOUND);
        }

        long targetEpoch = targetTime.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();
        return weatherList.stream()
                .min(Comparator.comparingLong(data ->
                        Math.abs(((Number) data.get("dt")).longValue() - targetEpoch)))
                .orElseThrow(() -> new CustomException(ErrorCode.WEATHER_DATA_NOT_FOUND));
    }
}