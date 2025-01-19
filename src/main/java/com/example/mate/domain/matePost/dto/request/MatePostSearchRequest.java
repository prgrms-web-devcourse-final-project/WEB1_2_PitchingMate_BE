package com.example.mate.domain.matePost.dto.request;

import com.example.mate.common.util.validator.ValidEnum;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.matePost.entity.Age;
import com.example.mate.domain.matePost.entity.SortType;
import com.example.mate.domain.matePost.entity.TransportType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatePostSearchRequest {

    @Min(value = 0, message = "팀 ID는 0 이상이어야 합니다.")
    @Max(value = 10, message = "팀 ID는 10 이하이어야 합니다.")
    private Long teamId;

    @ValidEnum(message = "정렬 기준 입력 값이 잘못되었습니다.", enumClass = SortType.class)
    private SortType sortType;

    @ValidEnum(message = "연령대의 입력 값이 잘못되었습니다.", enumClass = Age.class)
    private Age age;

    @ValidEnum(message = "성별의 입력 값이 잘못되었습니다.", enumClass = Gender.class)
    private Gender gender;

    @Min(value = 2, message = "최대 참여 인원은 2명 이상이어야 합니다.")
    @Max(value = 10, message = "최대 참여 인원은 10명 이하여야 합니다.")
    private Integer maxParticipants;

    @ValidEnum(message = "이동수단의 입력 값이 잘못되었습니다.", enumClass = TransportType.class)
    private TransportType transportType;
}
