package com.example.mate.domain.matePost.dto.request;

import com.example.mate.common.validator.ValidEnum;
import com.example.mate.domain.matePost.entity.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatePostStatusRequest {
    @ValidEnum(message = "모집 상태의 입력 값이 잘못되었습니다.", enumClass = Status.class)
    private Status status;

    @NotNull(message = "참여자 목록은 필수입니다")
    @Size(min = 1, message = "최소 1명 이상의 참여자가 필요합니다")
    @Size(max = 9, message = "방장 포함 최대 10명까지만 참여할 수 있습니다")
    private List<Long> participantIds;
}
