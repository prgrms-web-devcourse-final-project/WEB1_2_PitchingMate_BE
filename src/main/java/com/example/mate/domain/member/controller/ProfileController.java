package com.example.mate.domain.member.controller;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.common.validator.ValidPageable;
import com.example.mate.domain.goods.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.mate.dto.response.MatePostSummaryResponse;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse;
import com.example.mate.domain.member.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
@Tag(name = "Profile Controller", description = "회원 프로필 관련 API")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "굿즈거래 후기 페이징 조회")
    @GetMapping("/{memberId}/review/goods")
    public ResponseEntity<ApiResponse<PageResponse<MyReviewResponse>>> getGoodsReviews(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
            @Parameter(description = "페이지 요청 정보") @ValidPageable Pageable pageable
    ) {
        PageResponse<MyReviewResponse> response = profileService.getGoodsReviewPage(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메이트 후기 페이징 조회")
    @GetMapping("{memberId}/review/mate")
    public ResponseEntity<ApiResponse<PageResponse<MyReviewResponse>>> getMateReviews(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
            @Parameter(description = "페이지 요청 정보") @ValidPageable Pageable pageable
    ) {
        PageResponse<MyReviewResponse> response = profileService.getMateReviewPage(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "직관 타임라인 페이징 조회")
    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<PageResponse<MyVisitResponse>>> getMyVisits(
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember,
            @Parameter(description = "페이지 요청 정보") @ValidPageable Pageable pageable
    ) {
        PageResponse<MyVisitResponse> response = profileService.getMyVisitPage(authMember.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "굿즈 판매기록 페이징 조회")
    @GetMapping("/{memberId}/goods/sold")
    public ResponseEntity<ApiResponse<PageResponse<MyGoodsRecordResponse>>> getSoldGoods(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
            @Parameter(description = "페이지 요청 정보") @ValidPageable Pageable pageable
    ) {
        PageResponse<MyGoodsRecordResponse> response = profileService.getSoldGoodsPage(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "굿즈 구매기록 페이징 조회")
    @GetMapping("/{memberId}/goods/bought")
    public ResponseEntity<ApiResponse<PageResponse<MyGoodsRecordResponse>>> getBoughtGoods(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
            @Parameter(description = "페이지 요청 정보") @ValidPageable Pageable pageable,
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember
    ) {
        if (!authMember.getMemberId().equals(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_UNAUTHORIZED_ACCESS);
        }
        PageResponse<MyGoodsRecordResponse> response = profileService.getBoughtGoodsPage(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "작성한 굿즈 거래글 모아보기 페이징 조회")
    @GetMapping("/posts/goods")
    public ResponseEntity<ApiResponse<PageResponse<GoodsPostSummaryResponse>>> getGoodsPosts(
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember,
            @Parameter(description = "페이지 요청 정보") @ValidPageable Pageable pageable
    ) {
        PageResponse<GoodsPostSummaryResponse> response = profileService.getGoodsPostsPage(authMember.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "작성한 메이트 구인글 모아보기 페이징 조회")
    @GetMapping("/posts/mate")
    public ResponseEntity<ApiResponse<PageResponse<MatePostSummaryResponse>>> getMatePosts(
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember,
            @Parameter(description = "페이지 요청 정보") @ValidPageable Pageable pageable
    ) {
        PageResponse<MatePostSummaryResponse> response = profileService.getMatePostsPage(authMember.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}