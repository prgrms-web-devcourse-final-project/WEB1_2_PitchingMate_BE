package com.example.mate.domain.member.controller;

import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.dto.request.MemberInfoUpdateRequest;
import com.example.mate.domain.member.dto.response.JoinResponse;
import com.example.mate.domain.member.dto.response.MemberResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

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
                .team(joinRequest.getMyTeam())
                .build();

        return ResponseEntity.ok(joinResponse);
    }

    /*
    TODO : 2024/11/23 - 회원 조회
    1. JwtToken 을 통해 사용자 정보 조회
    */
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> findMemberInfo() {
        MemberResponse memberResponse = MemberResponse.builder()
                .nickname("삼성빠돌이")
                .imageUrl("default.jpg")
                .team("삼성")
                .manner(0.3f)
                .aboutMe("삼성을 사랑하는 삼성빠돌이입니다!")
                .followingCount(10)
                .followerCount(20)
                .build();

        return ResponseEntity.ok(memberResponse);
    }

    /*
    TODO : 2024/11/23 - 회원 정보 수정
    1. JwtToken 을 통해 사용자 정보 조회
    2. nickname, profileImage, aboutMe, myTeam 수정
    3. 회원 정보 update 및 저장
    */
    @PutMapping("/me")
    public ResponseEntity<MemberResponse> updateMemberInfo(@RequestBody MemberInfoUpdateRequest updateRequest) {

        String nickname = updateRequest.getNickname() != null ? updateRequest.getNickname() : "삼성빠돌이";
        String imageUrl = (updateRequest.getImage() != null && updateRequest.getImage().getOriginalFilename() != null)
                ? "upload/" + updateRequest.getImage().getOriginalFilename() : "upload/defaultImage.jpg";
        String myTeam = updateRequest.getMyTeam() != null ? updateRequest.getMyTeam() : "삼성";
        String aboutMe = updateRequest.getAboutMe() != null ? updateRequest.getAboutMe() : "삼성을 사랑하는 삼성빠돌이입니다!";

        MemberResponse memberResponse = MemberResponse.builder()
                .nickname(nickname)
                .imageUrl(imageUrl)
                .team(myTeam)
                .manner(0.3f)
                .aboutMe(aboutMe)
                .followingCount(10)
                .followerCount(20)
                .build();

        return ResponseEntity.ok(memberResponse);
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
