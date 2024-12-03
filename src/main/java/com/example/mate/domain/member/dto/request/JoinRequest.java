package com.example.mate.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinRequest {

    @Schema(description = "사용자 이름", example = "홍길동")
    @NotBlank(message = "이름은 필수 항목입니다.")
    @Size(max = 10, message = "이름은 최대 10자까지 입력 가능합니다.")
    private String name;

    @Schema(description = "사용자 이메일", example = "tester@example.com")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Size(max = 40, message = "이메일은 최대 40자까지 입력 가능합니다.")
    private String email;

    @Schema(description = "사용자 성별", example = "M")
    private String gender;

    @Schema(description = "사용자 출생연도", example = "2000")
    private String birthyear;

    @Schema(description = "선택한 마이팀 ID", example = "1")
    @Min(value = 0, message = "teamId는 0 이상이어야 합니다.")
    @Max(value = 10, message = "teamId는 10 이하이어야 합니다.")
    private Long teamId;

    @Schema(description = "사용자 닉네임", example = "tester")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Size(max = 20, message = "nickname은 최대 20자까지 입력할 수 있습니다.")
    private String nickname;
}
