package com.example.mate.domain.mateReview.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.domain.mateReview.dto.request.MateReviewCreateRequest;
import com.example.mate.domain.mateReview.dto.response.MateReviewCreateResponse;
import com.example.mate.domain.mateReview.service.MateReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mates/review")
@RequiredArgsConstructor
@Tag(name = "MateReviewController", description = "메이트 리뷰 관련 API")
public class MateReviewController {

    private final MateReviewService mateReviewService;

    @PostMapping("/{postId}")
    @Operation(summary = "메이트 직관 후기 등록", description = "직관 타임라인 페이지에서 메이트에 대한 후기를 등록합니다.")
    public ResponseEntity<ApiResponse<MateReviewCreateResponse>> createMateReview(@AuthenticationPrincipal AuthMember member,
                                                                                  @Parameter(description = "구인글 ID", required = true)
                                                                                  @PathVariable Long postId,
                                                                                  @Parameter(description = "리뷰 대상자 ID와 평점 및 코멘트", required = true)
                                                                                  @Valid @RequestBody MateReviewCreateRequest request
    ) {

        MateReviewCreateResponse response = mateReviewService.createReview(postId, member.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
