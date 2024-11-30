package com.example.mate.domain.goods.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.request.GoodsReviewFormRequest;
import com.example.mate.domain.goods.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goods.dto.response.GoodsReviewFormResponse;
import com.example.mate.domain.goods.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goods.service.GoodsService;
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
public class GoodsController {

    private final GoodsService goodsService;

    /*
    굿즈 거래 페이지 : 굿즈 거래글 등록
    TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
     */
    @PostMapping("/{memberId}")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> registerGoodsPost(
            @Validated @RequestPart("data") GoodsPostRequest request,
            @RequestPart("files") List<MultipartFile> files,
            @PathVariable Long memberId
    ) {
        GoodsPostResponse response = goodsService.registerGoodsPost(memberId, request, files);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /*
    굿즈 거래하기 상세 페이지 : 굿즈 거래글 수정
    TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods/{goodsPostId}" 로 변경 예정
     */
    @PutMapping("/{memberId}/post/{goodsPostId}")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> updateGoodsPost(
            @PathVariable Long memberId,
            @PathVariable Long goodsPostId,
            @Validated @RequestPart("data") GoodsPostRequest request,
            @RequestPart("files") List<MultipartFile> files
    ) {
        GoodsPostResponse response = goodsService.updateGoodsPost(memberId, goodsPostId, request, files);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /*
    굿즈 거래하기 상세 페이지 : 굿즈 거래글 삭제
    TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods/{goodsPostId}" 로 변경 예정
     */
    @DeleteMapping("/{memberId}/post/{goodsPostId}")
    public ResponseEntity<Void> deleteGoodsPost(@PathVariable Long memberId, @PathVariable Long goodsPostId) {
        goodsService.deleteGoodsPost(memberId, goodsPostId);
        return ResponseEntity.noContent().build();
    }

    // 굿즈 거래하기 상세 페이지 : 굿즈 거래글 단건 조회
    @GetMapping("/{goodsPostId}")
    public ResponseEntity<ApiResponse<GoodsPostResponse>> getGoodsPost(@PathVariable Long goodsPostId) {
        GoodsPostResponse response = goodsService.getGoodsPost(goodsPostId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 메인 페이지 : 굿즈 거래글 요약 4개 리스트 조회
    @GetMapping("/main")
    public ResponseEntity<ApiResponse<List<GoodsPostSummaryResponse>>> getGoodsPostsMain(@RequestParam(required = false) Long teamId) {
        List<GoodsPostSummaryResponse> responses = goodsService.getMainGoodsPosts(teamId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /*
    굿즈 거래 페이지 : 굿즈 거래글 팀/카테고리 기준 10개씩 페이징 조회
    */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GoodsPostSummaryResponse>>> getGoodsPosts(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String category,
            @PageableDefault Pageable pageable
    ) {
        PageResponse<GoodsPostSummaryResponse> pageGoodsPosts = goodsService.getPageGoodsPosts(teamId, category, pageable);

        return ResponseEntity.ok(ApiResponse.success(pageGoodsPosts));
    }

    // 굿즈 채팅창 - 알럿창 : 굿즈 거래 완료
    @PostMapping("/{goodsPostId}/complete")
    public ResponseEntity<Void> completeGoodsPost(@PathVariable Long goodsPostId) {
        return ResponseEntity.ok().build();
    }

    // 굿즈 거래후기 : 굿즈 거래후기 페이지 조회
    @GetMapping("/{goodsPostId}/review")
    public ResponseEntity<GoodsReviewFormResponse> getGoodsReviewForm(@PathVariable Long goodsPostId,
                                                                      @RequestBody GoodsReviewFormRequest request) {
        return ResponseEntity.ok(GoodsReviewFormResponse.builder()
                .goodsPostId(goodsPostId)
                .goodsPostTitle(request.getGoodsPostTitle())
                .reviewer(request.getReviewer())
                .reviewee(request.getReviewee())
                .imageUrl(request.getImageUrl())
                .build());
    }

    // 굿즈 거래후기 : 굿즈 거래후기 등록
    @PostMapping("/{goodsPostId}/review")
    public ResponseEntity<GoodsReviewResponse> registerGoodsReview(@PathVariable Long goodsPostId,
                                                                   @RequestBody GoodsReviewRequest request) {
        return ResponseEntity.ok(GoodsReviewResponse.createResponse(goodsPostId, request));
    }
}
