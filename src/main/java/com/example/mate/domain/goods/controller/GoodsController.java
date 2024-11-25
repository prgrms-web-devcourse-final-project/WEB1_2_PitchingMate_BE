package com.example.mate.domain.goods.controller;

import com.example.mate.common.PageResponse;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.request.GoodsReviewFormRequest;
import com.example.mate.domain.goods.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goods.dto.response.GoodsReviewFormResponse;
import com.example.mate.domain.goods.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goods.entity.Category;
import java.util.Collections;
import java.util.List;
import org.springframework.http.ResponseEntity;
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
public class GoodsController {

    /*
    메인 페이지 : 굿즈 거래글 요약 4개 리스트 조회
    테스트 용도로, 가상의 동일한 거래글 데이터 4개를 생성
     */
    @GetMapping("/main")
    public ResponseEntity<List<GoodsPostSummaryResponse>> getGoodsPostsMain(@RequestParam Long teamId) {
        return ResponseEntity.ok(Collections.nCopies(4, GoodsPostSummaryResponse.createResponse(teamId)));
    }

    /*
    굿즈 거래 페이지 : 굿즈 거래글 팀/카테고리 기준 10개씩 페이징 조회
    1. 가상의 데이터 24개 생성
    2. 페이지 번호와 리스트 크기를 고려해, 시작/끝 인덱스 설정
    3. 요청된 페이지에 맞는 데이터 반환
    */
    @GetMapping
    public ResponseEntity<PageResponse<GoodsPostSummaryResponse>> getGoodsPosts(@RequestParam Long teamId,
                                                                                @RequestParam Category category,
                                                                                @RequestParam int pageNumber,
                                                                                @RequestParam(required = false, defaultValue = "10") int pageSize) {
        List<GoodsPostSummaryResponse> list = Collections.nCopies(24,
                GoodsPostSummaryResponse.createResponse(teamId, category));

        int start = (pageNumber - 1) * 10;
        int end = Math.min(start + 10, list.size());
        List<GoodsPostSummaryResponse> pageContent = list.subList(start, end);

        PageResponse<GoodsPostSummaryResponse> pageResponse = PageResponse.<GoodsPostSummaryResponse>builder()
                .content(pageContent)
                .totalPages((int) Math.ceil((double) list.size() / 10)) // 총 페이지 수
                .totalElements(list.size())
                .hasNext(end < list.size())
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();

        return ResponseEntity.ok(pageResponse);
    }

    /*
    굿즈 거래 페이지 : 굿즈 거래글 등록
    요청 Body를 form-data 형식으로 설정하여, Key를 값을 넣은 "data"와 사진 파일 "files"로 구분
     */
    @PostMapping
    public ResponseEntity<GoodsPostResponse> registerGoodsPost(@RequestPart("data") GoodsPostRequest request,
                                                               @RequestPart("files") MultipartFile[] files) {
        return ResponseEntity.ok(GoodsPostResponse.of(request, files));
    }

    // 굿즈 거래하기 상세 페이지 : 굿즈 거래글 단건 조회
    @GetMapping("/{goodsPostId}")
    public ResponseEntity<GoodsPostResponse> getGoodsPost(@PathVariable Long goodsPostId) {
        return ResponseEntity.ok(GoodsPostResponse.createResponse(goodsPostId));
    }

    /*
    굿즈 거래하기 상세 페이지 : 굿즈 거래글 수정
    요청 Body를 form-data 형식으로 설정하여, Key를 값을 넣은 "data"와 사진 파일 "files"로 구분
     */
    @PutMapping("/{goodsPostId}")
    public ResponseEntity<GoodsPostResponse> updateGoodsPost(@PathVariable Long goodsPostId,
                                                             @RequestPart("data") GoodsPostRequest request,
                                                             @RequestPart("files") MultipartFile[] files) {
        return ResponseEntity.ok(GoodsPostResponse.updateResponse(goodsPostId, request, files));
    }

    // 굿즈 거래하기 상세 페이지 : 굿즈 거래글 삭제
    @DeleteMapping("/{goodsPostId}")
    public ResponseEntity<Void> deleteGoodsPost(@PathVariable Long goodsPostId) {
        return ResponseEntity.noContent().build();
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

//    /*
//    후기 모아보기 페이지 : 굿즈거래 후기 10개씩 페이징 조회
//    1. 가상의 데이터 24개 생성
//    2. 페이지 번호와 리스트 크기를 고려해, 시작/끝 인덱스 설정
//    3. 요청된 페이지에 맞는 데이터 반환
//     */
//    @GetMapping("/review")
//    public ResponseEntity<Page<GoodsReviewResponse>> getGoodsReviews(
//            @RequestParam(required = false, defaultValue = "10") int pageNumber) {
//        List<GoodsReviewResponse> list = Collections.nCopies(24, GoodsReviewResponse.createResponse());
//
//        int start = (pageNumber - 1) * 10;
//        int end = Math.min(start + 10, list.size());
//        List<GoodsReviewResponse> pageContent = list.subList(start, end);
//        Pageable pageable = PageRequest.of(pageNumber - 1, 10);
//
//        return ResponseEntity.ok(new PageImpl<>(pageContent, pageable, list.size()));
//    }
}