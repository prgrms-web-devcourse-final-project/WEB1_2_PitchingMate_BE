package com.example.mate.domain.match.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeatherRequest {
    @Getter
    @NoArgsConstructor
    public static class Create {
        @NotNull(message = "경기 ID는 필수입니다")
        @Positive(message = "경기 ID는 양수여야 합니다")
        private Long matchId;

        @NotNull(message = "기온은 필수입니다")
        @DecimalMin(value = "-50.0", message = "기온은 -50도 이상이어야 합니다")
        @DecimalMax(value = "50.0", message = "기온은 50도 이하여야 합니다")
        private Float temperature;

        @NotNull(message = "강수확률은 필수입니다")
        @DecimalMin(value = "0.0", message = "강수확률은 0% 이상이어야 합니다")
        @DecimalMax(value = "100.0", message = "강수확률은 100% 이하여야 합니다")
        private Float pop;

        @NotNull(message = "운량은 필수입니다")
        @Min(value = 0, message = "운량은 0 이상이어야 합니다")
        @Max(value = 100, message = "운량은 100 이하여야 합니다")
        private Integer cloudiness;
    }

    @Getter
    @NoArgsConstructor
    public static class Update {
        @NotNull(message = "기온은 필수입니다")
        @DecimalMin(value = "-50.0", message = "기온은 -50도 이상이어야 합니다")
        @DecimalMax(value = "50.0", message = "기온은 50도 이하여야 합니다")
        private Float temperature;

        @NotNull(message = "강수확률은 필수입니다")
        @DecimalMin(value = "0.0", message = "강수확률은 0% 이상이어야 합니다")
        @DecimalMax(value = "100.0", message = "강수확률은 100% 이하여야 합니다")
        private Float pop;

        @NotNull(message = "운량은 필수입니다")
        @Min(value = 0, message = "운량은 0 이상이어야 합니다")
        @Max(value = 100, message = "운량은 100 이하여야 합니다")
        private Integer cloudiness;
    }
}