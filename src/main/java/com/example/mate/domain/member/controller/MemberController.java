package com.example.mate.domain.member.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.dto.request.MemberInfoUpdateRequest;
import com.example.mate.domain.member.dto.response.JoinResponse;
import com.example.mate.domain.member.dto.response.MemberProfileResponse;
import com.example.mate.domain.member.dto.response.MyProfileResponse;
import com.example.mate.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member Controller", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    /*
        TODO : 2024/11/23 - 소셜 회원가입 후, 자체 회원가입 기능
        1. JwtToken 을 통해 사용자 정보 조회
        2. nickname, myTeam 정보 저장
        */
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> join(
            @RequestBody JoinRequest joinRequest
    ) {
        JoinResponse joinResponse = JoinResponse.builder()
                .name("홍길동")
                .nickname(joinRequest.getNickname())
                .email("test@gmail.com")
                .age(25)
                .gender("FEMALE")
                .team("삼성")
                .build();

        return ResponseEntity.ok(joinResponse);
    }

    /*
    TODO : 2024/11/23 - 내 프로필 조회
    1. JwtToken 을 통해 사용자 정보 조회
    */
    @GetMapping("/me")
    public ResponseEntity<MyProfileResponse> findMemberInfo() {
        MyProfileResponse myProfileResponse = MyProfileResponse.builder()
                .nickname("삼성빠돌이")
                .imageUrl("default.jpg")
                .teamName("삼성")
                .manner(0.3f)
                .aboutMe("삼성을 사랑하는 삼성빠돌이입니다!")
                .followingCount(10)
                .followerCount(20)
                .reviewsCount(10)
                .goodsSoldCount(20)
                .goodsBoughtCount(10)
                .visitsCount(20)
                .build();

        return ResponseEntity.ok(myProfileResponse);
    }

    @Operation(summary = "다른 회원 프로필 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> findMemberInfo(
            @Parameter(description = "회원 ID") @PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberProfile(memberId)));
    }

    /*
    TODO : 2024/11/23 - 회원 정보 수정
    1. JwtToken 을 통해 사용자 정보 조회
    2. nickname, profileImage, aboutMe, myTeam 수정
    3. 회원 정보 update 및 저장
    */
    @PutMapping(value = "/me")
    public ResponseEntity<MyProfileResponse> updateMemberInfo(
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "data") MemberInfoUpdateRequest updateRequest) {

        String imageUrl = (image != null && image.getOriginalFilename() != null)
                ? "upload/" + image.getOriginalFilename() : "upload/defaultImage.jpg";
        String nickname = updateRequest.getNickname() != null ? updateRequest.getNickname() : "삼성빠돌이";
        String myTeam = updateRequest.getTeamId() != null ? "삼성" : "한화";
        String aboutMe = updateRequest.getAboutMe() != null ? updateRequest.getAboutMe() : "삼성을 사랑하는 삼성빠돌이입니다!";

        MyProfileResponse myProfileResponse = MyProfileResponse.builder()
                .nickname(nickname)
                .imageUrl(imageUrl)
                .teamName(myTeam)
                .manner(0.3f)
                .aboutMe(aboutMe)
                .followingCount(10)
                .followerCount(20)
                .build();

        return ResponseEntity.ok(myProfileResponse);
    }

    /*
    TODO : 2024/11/23 - 회원 삭제
    1. JwtToken 을 통해 사용자 정보 조회
    2. 회원 삭제
    */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMember() {
        return ResponseEntity.noContent().build();
    }
}
