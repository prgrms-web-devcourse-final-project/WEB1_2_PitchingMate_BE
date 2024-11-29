package com.example.mate.domain.mate.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.dto.request.*;
import com.example.mate.domain.mate.dto.response.MatePostDetailResponse;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.dto.response.MatePostSummaryResponse;
import com.example.mate.domain.mate.dto.response.MateReviewCreateResponse;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.SortType;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.service.MateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    public ResponseEntity<MatePostDetailResponse> getMatePostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(MatePostDetailResponse.builder()
                        .postImageUrl("imageUrl")
                        .title("12월 경기 메이트 찾아요")
                        .status(Status.OPEN)
                        .rivalTeamName("삼성")
                        .rivalMatchTime(LocalDateTime.now())
                        .location("문학")
                        .age(Age.TWENTIES)
                        .gender(Gender.MALE)
                        .transportType(TransportType.PUBLIC)
                        .maxParticipants(10)
                        .userImageUrl("imageUrl")
                        .nickname("빌터")
                        .manner(0.300F)
                        .description("같이 갈 사람 구합니다.")
                .build());
    }

    // 메이트 게시글 상태 변경
    @PatchMapping("/{postId}/status")
    public ResponseEntity<MatePostResponse> updateMatePostStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MatePostStatusRequest request) {
        return ResponseEntity.ok(MatePostResponse.builder().id(1L).status(Status.CLOSED).build());
    }

    // 메이트 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteMatePost(@PathVariable Long postId) {
        return ResponseEntity.noContent().build();
    }


    // 메이트 게시글 직관 완료로 상태 변경
    @PostMapping("/{postId}/complete")
    public ResponseEntity<MatePostResponse> completeMatePost(@PathVariable Long postId,
                                                             @AuthenticationPrincipal UserDetails userDetails,
                                                             @RequestBody MatePostCompleteRequest request) {
        // 1. 게시글 상태 COMPLETE로 변경
        // 2. visit 테이블에 직관 기록 생성
        // 3. visit_part 테이블에 participantIds 저장
        return ResponseEntity.ok(MatePostResponse.builder().id(1L).build());
    }

    // 직관 후기 작성
    @PostMapping("/{postId}/reviews")
    public ResponseEntity<MateReviewCreateResponse> createMateReview(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MateReviewRequest request
    ) {
        return ResponseEntity.ok(MateReviewCreateResponse.builder().id(1L).build());
    }
}