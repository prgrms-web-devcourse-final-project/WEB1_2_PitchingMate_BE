package com.example.mate.domain.goodsReview.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.domain.goodsReview.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goodsReview.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goodsReview.service.GoodsReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goods/review")
@RequiredArgsConstructor
@Tag(name = "GoodsReviewController", description = "굿즈거래 리뷰 관련 API")
public class GoodsReviewController {

    private final GoodsReviewService goodsReviewService;

    /*
    TODO : 굿즈 후기 등록 폼 데이터 API 구현
    @GetMapping("/{goodsPostId}")
    @Operation(summary = "굿즈거래 후기 폼 데이터 조회", description = "굿즈거래 후기를 등록할 때 필요한 폼 데이터를 반환합니다.")
    */

    @PostMapping("/{goodsPostId}")
    @Operation(summary = "굿즈거래 후기 등록", description = "후기 페이지에서 굿즈거래 후기를 등록합니다.")
    public ResponseEntity<ApiResponse<GoodsReviewResponse>> registerGoodsReview(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "판매글 ID", required = true) @PathVariable Long goodsPostId,
            @Parameter(description = "후기 작성 데이터", required = true) @Validated @RequestBody GoodsReviewRequest request
    ) {
        GoodsReviewResponse response = goodsReviewService.registerGoodsReview(member.getMemberId(), goodsPostId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
