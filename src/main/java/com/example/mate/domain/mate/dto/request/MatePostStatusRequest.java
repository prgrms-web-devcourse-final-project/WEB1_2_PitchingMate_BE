package com.example.mate.domain.mate.dto.request;

import com.example.mate.common.utils.validator.ValidEnum;
import com.example.mate.domain.mate.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatePostStatusRequest {
    @ValidEnum(message = "모집 상태의 입력 값이 잘못되었습니다.", enumClass = Status.class)
    private Status status;
}
