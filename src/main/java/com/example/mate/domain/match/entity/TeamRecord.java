package com.example.mate.domain.match.entity;

import com.example.mate.domain.constant.TeamInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;


    @Column(name = "team_id", nullable = false, unique = true)
    private Long teamId;

    @Column(name = "`rank`", nullable = false)  // 예약어인 rank를 backtick(`)으로 감싸서 사용
    private Integer rank;

    @Column
    private Integer gamesPlayed;

    @Column
    private Integer totalGames;

    @Column
    private Integer wins;

    @Column
    private Integer draws;

    @Column
    private Integer losses;

    @Column
    private Double gamesBehind;

    @Builder
    public TeamRecord(Long teamId, Integer rank, Integer gamesPlayed, Integer totalGames,
                      Integer wins, Integer draws, Integer losses, Double gamesBehind) {
        this.teamId = teamId;
        this.rank = rank;
        this.gamesPlayed = gamesPlayed;
        this.totalGames = totalGames;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.gamesBehind = gamesBehind;
    }

//    // 팀 정보 조회
//    public TeamInfo.Team getTeamInfo() {
//        return TeamInfo.getById(this.id);
//    }

    // 성적 정보 업데이트
    public void updateRecord(Integer rank, Integer wins, Integer draws, Integer losses, Double gamesBehind) {
        this.rank = rank;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
        this.gamesBehind = gamesBehind;
        this.gamesPlayed = wins + draws + losses;
    }
}