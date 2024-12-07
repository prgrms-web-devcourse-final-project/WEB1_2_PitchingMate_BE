package com.example.mate.domain.member.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.dto.request.MemberInfoUpdateRequest;
import com.example.mate.domain.member.dto.request.MemberLoginRequest;
import com.example.mate.domain.member.dto.response.JoinResponse;
import com.example.mate.domain.member.dto.response.MemberLoginResponse;
import com.example.mate.domain.member.dto.response.MemberProfileResponse;
import com.example.mate.domain.member.dto.response.MyProfileResponse;
import com.example.mate.domain.member.service.LogoutRedisService;
import com.example.mate.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member Controller", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;
    private final LogoutRedisService logoutRedisService;

    @Operation(summary = "자체 회원가입 기능")
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<JoinResponse>> join(
            @Parameter(description = "소셜 로그인 정보와 사용자 추가 입력 정보") @RequestBody @Valid JoinRequest joinRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.join(joinRequest)));
    }

    @Operation(summary = "CATCH Mi 서비스 로그인", description = "캐치미 서비스에 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberLoginResponse>> catchMiLogin(
            @Parameter(description = "회원 로그인 요청 정보", required = true) @Valid @RequestBody MemberLoginRequest request
    ) {
        MemberLoginResponse response = memberService.loginByEmail(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "CATCH Mi 서비스 로그아웃", description = "캐치미 서비스에 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> catchMiLogout(
            @Parameter(description = "회원 로그인 토큰 헤더", required = true) @RequestHeader("Authorization") String token
    ) {
        logoutRedisService.addTokenToBlacklist(token);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> findMyInfo(
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMyProfile(authMember.getMemberId())));
    }

    @Operation(summary = "다른 회원 프로필 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> findMemberInfo(
            @Parameter(description = "회원 ID") @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberProfile(memberId)));
    }

    @Operation(summary = "회원 내 정보 수정")
    @PutMapping(value = "/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> updateMemberInfo(
            @Parameter(description = "프로필 사진") @RequestPart(value = "file", required = false) MultipartFile image,
            @Parameter(description = "수정할 회원 정보") @Valid @RequestPart(value = "data") MemberInfoUpdateRequest updateRequest,
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember) {
        updateRequest.setMemberId(authMember.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(memberService.updateMyProfile(image, updateRequest)));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember) {
        memberService.deleteMember(authMember.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
