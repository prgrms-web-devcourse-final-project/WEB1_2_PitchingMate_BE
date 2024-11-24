package com.example.mate.domain.match.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MatchRequest {
    @Getter
    @NoArgsConstructor
    public static class Create {
        @NotNull(message = "홈팀 ID는 필수입니다")
        @Positive(message = "홈팀 ID는 양수여야 합니다")
        private Long homeTeamId;

        @NotNull(message = "원정팀 ID는 필수입니다")
        @Positive(message = "원정팀 ID는 양수여야 합니다")
        private Long awayTeamId;

        @NotBlank(message = "경기장 위치는 필수입니다")
        private String location;

        @NotNull(message = "경기 시간은 필수입니다")
        @Future(message = "경기 시간은 미래 시간이어야 합니다")
        private LocalDateTime matchTime;
    }

    @Getter
    @NoArgsConstructor
    public static class Update {
        @NotNull(message = "경기 취소 여부는 필수입니다")
        private Boolean isCanceled;

        @PositiveOrZero(message = "홈팀 점수는 0 이상이어야 합니다")
        private Integer homeScore;

        @PositiveOrZero(message = "원정팀 점수는 0 이상이어야 합니다")
        private Integer awayScore;
    }
}
