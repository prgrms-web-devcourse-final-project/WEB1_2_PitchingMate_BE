package com.example.mate.domain.match.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchResult;
import com.example.mate.domain.match.entity.MatchStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchResponse {
    private Long id;
    private TeamResponse.Simple homeTeam;
    private TeamResponse.Simple awayTeam;
    private String location;
    private LocalDateTime matchTime;
    private Boolean isCanceled;
    private MatchStatus status;
    private WeatherResponse.Info weather;
    private Integer homeScore;
    private Integer awayScore;
    private MatchResult result;

    @Builder
    private MatchResponse(Match match, Long viewerTeamId) {
        this.id = match.getId();
        this.homeTeam = TeamResponse.Simple.from(TeamInfo.getById(match.getHomeTeamId()));
        this.awayTeam = TeamResponse.Simple.from(TeamInfo.getById(match.getAwayTeamId()));
        this.location = match.getStadium().name;
        this.matchTime = match.getMatchTime();
        this.isCanceled = match.getIsCanceled();
        this.status = match.getStatus();
        this.weather = WeatherResponse.Info.from(match.getWeather());
        this.homeScore = match.getHomeScore();
        this.awayScore = match.getAwayScore();
        this.result = calculateResult(match, viewerTeamId);
    }

    public static MatchResponse from(Match match, Long viewerTeamId) {  // viewerTeamId 파라미터 추가
        return new MatchResponse(match, viewerTeamId);
    }

    private MatchResult calculateResult(Match match, Long teamId) {
        if (match.getStatus() != MatchStatus.COMPLETED) return null;

        // teamId가 null이면 홈팀 기준으로 결과 계산
        if (teamId == null || match.getHomeTeamId().equals(teamId)) {
            if (match.getHomeScore() > match.getAwayScore()) return MatchResult.WIN;
            if (match.getHomeScore() < match.getAwayScore()) return MatchResult.LOSE;
        } else {
            if (match.getHomeScore() < match.getAwayScore()) return MatchResult.WIN;
            if (match.getHomeScore() > match.getAwayScore()) return MatchResult.LOSE;
        }
        return MatchResult.DRAW;
    }
}