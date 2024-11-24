package com.example.mate.domain.match.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamRequest {
    @Getter
    @NoArgsConstructor
    public static class Create {
        @NotNull(message = "경기장 ID는 필수입니다")
        @Positive(message = "경기장 ID는 양수여야 합니다")
        private Long stadiumId;

        @NotBlank(message = "팀 이름은 필수입니다")
        private String teamName;

        @NotBlank(message = "로고 이미지 URL은 필수입니다")
        private String logoImageUrl;
    }

    @Getter
    @NoArgsConstructor
    public static class Update {
        @NotBlank(message = "팀 이름은 필수입니다")
        private String teamName;

        @NotBlank(message = "로고 이미지 URL은 필수입니다")
        private String logoImageUrl;

        @Positive(message = "순위는 양수여야 합니다")
        private Integer rank;

        @PositiveOrZero(message = "경기 수는 0 이상이어야 합니다")
        private Integer gamesPlayed;

        @Positive(message = "전체 경기 수는 양수여야 합니다")
        private Integer totalGames;

        @PositiveOrZero(message = "승리 수는 0 이상이어야 합니다")
        private Integer wins;

        @PositiveOrZero(message = "무승부 수는 0 이상이어야 합니다")
        private Integer draws;

        @PositiveOrZero(message = "패배 수는 0 이상이어야 합니다")
        private Integer losses;

        @PositiveOrZero(message = "게임차는 0 이상이어야 합니다")
        private Double gamesBehind;
    }
}