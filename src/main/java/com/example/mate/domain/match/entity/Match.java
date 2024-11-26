package com.example.mate.domain.match.entity;

import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.entity.MatchStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "home_team_id", nullable = false)
    private Long homeTeamId;

    @Column(name = "away_team_id", nullable = false)
    private Long awayTeamId;

    @Column(name = "stadium_id", nullable = false)
    private Long stadiumId;

    @Column(nullable = false)
    private LocalDateTime matchTime;

    private Boolean isCanceled;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private Integer homeScore;
    private Integer awayScore;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "weather_id")
    private Weather weather;

    @Builder
    public Match(Long homeTeamId, Long awayTeamId, Long stadiumId,
                 LocalDateTime matchTime, Boolean isCanceled,
                 MatchStatus status) {
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.stadiumId = stadiumId;
        this.matchTime = matchTime;
        this.isCanceled = isCanceled;
        this.status = status;
    }

    // Stadium 정보 조회
    public StadiumInfo.Stadium getStadium() {
        return StadiumInfo.getById(this.stadiumId);
    }

    // 날씨 정보 업데이트
    public void updateWeather(Weather weather) {
        this.weather = weather;
        weather.setMatch(this);
    }

    // 홈 팀의 경기장인지 확인
    public boolean isHomeStadium() {
        TeamInfo.Team homeTeam = TeamInfo.getById(this.homeTeamId);
        return homeTeam.homeStadium.id.equals(this.stadiumId);
    }
}
