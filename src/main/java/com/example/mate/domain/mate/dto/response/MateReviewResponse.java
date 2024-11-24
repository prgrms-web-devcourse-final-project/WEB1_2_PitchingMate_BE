package com.example.mate.domain.mate.dto.response;

import com.example.mate.entity.Rating;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MateReviewResponse {
    private Long reviewId;
    private Long postId;
    private String postTitle;
    private Rating rating;
    private String content;
    private String reviewerNickname;
    private LocalDateTime createdAt;
}
