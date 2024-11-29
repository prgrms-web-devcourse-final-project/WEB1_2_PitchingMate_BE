package com.example.mate.domain.match.dto.response;

import com.example.mate.domain.match.util.WeekCalculator;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyMatchesResponse {
    private int weekNumber;
    private String weekLabel;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<MatchResponse> matches;

    @Builder
    public WeeklyMatchesResponse(int weekNumber, String weekLabel,
                                 LocalDate weekStartDate, LocalDate weekEndDate,
                                 List<MatchResponse> matches) {
        this.weekNumber = weekNumber;
        this.weekLabel = weekLabel;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.matches = matches;
    }

    public static WeeklyMatchesResponse of(
            int weekNumber,
            LocalDate weekStart,
            List<MatchResponse> matches
    ) {
        return WeeklyMatchesResponse.builder()
                .weekNumber(weekNumber)
                .weekLabel(WeekCalculator.generateWeekLabel(weekStart))
                .weekStartDate(weekStart)
                .weekEndDate(weekStart.plusDays(6))
                .matches(matches)
                .build();
    }
}