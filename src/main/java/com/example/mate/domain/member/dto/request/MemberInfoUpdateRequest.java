package com.example.mate.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberInfoUpdateRequest {

    @Schema(description = "팀 ID", example = "1")
    @Min(value = 0, message = "팀 ID는 0 이상이어야 합니다.")
    @Max(value = 10, message = "팀 ID는 10 이하이어야 합니다.")
    private Long teamId;

    @Schema(description = "사용자 닉네임", example = "newTester")
    @NotBlank(message = "닉네임은 필수 항목입니다.")
    @Size(max = 20, message = "닉네임은 최대 20자까지 입력 가능합니다.")
    private String nickname;

    @Schema(description = "사용자 소개글", example = "안녕하세요")
    @Size(max = 100, message = "소개글은 최대 100자까지 입력 가능합니다.")
    private String aboutMe;

    @Min(value = 1, message = "회원 ID는 1 이상이어야 합니다.")
    private Long memberId;
}
