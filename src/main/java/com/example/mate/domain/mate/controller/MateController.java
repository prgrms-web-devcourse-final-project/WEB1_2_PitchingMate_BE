package com.example.mate.domain.mate.controller;

import com.example.mate.common.PageResponse;
import com.example.mate.domain.mate.dto.request.*;
import com.example.mate.domain.mate.dto.response.*;
import com.example.mate.entity.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mates")
public class MateController {

    // 메이트 게시글 작성
    @PostMapping
    public ResponseEntity<MatePostResponse> createMatePost(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @RequestPart("data") MatePostCreateRequest request,
                                                                 @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(MatePostResponse.builder().id(1L).build());
    }

    // 메이트 게시글 목록 조회(메인 페이지)
    @GetMapping("/main")
    public ResponseEntity<List<MatePostSummaryResponse>> getMatePostsMain(@RequestParam Long teamId) {
        return ResponseEntity.ok(List.of(MatePostSummaryResponse.builder()
                        .imageUrl("imageUrl")
                        .title("12월 경기 메이트 찾아요")
                        .status(Status.OPEN)
                        .rivalTeamName("삼성")
                        .rivalMatchTime(LocalDateTime.now())
                        .maxParticipants(10)
                        .age(Age.TWENTIES)
                        .gender(Gender.MALE)
                        .transportType(TransportType.PUBLIC)
                        .build()));
    }

    // 메이트 게시글 목록 조회(메이트 페이지)
    // 필터링 검색을 위한 동적 쿼리 적용 필요
    @GetMapping
    public ResponseEntity<PageResponse<MatePostSummaryResponse>> getMatePosts(@RequestParam Long teamId,
                                                                              @PageableDefault(size = 10) Pageable pageable,
                                                                              @ModelAttribute MatePostSearchRequest searchRequest) {
        List<MatePostSummaryResponse> posts = List.of(
                MatePostSummaryResponse.builder()
                        .imageUrl("imageUrl")
                        .title("12월 경기 메이트 찾아요")
                        .status(Status.OPEN)
                        .rivalTeamName("삼성")
                        .rivalMatchTime(LocalDateTime.now())
                        .maxParticipants(10)
                        .age(Age.TWENTIES)
                        .gender(Gender.MALE)
                        .transportType(TransportType.PUBLIC)
                        .build()
        );

        PageResponse<MatePostSummaryResponse> pageResponse = PageResponse.<MatePostSummaryResponse>builder()
                .content(posts)
                .totalPages(5)          // 총 페이지 수
                .totalElements(42L)     // 총 게시글 수
                .hasNext(true)          // 다음 페이지 존재 여부
                .pageNumber(0)          // 현재 페이지 번호 (0부터 시작)
                .pageSize(10)           // 페이지 크기
                .build();

        return ResponseEntity.ok(pageResponse);
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


    // 직관 타임라인 조회
    @GetMapping("/{postId}/timeline")
    public ResponseEntity<MateTimeLineResponse> getMateTimeline(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(MateTimeLineResponse.builder()
                .visitDate(LocalDateTime.now())
                .stadiumName("대구 라이온스 파크")
                .teamName("삼성")
                .rivalTeamName("KT")
                .participants(List.of(
                        TimelineParticipantResponse.builder()
                                .userId(1L)
                                .nickname("김야구")
                                .content(null)  // 아직 후기를 작성하지 않은 경우
                                .build(),
                        TimelineParticipantResponse.builder()
                                .userId(2L)
                                .nickname("장야구")
                                .content("즐거웠어요!")
                                .build()
                ))
                .build());
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

    // 직관 후기 조회
    @GetMapping("/reviews/received")
    public ResponseEntity<PageResponse<MateReviewResponse>> getReceivedReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        List<MateReviewResponse> reviews = List.of(
                MateReviewResponse.builder()
                        .reviewId(1L)
                        .postId(100L)  // 연계된 게시글 ID
                        .postTitle("12월 경기 메이트 찾아요")  // 게시글 제목
                        .rating(Rating.GREAT)
                        .content("같이 응원하는 모습이 열정적이었어요!")
                        .reviewerNickname("응원단장")
                        .createdAt(LocalDateTime.now().minusDays(2))
                        .build(),
                MateReviewResponse.builder()
                        .reviewId(2L)
                        .postId(101L)
                        .postTitle("두산전 같이 가실 분")
                        .rating(Rating.GOOD)
                        .content("시간 약속을 잘 지켜주셨어요")
                        .reviewerNickname("야구친구")
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .build()
        );

        PageResponse<MateReviewResponse> pageResponse = PageResponse.<MateReviewResponse>builder()
                .content(reviews)
                .totalPages(5)
                .totalElements(42L)
                .hasNext(true)
                .pageNumber(0)
                .pageSize(10)
                .build();

        return ResponseEntity.ok(pageResponse);
    }
}
