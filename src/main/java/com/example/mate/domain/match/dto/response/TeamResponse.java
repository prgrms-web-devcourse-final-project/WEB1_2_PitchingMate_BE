package com.example.mate.domain.match.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.TeamRecord;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamResponse {
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Simple {
        private Long id;
        private String teamName;

        @Builder
        public Simple(TeamInfo.Team team) {
            this.id = team.id;
            this.teamName = team.fullName;
        }

        public static Simple from(TeamInfo.Team team) {
            return Simple.builder()
                    .team(team)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Detail {
        private Long id;
        private String teamName;
        private StadiumResponse.Info stadium;
        private Integer rank;
        private Integer gamesPlayed;
        private Integer totalGames;
        private Integer wins;
        private Integer draws;
        private Integer losses;
        private Double gamesBehind;

        @Builder
        public Detail(TeamInfo.Team team, TeamRecord record) {
            this.id = team.id;
            this.teamName = team.fullName;
            this.stadium = StadiumResponse.Info.from(team.homeStadium);
            this.rank = record.getRank();
            this.gamesPlayed = record.getGamesPlayed();
            this.totalGames = record.getTotalGames();
            this.wins = record.getWins();
            this.draws = record.getDraws();
            this.losses = record.getLosses();
            this.gamesBehind = record.getGamesBehind();
        }

        public static Detail from(TeamInfo.Team team, TeamRecord record) {
            return Detail.builder()
                    .team(team)
                    .record(record)
                    .build();
        }
    }
}
