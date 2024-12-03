package com.example.mate.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberLoginRequest {

    @Schema(description = "사용자 이메일", example = "test@example.com")
    @NotBlank(message = "이메일은 필수 항목입니다.")
    @Size(max = 40, message = "이메일은 최대 40자까지 입력 가능합니다.")
    private String email;

    @JsonCreator
    public MemberLoginRequest(@JsonProperty("email") String email) {
        this.email = email;
    }
}
