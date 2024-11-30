package com.example.mate.domain.member.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class FollowController {

    private final FollowService followService;

    /*
    TODO : 2024/11/29 - 회원 팔로우 기능
    1. JwtToken 을 통해 사용자 정보 조회 - 현재는 임시로 @RequestParam 사용
    */
    @Operation(summary = "회원 팔로우 기능")
    @PostMapping("/follow/{memberId}")
    public ResponseEntity<ApiResponse<Void>> followMember(
            @Parameter(description = "팔로우할 회원 ID") @PathVariable Long memberId,
            @Parameter(description = "팔로우하는 회원 ID") @RequestParam Long followerId) {
        followService.follow(followerId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /*
    TODO : 2024/11/29 - 회원 언팔로우 기능
    1. JwtToken 을 통해 사용자 정보 조회 - 현재는 임시로 @RequestParam 사용
    */
    @Operation(summary = "회원 언팔로우 기능")
    @DeleteMapping("/follow/{memberId}")
    public ResponseEntity<Void> unfollowMember(
            @Parameter(description = "언팔로우할 회원 ID") @PathVariable Long memberId,
            @Parameter(description = "언팔로우하는 회원 ID") @RequestParam Long unfollowerId) {
        followService.unfollow(unfollowerId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "특정 회원이 팔로우하는 회원 리스트 페이징 조회")
    @GetMapping("{memberId}/followings")
    public ResponseEntity<ApiResponse<PageResponse<MemberSummaryResponse>>> getFollowings(
            @Parameter(description = "특정 회원 ID") @PathVariable Long memberId,
            @Parameter(description = "페이지 번호") @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @Parameter(description = "페이지 크기") @RequestParam(required = false, defaultValue = "10") int pageSize
    ) {
        PageResponse<MemberSummaryResponse> response = followService.getFollowingsPage(memberId, pageNumber, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /*
    TODO : 2024/11/25 - 특정 사용자를 팔로우하는 회원들 페이징 조회
    1. memberId 을 통해 회원 정보 조회
    2. 회원을 팔로우하는 회원 정보 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("{memberId}/followers")
    public ResponseEntity<Page<MemberSummaryResponse>> getFollowers(
            @PathVariable Long memberId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
//        MemberSummaryResponse response = MemberSummaryResponse.from();
//        List<MemberSummaryResponse> responses = Collections.nCopies(10, response);
//        Page<MemberSummaryResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(null);
    }
}
