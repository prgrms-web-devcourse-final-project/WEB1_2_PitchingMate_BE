package com.example.mate.domain.member.controller;

import static com.example.mate.common.response.PageResponse.validatePageable;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse;
import com.example.mate.domain.member.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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

    /*
    TODO : 2024/11/24 - 굿즈거래 후기 페이징 조회
    1. memberId 을 통해 회원 정보 조회
    2. 회원이 받은 굿즈거래 후기 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("/{memberId}/review/goods")
    public ResponseEntity<Page<MyReviewResponse>> getGoodsReviews(
            @PathVariable Long memberId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        MyReviewResponse myReviewResponse = MyReviewResponse.goodsFrom();
        List<MyReviewResponse> responses = Collections.nCopies(10, myReviewResponse);
        Page<MyReviewResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(page);
    }

    /*
    TODO : 2024/11/24 - 메이트 후기 페이징 조회
    1. memberId 을 통해 회원 정보 조회
    2. 회원이 받은 메이트 후기 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("{memberId}/review/mate")
    public ResponseEntity<Page<MyReviewResponse>> getMateReviews(
            @PathVariable Long memberId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        MyReviewResponse myReviewResponse = MyReviewResponse.mateFrom();
        List<MyReviewResponse> responses = Collections.nCopies(10, myReviewResponse);
        Page<MyReviewResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(page);
    }

    /*
    TODO : 2024/11/24 - 직관 타임라인 페이징 조회
    1. JwtToken 을 통해 회원 정보 조회
    2. 회원이 다녀온 직관 기록, 같이 본 사용자 정보, 메이트 후기 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("/timeline")
    public ResponseEntity<Page<MyVisitResponse>> getMyVisits(@PageableDefault(size = 10) Pageable pageable) {
        MyVisitResponse myVisitResponse = MyVisitResponse.from();
        List<MyVisitResponse> responses = Collections.nCopies(10, myVisitResponse);
        Page<MyVisitResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(page);
    }

    @Operation(summary = "굿즈 판매기록 페이징 조회")
    @GetMapping("/{memberId}/goods/sold")
    public ResponseEntity<ApiResponse<PageResponse<MyGoodsRecordResponse>>> getSoldGoods(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
            @Parameter(description = "페이지 요청 정보") @PageableDefault Pageable pageable
    ) {
        pageable = validatePageable(pageable);
        PageResponse<MyGoodsRecordResponse> response = profileService.getSoldGoodsPage(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /*
    TODO : 2024/11/24 - 굿즈 구매기록 페이징 조회
    1. memberId 을 통해 회원 정보 조회
    2. 회원이 구매한 굿즈기록 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("/{memberId}/goods/bought")
    public ResponseEntity<Page<MyGoodsRecordResponse>> getBoughtGoods(
            @PathVariable Long memberId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        MyGoodsRecordResponse myGoodsRecordResponse = MyGoodsRecordResponse.from();
        List<MyGoodsRecordResponse> responses = Collections.nCopies(10, myGoodsRecordResponse);
        Page<MyGoodsRecordResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(page);
    }
}
