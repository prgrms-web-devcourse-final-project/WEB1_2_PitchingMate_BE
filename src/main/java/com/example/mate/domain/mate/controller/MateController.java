package com.example.mate.domain.mate.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.dto.request.*;
import com.example.mate.domain.mate.dto.response.*;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.SortType;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.service.MateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mates")
public class MateController {

    private final MateService mateService;

    // 메이트 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<MatePostResponse>> createMatePost(@Valid @RequestPart(value = "data") MatePostCreateRequest request,
                                                                       @RequestPart(value = "file", required = false) MultipartFile file) {
        //TODO - member 정보를 request가 아니라  @AuthenticationPrincipal Long memberId로 받도록 변경
        MatePostResponse response = mateService.createMatePost(request, file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메이트 게시글 목록 조회(메인 페이지)
    @GetMapping(value = "/main", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<MatePostSummaryResponse>>> getMainPagePosts(@RequestParam(required = false) Long teamId) {

        List<MatePostSummaryResponse> matePostMain = mateService.getMainPagePosts(teamId);
        return ResponseEntity.ok(ApiResponse.success(matePostMain));
    }

    // 메이트 게시글 목록 조회(메이트 페이지)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PageResponse<MatePostSummaryResponse>>> getMatePagePosts(@RequestParam(required = false) Long teamId,
                                                                                               @RequestParam(required = false) String sortType,
                                                                                               @RequestParam(required = false) String age,
                                                                                               @RequestParam(required = false) String gender,
                                                                                               @RequestParam(required = false) Integer maxParticipants,
                                                                                               @RequestParam(required = false) String transportType,
                                                                                               @PageableDefault(size = 10) Pageable pageable) {

        MatePostSearchRequest request = MatePostSearchRequest.builder()
                .teamId(teamId)
                .sortType(sortType != null ? SortType.from(sortType) : null)
                .age(age != null ? Age.from(age) : null)
                .gender(gender != null ? Gender.from(gender) : null)
                .maxParticipants(maxParticipants)
                .transportType(transportType != null ? TransportType.from(transportType) : null)
                .build();

        PageResponse<MatePostSummaryResponse> response = mateService.getMatePagePosts(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 메이트 게시글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<MatePostDetailResponse>> getMatePostDetail(@PathVariable Long postId) {

        MatePostDetailResponse response = mateService.getMatePostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    // 메이트 게시글 모집 상태 변경(모집중, 모집완료)
    @PatchMapping("/{memberId}/{postId}/status")
    public ResponseEntity<ApiResponse<MatePostResponse>> updateMatePostStatus(@PathVariable(value = "memberId") Long memberId,
                                                                              @PathVariable(value = "postId") Long postId,
                                                                              @Valid @RequestBody MatePostStatusRequest request) {

        MatePostResponse response = mateService.updateMatePostStatus(memberId, postId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    // 메이트 게시글 직관 완료로 상태 변경
    @PatchMapping("/{memberId}/{postId}/complete")
    public ResponseEntity<ApiResponse<MatePostCompleteResponse>> completeVisit(@PathVariable Long memberId,
                                                                               @PathVariable Long postId,
                                                                               @Valid @RequestBody MatePostCompleteRequest request) {

        MatePostCompleteResponse response = mateService.completeVisit(memberId, postId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    // 메이트 게시글 삭제
    @DeleteMapping("/{memberId}/{postId}")
    public ResponseEntity<Void> deleteMatePost(@PathVariable Long memberId, @PathVariable Long postId) {
        mateService.deleteMatePost(memberId, postId);
        return ResponseEntity.noContent().build();
    }

    // TODO: @PathVariable Long memberId -> @AuthenticationPrincipal 로 변경
    // 직관 후기 작성
    @PostMapping("/{memberId}/{postId}/reviews")
    public ResponseEntity<ApiResponse<MateReviewCreateResponse>> createMateReview(
            @PathVariable Long memberId,
            @PathVariable Long postId,
            @Valid @RequestBody MateReviewCreateRequest request
    ) {

        MateReviewCreateResponse response = mateService.createReview(postId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}