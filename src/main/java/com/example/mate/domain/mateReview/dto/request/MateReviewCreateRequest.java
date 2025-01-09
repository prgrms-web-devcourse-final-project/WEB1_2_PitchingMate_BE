package com.example.mate.domain.mateReview.dto.request;

import com.example.mate.common.validator.ValidEnum;
import com.example.mate.domain.constant.Rating;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MateReviewCreateRequest {

    @NotNull(message = "리뷰 대상자 ID는 필수입니다.")
    private Long revieweeId;

    @ValidEnum(message = "유효하지 않은 평가입니다.", enumClass = Rating.class)
    private Rating rating;

    @Size(max = 100, message = "리뷰 내용은 100자를 초과할 수 없습니다.")
    private String content;
}
