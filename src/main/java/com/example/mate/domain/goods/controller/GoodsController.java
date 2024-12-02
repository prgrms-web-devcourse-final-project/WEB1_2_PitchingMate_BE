package com.example.mate.domain.goods.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goods.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goods.service.GoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
@Tag(name = "GoodsController", description = "굿즈거래 관련 API")
public class GoodsController {

    private final GoodsService goodsService;

    /*
    TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods" 로 변경 예정
     */
    @PostMapping("/{memberId}")
    @Operation(summary = "굿즈거래 판매글 등록", description = "굿즈거래 페이지에서 판매글을 등록합니다.")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> registerGoodsPost(
            @Parameter(description = "판매글 등록 데이터", required = true) @Validated @RequestPart("data") GoodsPostRequest request,
            @Parameter(description = "판매글 이미지 리스트", required = true) @RequestPart("files") List<MultipartFile> files,
            @Parameter(description = "판매자 ID (삭제 예정)", required = true) @PathVariable Long memberId
    ) {
        GoodsPostResponse response = goodsService.registerGoodsPost(memberId, request, files);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /*
    TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods/{goodsPostId}" 로 변경 예정
     */
    @PutMapping("/{memberId}/post/{goodsPostId}")
    @Operation(summary = "굿즈거래 판매글 수정", description = "굿즈거래 판매글 상세 페이지에서 판매글을 수정합니다.")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> updateGoodsPost(
            @Parameter(description = "판매자 ID (삭제 예정)", required = true) @PathVariable Long memberId,
            @Parameter(description = "판매글 ID", required = true) @PathVariable Long goodsPostId,
            @Parameter(description = "수정할 판매글 데이터", required = true) @Validated @RequestPart("data") GoodsPostRequest request,
            @Parameter(description = "수정할 첨부 파일 리스트", required = true) @RequestPart("files") List<MultipartFile> files
    ) {
        GoodsPostResponse response = goodsService.updateGoodsPost(memberId, goodsPostId, request, files);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /*
    TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods/{goodsPostId}" 로 변경 예정
    */
    @DeleteMapping("/{memberId}/post/{goodsPostId}")
    @Operation(summary = "굿즈거래 판매글 삭제", description = "굿즈거래 판매글 상세 페이지에서 판매글을 삭제합니다.")
    public ResponseEntity<Void> deleteGoodsPost(
            @Parameter(description = "판매자 ID (삭제 예정)", required = true) @PathVariable Long memberId,
            @Parameter(description = "삭제할 판매글 ID", required = true) @PathVariable Long goodsPostId)
    {
        goodsService.deleteGoodsPost(memberId, goodsPostId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goodsPostId}")
    @Operation(summary = "굿즈거래 판매글 상세 조회", description = "굿즈거래 판매글 상세 페이지에서 판매글을 조회합니다.")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> getGoodsPost(@Parameter(description = "조회할 판매글 ID", required = true)
                                                                       @PathVariable Long goodsPostId) {
        GoodsPostResponse response = goodsService.getGoodsPost(goodsPostId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/main")
    @Operation(summary = "메인페이지 굿즈거래 판매글 조회", description = "메인 페이지에서 굿즈거래 판매글을 요약한 4개의 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<List<GoodsPostSummaryResponse>>> getGoodsPostsMain(@Parameter(description = "팀 ID")
                                                                                         @RequestParam(required = false) Long teamId) {
        List<GoodsPostSummaryResponse> responses = goodsService.getMainGoodsPosts(teamId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping
    @Operation(summary = "굿즈거래 판매글 페이징 조회", description = "굿즈거래 페이지에서 팀/카테고리 기준으로 굿즈 거래글을 페이징 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<GoodsPostSummaryResponse>>> getGoodsPosts(
            @Parameter(description = "팀 ID") @RequestParam(required = false) Long teamId,
            @Parameter(description = "카테고리") @RequestParam(required = false) String category,
            @Parameter(description = "페이징 정보", required = true) @PageableDefault Pageable pageable
    ) {
        PageResponse<GoodsPostSummaryResponse> pageGoodsPosts = goodsService.getPageGoodsPosts(teamId, category, pageable);

        return ResponseEntity.ok(ApiResponse.success(pageGoodsPosts));
    }

    /*
    TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods/{goodsPostId}/complete" 로 변경 예정
    */
    @PostMapping("{sellerId}/post/{goodsPostId}/complete")
    @Operation(summary = "굿즈 거래 완료", description = "굿즈거래 채팅방에서 굿즈거래를 거래완료 처리합니다.")
    public ResponseEntity<ApiResponse<Void>> completeGoodsPost(
            @Parameter(description = "판매자 ID (삭제 예정)", required = true) @PathVariable Long sellerId,
            @Parameter(description = "판매글 ID", required = true) @PathVariable Long goodsPostId,
            @Parameter(description = "구매자 ID", required = true) @RequestParam Long buyerId
            ) {
        goodsService.completeTransaction(sellerId, goodsPostId, buyerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /*
    TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods/{goodsPostId}/review" 로 변경 예정
    */
    @PostMapping("/{reviewerId}/post/{goodsPostId}/review")
    @Operation(summary = "굿즈거래 후기 등록", description = "후기 페이지에서 굿즈거래 후기를 등록합니다.")
    public ResponseEntity<ApiResponse<GoodsReviewResponse>> registerGoodsReview(
            @Parameter(description = "리뷰 작성자 ID (삭제 예정)", required = true) @PathVariable Long reviewerId,
            @Parameter(description = "판매글 ID", required = true) @PathVariable Long goodsPostId,
            @Parameter(description = "후기 작성 데이터", required = true) @Validated @RequestBody GoodsReviewRequest request
    ) {
        GoodsReviewResponse response = goodsService.registerGoodsReview(reviewerId, goodsPostId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
