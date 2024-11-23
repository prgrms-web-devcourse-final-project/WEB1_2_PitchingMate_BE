package com.example.mate.domain.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    /*
    TODO : 2024/11/23 - 회원 팔로우 기능
    1. JwtToken 을 통해 사용자 정보 조회
    2. 팔로우 여부 확인
    3. memberId 회원 유효성 검사
    4. 팔로우 처리
    */
    @PostMapping("/follow/{memberId}")
    public ResponseEntity<Void> followMember(@PathVariable Long memberId) {
        return ResponseEntity.ok().build();
    }

    /*
    TODO : 2024/11/23 - 회원 언팔로우 기능
    1. JwtToken 을 통해 사용자 정보 조회
    2. 팔로우 여부 확인
    3. memberId 회원 유효성 검사
    4. 언팔로우 처리
    */
    @DeleteMapping("/follow/{memberId}")
    public ResponseEntity<Void> unfollowMember(@PathVariable Long memberId) {
        return ResponseEntity.ok().build();
    }
}
