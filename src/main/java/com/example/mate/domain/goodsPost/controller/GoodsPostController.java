package com.example.mate.domain.goodsPost.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.common.validator.ValidPageable;
import com.example.mate.domain.goodsPost.dto.request.GoodsPostRequest;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostResponse;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goodsPost.service.GoodsPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
@Tag(name = "GoodsPostController", description = "굿즈거래 판매글 관련 API")
public class GoodsPostController {

    private final GoodsPostService goodsPostService;

    @PostMapping
    @Operation(summary = "굿즈거래 판매글 등록", description = "굿즈거래 페이지에서 판매글을 등록합니다.")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> registerGoodsPost(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "판매글 등록 데이터", required = true) @Validated @RequestPart("data") GoodsPostRequest request,
            @Parameter(description = "판매글 이미지 리스트", required = true) @RequestPart("files") List<MultipartFile> files
    ) {
        GoodsPostResponse response = goodsPostService.registerGoodsPost(member.getMemberId(), request, files);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{goodsPostId}")
    @Operation(summary = "굿즈거래 판매글 수정", description = "굿즈거래 판매글 상세 페이지에서 판매글을 수정합니다.")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> updateGoodsPost(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "판매글 ID", required = true) @PathVariable Long goodsPostId,
            @Parameter(description = "수정할 판매글 데이터", required = true) @Validated @RequestPart("data") GoodsPostRequest request,
            @Parameter(description = "수정할 첨부 파일 리스트", required = true) @RequestPart("files") List<MultipartFile> files
    ) {
        GoodsPostResponse response = goodsPostService.updateGoodsPost(member.getMemberId(), goodsPostId, request, files);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{goodsPostId}")
    @Operation(summary = "굿즈거래 판매글 삭제", description = "굿즈거래 판매글 상세 페이지에서 판매글을 삭제합니다.")
    public ResponseEntity<Void> deleteGoodsPost(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "삭제할 판매글 ID", required = true) @PathVariable Long goodsPostId) {
        goodsPostService.deleteGoodsPost(member.getMemberId(), goodsPostId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goodsPostId}")
    @Operation(summary = "굿즈거래 판매글 상세 조회", description = "굿즈거래 판매글 상세 페이지에서 판매글을 조회합니다.")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> getGoodsPost(@Parameter(description = "조회할 판매글 ID", required = true)
                                                                       @PathVariable Long goodsPostId) {
        GoodsPostResponse response = goodsPostService.getGoodsPost(goodsPostId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/main")
    @Operation(summary = "메인페이지 굿즈거래 판매글 조회", description = "메인 페이지에서 굿즈거래 판매글을 요약한 4개의 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<List<GoodsPostSummaryResponse>>> getGoodsPostsMain(@Parameter(description = "팀 ID")
                                                                                         @RequestParam(required = false) Long teamId) {
        List<GoodsPostSummaryResponse> responses = goodsPostService.getMainGoodsPosts(teamId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping
    @Operation(summary = "굿즈거래 판매글 페이징 조회", description = "굿즈거래 페이지에서 팀/카테고리 기준으로 굿즈 거래글을 페이징 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<GoodsPostSummaryResponse>>> getGoodsPosts(
            @Parameter(description = "팀 ID") @RequestParam(required = false) Long teamId,
            @Parameter(description = "카테고리") @RequestParam(required = false) String category,
            @Parameter(description = "페이징 정보", required = true) @ValidPageable Pageable pageable
    ) {
        PageResponse<GoodsPostSummaryResponse> pageGoodsPosts = goodsPostService.getPageGoodsPosts(teamId, category, pageable);

        return ResponseEntity.ok(ApiResponse.success(pageGoodsPosts));
    }

    @PostMapping("/{goodsPostId}/complete")
    @Operation(summary = "굿즈 거래 완료", description = "굿즈거래 채팅방에서 굿즈거래를 거래완료 처리합니다.")
    public ResponseEntity<ApiResponse<Void>> completeGoodsPost(
            @AuthenticationPrincipal AuthMember member,
            @Parameter(description = "판매글 ID", required = true) @PathVariable Long goodsPostId,
            @Parameter(description = "구매자 ID", required = true) @RequestParam Long buyerId
    ) {
        goodsPostService.completeTransaction(member.getMemberId(), goodsPostId, buyerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
