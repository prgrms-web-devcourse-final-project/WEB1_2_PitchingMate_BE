package com.example.mate.domain.match.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StadiumRequest {
    @Getter
    @NoArgsConstructor
    public static class Create {
        @NotBlank(message = "경기장 이름은 필수입니다")
        private String stadiumName;

        @NotBlank(message = "위치는 필수입니다")
        private String location;

        @NotBlank(message = "경도는 필수입니다")
        @Pattern(regexp = "^-?([0-9]{1,3}\\.[0-9]+)$", message = "올바른 경도 형식이 아닙니다")
        private String longitude;

        @NotBlank(message = "위도는 필수입니다")
        @Pattern(regexp = "^-?([0-9]{1,2}\\.[0-9]+)$", message = "올바른 위도 형식이 아닙니다")
        private String latitude;
    }

    @Getter
    @NoArgsConstructor
    public static class Update {
        @NotBlank(message = "경기장 이름은 필수입니다")
        private String stadiumName;

        @NotBlank(message = "위치는 필수입니다")
        private String location;

        @Pattern(regexp = "^-?([0-9]{1,3}\\.[0-9]+)$", message = "올바른 경도 형식이 아닙니다")
        private String longitude;

        @Pattern(regexp = "^-?([0-9]{1,2}\\.[0-9]+)$", message = "올바른 위도 형식이 아닙니다")
        private String latitude;
    }
}