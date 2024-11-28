package com.example.mate.domain.match.entity;

import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "weather_id")
    private Weather weather;

    @Builder
    public Match(Long id, Long homeTeamId, Long awayTeamId, Long stadiumId,
                 LocalDateTime matchTime, Boolean isCanceled,
                 MatchStatus status) {
        this.id = id;
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
