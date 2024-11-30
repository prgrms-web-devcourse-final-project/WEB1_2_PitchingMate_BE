package com.example.mate.domain.member.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinRequest {

    private String name;
    private String email;
    private String gender;
    private String birthyear;

    @Min(value = 0, message = "teamId는 0 이상이어야 합니다.")
    @Max(value = 10, message = "teamId는 10 이하이어야 합니다.")
    private Long teamId;

    @Size(max = 20, message = "nickname은 최대 20자까지 입력할 수 있습니다.")
    private String nickname;
}
