package com.example.mate.domain.mate.dto.request;

import com.example.mate.common.validator.ValidEnum;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.TransportType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatePostCreateRequest {

    @NotNull(message = "팀 ID는 필수 입력 값입니다.")
    @Min(value = 0, message = "팀 ID는 0 이상이어야 합니다.")
    @Max(value = 10, message = "팀 ID는 10 이하이어야 합니다.")
    private Long teamId;

    @NotNull(message = "경기 ID는 필수입니다.")
    private Long matchId;

    @NotBlank(message = "제목은 필수입니다.")
    @Length(min = 1, max = 20, message = "제목은 1자 이상 20자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Length(min = 1, max = 500, message = "내용은 1자 이상 500자 이하여야 합니다.")
    private String content;

    @ValidEnum(message = "연령대의 입력 값이 잘못되었습니다.", enumClass = Age.class)
    private Age age;

    @NotNull(message = "최대 참여 인원은 필수입니다.")
    @Min(value = 2, message = "최소 참여 인원은 2명 이상이어야 합니다.")
    @Max(value = 10, message = "최대 참여 인원은 10명 이하여야 합니다.")
    private Integer maxParticipants;

    @ValidEnum(message = "성별의 입력 값이 잘못되었습니다.", enumClass = Gender.class)
    private Gender gender;

    @ValidEnum(message = "이동수단의 입력 값이 잘못되었습니다.", enumClass = TransportType.class)
    private TransportType transportType;
}
