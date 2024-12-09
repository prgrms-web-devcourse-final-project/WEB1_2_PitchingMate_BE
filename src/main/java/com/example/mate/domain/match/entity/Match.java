package com.example.mate.domain.match.entity;

import com.example.mate.domain.constant.StadiumInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "`match`") // 테이블 이름을 backtick(`)으로 감싸서 사용
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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "weather_id")
    private Weather weather;

    @Builder
    public Match(Long id, Long homeTeamId, Long awayTeamId, Long stadiumId,
                 LocalDateTime matchTime, Boolean isCanceled,
                 MatchStatus status, Integer homeScore, Integer awayScore) {
        this.id = id;
        this.homeTeamId = homeTeamId;
        this.awayTeamId = awayTeamId;
        this.stadiumId = stadiumId;
        this.matchTime = matchTime;
        this.isCanceled = isCanceled;
        this.status = status;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
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

    // 업데이트용 메서드
    public void updateMatchDetails(Integer homeScore, Integer awayScore, MatchStatus status, Boolean isCanceled) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.status = status;
        this.isCanceled = isCanceled;
    }

}
