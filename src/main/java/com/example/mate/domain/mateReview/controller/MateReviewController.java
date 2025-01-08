package com.example.mate.domain.mateReview.controller;

import com.example.mate.domain.mateReview.service.MateReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mates/review")
@RequiredArgsConstructor
@Tag(name = "MateReviewController", description = "메이트 리뷰 관련 API")
public class MateReviewController {

    private final MateReviewService mateReviewService;
}
