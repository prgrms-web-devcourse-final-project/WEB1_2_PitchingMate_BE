package com.example.mate.domain.match.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.TeamRecord;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
        private List<StadiumResponse.Info> stadiums;
        private Integer rank;
        private Integer gamesPlayed;
        private Integer totalGames;
        private Integer wins;
        private Integer draws;
        private Integer losses;
        private Double gamesBehind;

        private Detail(TeamInfo.Team team, TeamRecord record) {
            this.id = team.id;
            this.teamName = team.fullName;
            this.stadiums = team.getHomeStadiums().stream()
                    .map(StadiumResponse.Info::from)
                    .toList();
            this.rank = record.getRank();
            this.gamesPlayed = record.getGamesPlayed();
            this.totalGames = record.getTotalGames();
            this.wins = record.getWins();
            this.draws = record.getDraws();
            this.losses = record.getLosses();
            this.gamesBehind = record.getGamesBehind();
        }

        public static Detail from(TeamInfo.Team team, TeamRecord record) {
            return new Detail(team, record);
        }
    }
}

