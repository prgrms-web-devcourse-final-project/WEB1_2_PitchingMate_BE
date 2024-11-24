package com.example.mate.domain.match.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MatchResponse {
    public enum MatchStatus {
        SCHEDULED("예정"),
        COMPLETED("종료"),
        CANCELED("취소");

        private final String description;

        MatchStatus(String description) {
            this.description = description;
        }
    }

    public enum MatchResult {
        WIN("승"),
        LOSE("패"),
        DRAW("무");

        private final String description;

        MatchResult(String description) {
            this.description = description;
        }
    }

    @Getter
    @Builder
    public static class Simple {  // 종료된 경기용 Response (@@팀의 최근 전적)
        private Long id;
        private Long homeTeamId;
        private Long awayTeamId;
        private String homeTeamName;
        private String awayTeamName;
        private String location;
        private LocalDateTime matchTime;
        private Boolean isCanceled;
        private Integer homeScore;
        private Integer awayScore;
        private MatchStatus status;
        private MatchResult result;

    }

    @Getter
    @Builder
    public static class Detail {  // 예정된 경기용 Response ( 상단 배너 )
        private Long id;
        private TeamResponse.Simple homeTeam;
        private TeamResponse.Simple awayTeam;
        private String location;
        private LocalDateTime matchTime;
        private Boolean isCanceled;
        private MatchStatus status;
        private WeatherResponse.Info weather;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        public LocalDateTime getMatchTime() {
            return matchTime;
        }
    }
}