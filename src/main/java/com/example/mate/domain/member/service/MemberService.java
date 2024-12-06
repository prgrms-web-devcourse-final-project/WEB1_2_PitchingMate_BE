package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.jwt.JwtToken;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileService;
import com.example.mate.domain.file.FileValidator;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goods.repository.GoodsReviewRepository;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.mate.repository.VisitPartRepository;
import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.dto.request.MemberInfoUpdateRequest;
import com.example.mate.domain.member.dto.request.MemberLoginRequest;
import com.example.mate.domain.member.dto.response.JoinResponse;
import com.example.mate.domain.member.dto.response.MemberLoginResponse;
import com.example.mate.domain.member.dto.response.MemberProfileResponse;
import com.example.mate.domain.member.dto.response.MyProfileResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final GoodsReviewRepository goodsReviewRepository;
    private final MateReviewRepository mateReviewRepository;
    private final VisitPartRepository visitPartRepository;
    private final JwtUtil jwtUtil;
    private final FileService fileService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 자체 회원가입 기능
    public JoinResponse join(JoinRequest request) {
        Member savedMember = memberRepository.save(Member.of(request, getDefaultMemberImageUrl()));
        return JoinResponse.from(savedMember);
    }

    // 자체 로그인 기능
    @Transactional(readOnly = true)
    public MemberLoginResponse loginByEmail(MemberLoginRequest request) {
        Member member = findByEmail(request.getEmail());
        return MemberLoginResponse.from(member, makeToken(member));
    }

    // JWT 토큰 생성
    private JwtToken makeToken(Member member) {
        Map<String, Object> payloadMap = member.getPayload();
        String accessToken = jwtUtil.createToken(payloadMap, 60 * 24 * 3); // 3일 유효
        String refreshToken = jwtUtil.createToken(Map.of("memberId", member.getId()), 60 * 24 * 7); // 7일 유효
        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_EMAIL));
    }

    // 내 프로필 조회
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long memberId) {
        return getProfile(memberId, MyProfileResponse.class);
    }

    // 다른 회원 프로필 조회
    @Transactional(readOnly = true)
    public MemberProfileResponse getMemberProfile(Long memberId) {
        return getProfile(memberId, MemberProfileResponse.class);
    }

    // 회원 정보 수정
    public MyProfileResponse updateMyProfile(MultipartFile file, MemberInfoUpdateRequest request) {
        Member member = findByMemberId(request.getMemberId());

        checkNicknameAndChange(member, request.getNickname()); // 닉네임 중복 검증한 뒤 바뀐 경우에만 수정
        member.changeTeam(TeamInfo.getById(request.getTeamId()));
        member.changeAboutMe(request.getAboutMe());

        if (file != null && !file.isEmpty()) {
            FileValidator.validateSingleImage(file);
            deleteNonDefaultImage(member.getImageUrl());
            member.changeImageUrl(fileService.uploadFile(file));
        }

        return MyProfileResponse.from(memberRepository.save(member));
    }

    // 회원 탈퇴
    public void deleteMember(Long memberId) {
        Member member = findByMemberId(memberId);
        deleteNonDefaultImage(member.getImageUrl());
        memberRepository.deleteById(memberId);
    }

    private void deleteNonDefaultImage(String imageUrl) {
        if (!imageUrl.equals(getDefaultMemberImageUrl())) {
            fileService.deleteFile(imageUrl);
        }
    }

    private String getDefaultMemberImageUrl() {
        return "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/member_default.svg";
    }

    private void checkNicknameAndChange(Member member, String request) {
        if (member.getNickname().equals(request)) {
            return;
        }
        if (memberRepository.existsByNickname(request)) {
            throw new CustomException(ErrorCode.ALREADY_USED_NICKNAME);
        }
        member.changeNickname(request);
    }

    // 공통 프로필 생성 팩토리 메서드. DTO 클래스 타입에 따라 다른 타입 리턴
    private <T> T getProfile(Long memberId, Class<T> responseType) {
        Member member = findByMemberId(memberId);
        int followCount = followRepository.countByFollowerId(memberId);
        int followerCount = followRepository.countByFollowingId(memberId);
        int reviewsCount = goodsReviewRepository.countByRevieweeId(memberId) +
                mateReviewRepository.countByRevieweeId(memberId);
        int goodsSoldCount = goodsPostRepository.countGoodsPostsBySellerIdAndStatus(memberId, Status.CLOSED);

        if (responseType == MemberProfileResponse.class) { // MemberProfileResponse가 요청된 경우, 불필요한 조회를 생략하고 바로 리턴
            return responseType.cast(
                    MemberProfileResponse.of(member, followCount, followerCount, reviewsCount, goodsSoldCount));
        }

        if (responseType == MyProfileResponse.class) { // MyProfileResponse가 요청된 경우, 추가적인 조회를 수행
            int goodsBoughtCount = goodsPostRepository.countGoodsPostsByBuyerIdAndStatus(memberId, Status.CLOSED);
            int visitsCount = visitPartRepository.countByMember(member);
            return responseType.cast(
                    MyProfileResponse.of(member, followCount, followerCount, reviewsCount,
                            goodsSoldCount, goodsBoughtCount, visitsCount));
        }

        throw new CustomException(ErrorCode.UNSUPPORTED_RESPONSE_TYPE);
    }

    private Member findByMemberId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }
}