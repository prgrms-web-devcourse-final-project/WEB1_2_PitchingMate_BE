package com.example.mate.domain.matePost.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.common.validator.ValidPageable;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.matePost.dto.request.*;
import com.example.mate.domain.matePost.dto.response.*;
import com.example.mate.domain.matePost.entity.Age;
import com.example.mate.domain.matePost.entity.SortType;
import com.example.mate.domain.matePost.entity.TransportType;
import com.example.mate.domain.matePost.service.MatePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mates")
@Tag(name = "MateController", description = "메이트 구인글 관련 API")
public class MatePostController {

    private final MatePostService matePostService;

    @PostMapping
    @Operation(summary = "메이트 구인글 등록", description = "메이트 구인글 페이지에서 등록합니다.")
    public ResponseEntity<ApiResponse<MatePostResponse>> createMatePost(@Parameter(description = "구인글 등록 데이터", required = true)
                                                                        @Valid @RequestPart(value = "data") MatePostCreateRequest request,
                                                                        @Parameter(description = "구인글 대표사진", required = true)
                                                                        @RequestPart(value = "file", required = false) MultipartFile file,
                                                                        @AuthenticationPrincipal AuthMember member) {
        MatePostResponse response = matePostService.createMatePost(request, file, member.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping(value = "/main", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "메인페이지 메이트 구인글 조회", description = "메인 페이지에서 메이트 구인글을 요약한 4개의 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<List<MatePostSummaryResponse>>> getMainPagePosts(@Parameter(description = "팀 ID")
                                                                                       @RequestParam(required = false) Long teamId) {
        List<MatePostSummaryResponse> matePostMain = matePostService.getMainPagePosts(teamId);
        return ResponseEntity.ok(ApiResponse.success(matePostMain));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "메이트 구인글 페이징 조회", description = "메이트 구인글 페이지에서 팀/카테고리 기준으로 페이징 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<MatePostSummaryResponse>>> getMatePagePosts(@Parameter(description = "팀 ID")
                                                                                                 @RequestParam(required = false) Long teamId,
                                                                                               @Parameter(description = "정렬 기준")
                                                                                                 @RequestParam(required = false) String sortType,
                                                                                               @Parameter(description = "연령대 카테고리")
                                                                                                 @RequestParam(required = false) String age,
                                                                                               @Parameter(description = "성별 카테고리")
                                                                                                 @RequestParam(required = false) String gender,
                                                                                               @Parameter(description = "모집인원 수")
                                                                                                 @RequestParam(required = false) Integer maxParticipants,
                                                                                               @Parameter(description = "이동수단 카테고리")
                                                                                                 @RequestParam(required = false) String transportType,
                                                                                               @Parameter(description = "페이징 정보")
                                                                                                   @ValidPageable Pageable pageable) {

        MatePostSearchRequest request = MatePostSearchRequest.builder()
                .teamId(teamId)
                .sortType(sortType != null ? SortType.from(sortType) : null)
                .age(age != null ? Age.from(age) : null)
                .gender(gender != null ? Gender.from(gender) : null)
                .maxParticipants(maxParticipants)
                .transportType(transportType != null ? TransportType.from(transportType) : null)
                .build();

        PageResponse<MatePostSummaryResponse> response = matePostService.getMatePagePosts(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "메이트 구인글 상세 조회", description = "메이트 구인글 상세 페이지에서 조회합니다.")
    public ResponseEntity<ApiResponse<MatePostDetailResponse>> getMatePostDetail(@Parameter(description = "조회할 구인글 ID", required = true)
                                                                                 @PathVariable Long postId) {

        MatePostDetailResponse response = matePostService.getMatePostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "메이트 구인글 수정", description = "메이트 구인글 상세 페이지에서 수정합니다.")
    public ResponseEntity<ApiResponse<MatePostResponse>> updateMatePost(  @AuthenticationPrincipal AuthMember member,
                                                                          @Parameter(description = "구인글 ID", required = true)
                                                                          @PathVariable Long postId,
                                                                          @Parameter(description = "수정할 구인글 데이터", required = true)
                                                                          @Valid @RequestPart(value = "data") MatePostUpdateRequest request,
                                                                          @Parameter(description = "수정할 대표사진 파일 ", required = true)
                                                                          @RequestPart(value = "file", required = false) MultipartFile file) {

        MatePostResponse response = matePostService.updateMatePost(member.getMemberId(), postId, request, file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    // 메이트 게시글 모집 상태 변경
    @PatchMapping("/{postId}/status")
    @Operation(summary = "메이트 구인글 모집상태 변경", description = "메이트 구인글 채팅방에서 모집상태를 변경합니다.")
    public ResponseEntity<ApiResponse<MatePostResponse>> updateMatePostStatus(  @AuthenticationPrincipal AuthMember member,
                                                                                @Parameter(description = "구인글 ID", required = true)
                                                                                @PathVariable(value = "postId") Long postId,
                                                                                @Parameter(description = "변경할 모집상태와 현재 참여자 리스트 ID", required = true)
                                                                                @Valid @RequestBody MatePostStatusRequest request) {

        MatePostResponse response = matePostService.updateMatePostStatus(member.getMemberId(), postId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "메이트 구인글 삭제", description = "메이트 구인글 상세 페이지에서 삭제합니다.")
    public ResponseEntity<Void> deleteMatePost(  @AuthenticationPrincipal AuthMember member,
                                                 @Parameter(description = "삭제할 구인글 ID", required = true)
                                                 @PathVariable Long postId) {

        matePostService.deleteMatePost(member.getMemberId(), postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/complete")
    @Operation(summary = "직관완료 처리", description = "메이트 구인글 채팅방에서 직관완료 처리를 진행합니다.")
    public ResponseEntity<ApiResponse<MatePostCompleteResponse>> completeVisit(  @AuthenticationPrincipal AuthMember member,
                                                                                 @Parameter(description = "구인글 ID", required = true)
                                                                                 @PathVariable Long postId,
                                                                                 @Parameter(description = "실제 직관 참여자 리스트 ID", required = true)
                                                                                 @Valid @RequestBody MatePostCompleteRequest request) {

        MatePostCompleteResponse response = matePostService.completeVisit(member.getMemberId(), postId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}