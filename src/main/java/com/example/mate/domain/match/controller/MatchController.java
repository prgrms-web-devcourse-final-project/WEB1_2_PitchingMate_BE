package com.example.mate.domain.match.controller;

import com.example.mate.domain.match.dto.response.*;
import com.example.mate.domain.match.model.TeamInfo;
import com.example.mate.global.common.response.ApiResponse;
import com.example.mate.global.exception.CustomException;
import com.example.mate.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/matches")
@Tag(name = "Match", description = "경기 관련 API")
@RequiredArgsConstructor
public class MatchController {
    private static final int DEFAULT_ALL_MATCHES_LIMIT = 5;
    private static final int DEFAULT_MY_TEAM_MATCHES_LIMIT = 3;
    private static final int DEFAULT_HOME_GAMES = 2;
    private static final int DEFAULT_AWAY_GAMES = 2;
    private static final int MATCH_HOUR = 18;
    private static final int MATCH_MINUTE = 30;

    private static final float MIN_TEMP = 15.0f;
    private static final float MAX_TEMP = 30.0f;
    private static final int MAX_CLOUDINESS = 100;

    private static final List<TeamInfo> TEAM_LIST = Arrays.asList(
            new TeamInfo(1L, "KIA 타이거즈", "광주-기아 챔피언스 필드"),
            new TeamInfo(2L, "LG 트윈스", "잠실야구장"),
            new TeamInfo(3L, "NC 다이노스", "창원NC파크"),
            new TeamInfo(4L, "SSG 랜더스", "인천SSG랜더스필드"),
            new TeamInfo(5L, "kt wiz", "수원 kt wiz 파크"),
            new TeamInfo(6L, "두산 베어스", "잠실야구장"),
            new TeamInfo(7L, "롯데 자이언츠", "사직야구장"),
            new TeamInfo(8L, "삼성 라이온즈", "대구삼성라이온즈파크"),
            new TeamInfo(9L, "키움 히어로즈", "고척스카이돔"),
            new TeamInfo(10L, "한화 이글스", "한화생명이글스파크")
    );

    @Operation(summary = "경기 조회", description = "경기 일정을 조회합니다. 팀ID 지정 시 해당 팀 경기만 조회됩니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchResponse.Detail>>> getMatches(
            @Parameter(description = "팀 ID")
            @RequestParam(required = false) Long teamId,

            @Parameter(description = "경기 상태 (SCHEDULED: 예정, COMPLETED: 종료, CANCELED: 취소)")
            @RequestParam(required = false) String status,

            @Parameter(description = "조회할 경기 수", example = "5")
            @RequestParam(defaultValue = "5") @Positive int limit) {

        // 10개의 랜덤 경기 생성
        List<MatchResponse.Detail> matches = createRandomMatches(10);

        // 팀ID로 필터링
        matches = filterMatchesByTeam(matches, teamId);

        // 상태로 필터링
        matches = filterMatchesByStatus(matches, status);

        // 정렬 및 개수 제한하여 반환
        return ResponseEntity.ok(ApiResponse.success(
                matches.stream()
                        .sorted(Comparator.comparing(MatchResponse.Detail::getMatchTime))
                        .limit(Math.min(limit, DEFAULT_ALL_MATCHES_LIMIT))
                        .collect(Collectors.toList())
        ));
    }

