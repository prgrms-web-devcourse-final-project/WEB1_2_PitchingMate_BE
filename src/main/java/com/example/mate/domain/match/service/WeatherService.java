package com.example.mate.domain.match.service;

import com.example.mate.domain.match.api.WeatherApiClient;
import com.example.mate.domain.match.dto.response.WeatherApiResponse;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.Weather;
import com.example.mate.domain.match.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private final MatchRepository matchRepository;
    private final WeatherApiClient weatherApiClient;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateWeatherForUpcomingMatches() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxForecastTime = now.plusDays(5);

        List<Match> upcomingMatches = matchRepository.findUpcomingMatches(now, maxForecastTime);

        for (Match match : upcomingMatches) {
            try {
                WeatherApiResponse weatherInfo = weatherApiClient.getWeather(
                        match.getStadium().latitude,
                        match.getStadium().longitude,
                        match.getMatchTime()
                );

                Weather weather = match.getWeather();
                if (weather == null) {
                    weather = Weather.builder()
                            .temperature(weatherInfo.getTemperature())
                            .pop(weatherInfo.getPop())
                            .cloudiness(weatherInfo.getCloudiness())
                            .wtTime(weatherInfo.getWtTime())
                            .build();
                    match.updateWeather(weather);
                } else {
                    weather.update(
                            weatherInfo.getTemperature(),
                            weatherInfo.getPop(),
                            weatherInfo.getCloudiness(),
                            weatherInfo.getWtTime()
                    );
                }
                log.debug("Updated weather for match id: {}", match.getId());
            } catch (Exception e) {
                log.error("Failed to update weather for match id: {}", match.getId(), e);
            }
        }
    }
}