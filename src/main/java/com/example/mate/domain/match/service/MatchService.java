package com.example.mate.domain.match.service;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.MatchResponse;
import com.example.mate.domain.match.dto.response.WeeklyMatchesResponse;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.match.util.WeekCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;

    private static final int WEEKS_TO_FETCH = 4;

    public List<MatchResponse> getMainBannerMatches() {
        return matchRepository.findTop5ByOrderByMatchTimeDesc().stream()
                .filter(match -> match.getMatchTime().isAfter(LocalDateTime.now()))
                .map(match -> MatchResponse.from(match, null))
                .collect(Collectors.toList());
    }


    public List<MatchResponse> getTeamMatches(Long teamId) {
        TeamInfo.getById(teamId);

        return matchRepository.findTop3ByHomeTeamIdOrAwayTeamIdOrderByMatchTimeDesc(teamId, teamId).stream()
                .filter(match -> match.getMatchTime().isAfter(LocalDateTime.now()))
                .map(match -> MatchResponse.from(match, teamId))
                .collect(Collectors.toList());
    }

    public List<MatchResponse> getTeamCompletedMatches(Long teamId) {
        TeamInfo.getById(teamId);

        return matchRepository.findRecentCompletedMatches(
                        MatchStatus.COMPLETED, teamId,
                        MatchStatus.COMPLETED, teamId)
                .stream()
                .map(match -> MatchResponse.from(match, teamId))
                .collect(Collectors.toList());
    }

    // 특정 팀의 주차별 경기 일정 조회
    public List<WeeklyMatchesResponse> getTeamWeeklyMatches(Long teamId, LocalDate queryDate) {
        TeamInfo.getById(teamId);

        // 현재 주의 월요일과 4주 후의 날짜 계산
        LocalDate currentWeekStart = WeekCalculator.getWeekStart(queryDate);
        LocalDate endDate = WeekCalculator.calculateEndDate(currentWeekStart, WEEKS_TO_FETCH);

        // 경기 조회 및 주차별 그룹화
        List<Match> matches = fetchMatchesForPeriod(teamId, queryDate, endDate);
        return createWeeklyResponses(matches, queryDate, currentWeekStart, teamId);
    }

    // 특정 기간 동안의 팀 경기를 조회
    private List<Match> fetchMatchesForPeriod(Long teamId, LocalDate queryDate, LocalDate endDate) {
        return matchRepository.findTeamMatchesInPeriod(
                teamId,
                queryDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );
    }

    // 조회된 경기들을 주차별로 그룹화
    private List<WeeklyMatchesResponse> createWeeklyResponses(
            List<Match> matches,
            LocalDate queryDate,
            LocalDate weekStart,
            Long teamId
    ) {
        List<WeeklyMatchesResponse> responses = new ArrayList<>();

        for (int weekIndex = 0; weekIndex < WEEKS_TO_FETCH; weekIndex++) {
            LocalDate currentWeekStart = weekStart.plusWeeks(weekIndex);
            // 해당 주차의 경기 필터링
            List<MatchResponse> weekMatches = filterMatchesForWeek(
                    matches, currentWeekStart, queryDate, weekIndex, teamId);

            // 주차별 응답 객체 생성
            responses.add(WeeklyMatchesResponse.of(
                    weekIndex + 1,
                    currentWeekStart,
                    weekMatches
            ));
        }

        return responses;
    }

    // 특정 주차에 해당하는 경기만 필터링
    private List<MatchResponse> filterMatchesForWeek(
            List<Match> matches,
            LocalDate weekStart,
            LocalDate queryDate,
            int weekIndex,
            Long teamId
    ) {
        LocalDate weekEnd = weekStart.plusDays(6);

        return matches.stream()
                .filter(match -> isMatchInWeekPeriod(match, weekStart, weekEnd, queryDate, weekIndex))
                .sorted(Comparator.comparing(Match::getMatchTime))
                .map(match -> MatchResponse.from(match, teamId))
                .collect(Collectors.toList());
    }

    // 경기가 해당 주차에 속하는지 판단
    private boolean isMatchInWeekPeriod(
            Match match,
            LocalDate weekStart,
            LocalDate weekEnd,
            LocalDate queryDate,
            int weekIndex
    ) {
        LocalDate matchDate = match.getMatchTime().toLocalDate();

        // 조회 시작일부터 해당 주의 일요일까지의 경기
        if (weekIndex == 0) {
            return !matchDate.isBefore(queryDate) && !matchDate.isAfter(weekEnd);
        }

        // 2주차 ~ 4주차
        return !matchDate.isBefore(weekStart) && !matchDate.isAfter(weekEnd);
    }
}

