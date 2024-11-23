package com.example.mate.domain.member.controller;

import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        return ResponseEntity.noContent().build();
    }

    /*
    TODO : 2024/11/24 - 굿즈거래 후기 페이징 조회
    1. JwtToken 을 통해 회원 정보 조회
    2. 회원이 받은 굿즈거래 후기 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("/review/goods")
    public ResponseEntity<Page<MyReviewResponse>> getGoodsReviews(@RequestParam int pageNumber) {
        MyReviewResponse myReviewResponse = MyReviewResponse.goodsFrom();
        List<MyReviewResponse> responses = Collections.nCopies(10, myReviewResponse);

        PageRequest pageable = PageRequest.of(pageNumber - 1, 10);
        Page<MyReviewResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(page);
    }

    /*
    TODO : 2024/11/24 - 메이트 후기 페이징 조회
    1. JwtToken 을 통해 회원 정보 조회
    2. 회원이 받은 메이트 후기 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("/review/mate")
    public ResponseEntity<Page<MyReviewResponse>> getMateReviews(@RequestParam int pageNumber) {
        MyReviewResponse myReviewResponse = MyReviewResponse.mateFrom();
        List<MyReviewResponse> responses = Collections.nCopies(10, myReviewResponse);

        PageRequest pageable = PageRequest.of(pageNumber - 1, 10);
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
    public ResponseEntity<Page<MyVisitResponse>> getMyVisits(@RequestParam int pageNumber) {
        MyVisitResponse myVisitResponse = MyVisitResponse.from();
        List<MyVisitResponse> responses = Collections.nCopies(10, myVisitResponse);

        PageRequest pageable = PageRequest.of(pageNumber - 1, 10);
        Page<MyVisitResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(page);
    }

    /*
    TODO : 2024/11/24 - 굿즈 판매기록 페이징 조회
    1. JwtToken 을 통해 회원 정보 조회
    2. 회원이 판매한 굿즈기록 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("/goods/sold")
    public ResponseEntity<Page<MyGoodsRecordResponse>> getSoldGoods(@RequestParam int pageNumber) {
        MyGoodsRecordResponse myGoodsRecordResponse = MyGoodsRecordResponse.from();
        List<MyGoodsRecordResponse> responses = Collections.nCopies(10, myGoodsRecordResponse);

        PageRequest pageable = PageRequest.of(pageNumber - 1, 10);
        Page<MyGoodsRecordResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(page);
    }

    /*
    TODO : 2024/11/24 - 굿즈 구매기록 페이징 조회
    1. JwtToken 을 통해 회원 정보 조회
    2. 회원이 구매한 굿즈기록 조회
    3. 페이징 처리 후 반환
    */
    @GetMapping("/goods/bought")
    public ResponseEntity<Page<MyGoodsRecordResponse>> getBoughtGoods(@RequestParam int pageNumber) {
        MyGoodsRecordResponse myGoodsRecordResponse = MyGoodsRecordResponse.from();
        List<MyGoodsRecordResponse> responses = Collections.nCopies(10, myGoodsRecordResponse);

        PageRequest pageable = PageRequest.of(pageNumber - 1, 10);
        Page<MyGoodsRecordResponse> page = new PageImpl<>(responses, pageable, responses.size());

        return ResponseEntity.ok(page);
    }
}
