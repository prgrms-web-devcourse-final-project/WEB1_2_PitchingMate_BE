package com.example.mate.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamResponse {
    @Getter
    @Builder
    public static class Simple {
        private Long id;
        private String teamName;
        private String logoImageUrl;
    }

    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private String teamName;
        private String logoImageUrl;
        private StadiumResponse.Info stadium;
        private Integer rank;
        private Integer gamesPlayed;
        private Integer totalGames;
        private Integer wins;
        private Integer draws;
        private Integer losses;
        private Double gamesBehind;
    }
}