    @Operation(summary = "특정 팀의 경기 결과 조회", description = "특정 팀의 최근 경기 결과를 조회합니다.")
    @GetMapping("/{teamId}/results")
    public ResponseEntity<ApiResponse<List<MatchResponse.Simple>>> getTeamResults(
            @PathVariable @Positive Long teamId,
            @RequestParam(defaultValue = "2") @Positive int limit) {

        TeamInfo team = getTeamById(teamId);
        List<MatchResponse.Simple> matches = createTeamResults(team);

        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    private List<MatchResponse.Detail> createRandomMatches(int count) {
        List<MatchResponse.Detail> matches = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            TeamInfo homeTeam = getRandomTeam();
            TeamInfo awayTeam;
            do {
                awayTeam = getRandomTeam();
            } while (homeTeam.getId().equals(awayTeam.getId()));

            matches.add(MatchResponse.Detail.builder()
                    .id((long) i)
                    .homeTeam(createTeamSimple(homeTeam))
                    .awayTeam(createTeamSimple(awayTeam))
                    .location(homeTeam.getStadium())
                    .matchTime(LocalDateTime.now().plusDays(i)
                            .withHour(MATCH_HOUR)
                            .withMinute(MATCH_MINUTE))
                    .isCanceled(false)
                    .status(MatchResponse.MatchStatus.SCHEDULED)
                    .weather(createRandomWeather())
                    .build());
        }
        return matches;
    }

    private List<MatchResponse.Simple> createTeamResults(TeamInfo team) {
        List<MatchResponse.Simple> matches = new ArrayList<>();

        // 홈 경기 결과 1개 생성
        matches.add(MatchResponse.Simple.builder()
                .id(1L)
                .homeTeamId(team.getId())
                .awayTeamId(2L)
                .homeTeamName(team.getName())
                .awayTeamName(getTeamById(2L).getName())
                .location(team.getStadium())
                .matchTime(LocalDateTime.now().minusDays(1))
                .isCanceled(false)
                .homeScore(3)
                .awayScore(1)
                .status(MatchResponse.MatchStatus.COMPLETED)
                .result(MatchResponse.MatchResult.WIN)
                .build());

        // 원정 경기 결과 1개 생성
        matches.add(MatchResponse.Simple.builder()
                .id(2L)
                .homeTeamId(3L)
                .awayTeamId(team.getId())
                .homeTeamName(getTeamById(3L).getName())
                .awayTeamName(team.getName())
                .location(getTeamById(3L).getStadium())
                .matchTime(LocalDateTime.now().minusDays(2))
                .isCanceled(false)
                .homeScore(2)
                .awayScore(2)
                .status(MatchResponse.MatchStatus.COMPLETED)
                .result(MatchResponse.MatchResult.DRAW)
                .build());

        return matches;
    }

    private List<MatchResponse.Detail> filterMatchesByTeam(List<MatchResponse.Detail> matches, Long teamId) {
        if (teamId == null) return matches;
        return matches.stream()
                .filter(match -> match.getHomeTeam().getId().equals(teamId)
                        || match.getAwayTeam().getId().equals(teamId))
                .collect(Collectors.toList());
    }

    private List<MatchResponse.Detail> filterMatchesByStatus(List<MatchResponse.Detail> matches, String status) {
        if (status == null) return matches;
        return matches.stream()
                .filter(match -> match.getStatus().name().equals(status))
                .collect(Collectors.toList());
    }

    private WeatherResponse.Info createRandomWeather() {
        Random random = new Random();
        return WeatherResponse.Info.builder()
                .temperature(random.nextFloat() * (MAX_TEMP - MIN_TEMP) + MIN_TEMP)
                .pop(random.nextFloat() * MAX_CLOUDINESS)
                .cloudiness(random.nextInt(MAX_CLOUDINESS + 1))
                .wtTime(LocalDateTime.now())
                .build();
    }

    private TeamInfo getRandomTeam() {
        return TEAM_LIST.get(new Random().nextInt(TEAM_LIST.size()));
    }

    private TeamInfo getTeamById(Long teamId) {
        return TEAM_LIST.stream()
                .filter(team -> team.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    private TeamResponse.Simple createTeamSimple(TeamInfo teamInfo) {
        return TeamResponse.Simple.builder()
                .id(teamInfo.getId())
                .teamName(teamInfo.getName())
                .logoImageUrl(String.format("http://example.com/teams/%d/logo.png", teamInfo.getId()))
                .build();
    }
}