package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goods.repository.GoodsReviewRepositoryCustom;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.mate.entity.MateReview;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.mate.repository.MateReviewRepositoryCustom;
import com.example.mate.domain.mate.repository.VisitPartRepository;
import com.example.mate.domain.mate.repository.VisitRepository;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.dto.response.MyTimelineResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse.MateReviewResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final MateReviewRepositoryCustom mateReviewRepositoryCustom;
    private final GoodsReviewRepositoryCustom goodsReviewRepositoryCustom;
    private final VisitPartRepository visitPartRepository;
    private final MateReviewRepository mateReviewRepository;
    private final VisitRepository visitRepository;

    // 굿즈 판매기록 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyGoodsRecordResponse> getSoldGoodsPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<GoodsPost> soldGoodsPage = goodsPostRepository.findGoodsPostsBySellerId(memberId, Status.CLOSED,
                pageable);

        List<MyGoodsRecordResponse> content = soldGoodsPage.getContent().stream()
                .map(this::convertToRecordResponse).toList();

        return PageResponse.from(soldGoodsPage, content);
    }

    // 굿즈 구매기록 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyGoodsRecordResponse> getBoughtGoodsPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<GoodsPost> boughtGoodsPage = goodsPostRepository.findGoodsPostsByBuyerId(memberId, Status.CLOSED,
                pageable);

        List<MyGoodsRecordResponse> content = boughtGoodsPage.getContent().stream()
                .map(this::convertToRecordResponse).toList();

        return PageResponse.from(boughtGoodsPage, content);
    }

    private void validateMemberId(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private MyGoodsRecordResponse convertToRecordResponse(GoodsPost goodsPost) {
        String mainImageUrl = goodsPost.getMainImageUrl();
        return MyGoodsRecordResponse.of(goodsPost, mainImageUrl);
    }

    // 메이트 후기 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyReviewResponse> getMateReviewPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<MyReviewResponse> mateReviewPage = mateReviewRepositoryCustom.findMateReviewsByRevieweeId(
                memberId, pageable);

        return PageResponse.from(mateReviewPage);
    }

    // 굿즈거래 후기 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyReviewResponse> getGoodsReviewPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<MyReviewResponse> goodsReviewPage = goodsReviewRepositoryCustom.findGoodsReviewsByRevieweeId(
                memberId, pageable);

        return PageResponse.from(goodsReviewPage);
    }

    // 직관 타임라인 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyVisitResponse> getMyVisitPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        // 회원이 참여한 직관 목록을 페이지네이션
        Page<MyTimelineResponse> visitsByMemberIdPage = visitRepository.findVisitsByMemberId(memberId, pageable);

        // 회원이 참여한 경기 정보 리스트
        List<Match> matchesByMatePostId = visitRepository.findMatchesByMemberId(memberId);

        // 각각의 직관 목록과 경기 정보에 따른 응답 객체 생성 및 주입
        List<MyVisitResponse> responses = new ArrayList<>();
        for (int i = 0; i < visitsByMemberIdPage.getContent().size(); i++) {
            MyTimelineResponse response = visitsByMemberIdPage.getContent().get(i);
            Match match = matchesByMatePostId.get(i);
            responses.add(createVisitResponse(response, memberId, match));
        }

        // 페이지네이션
        return createPageResponse(visitsByMemberIdPage, responses, pageable);
    }

    private MyVisitResponse createVisitResponse(MyTimelineResponse response, Long memberId, Match match) {
        // 회원이 참여한 직관에서 메이트에 남긴 모든 리뷰 리스트
        List<MateReview> existReviews = mateReviewRepository
                .findMateReviewsByVisitIdAndReviewerId(response.getVisitId(), memberId);

        // 회원 본인을 제외한 직관 참여 메이트 리스트
        List<Member> mates = visitPartRepository.findMembersByVisitIdExcludeMember(response.getVisitId(), memberId);

        // 각 메이트에 대한 리뷰 여부에 따른 응답 리뷰 리스트
        List<MateReviewResponse> reviews = createMateReviews(response, mates, memberId, existReviews);

        return MyVisitResponse.of(match, reviews, response.getMatePostId());
    }

    private List<MateReviewResponse> createMateReviews(MyTimelineResponse response, List<Member> mates,
                                                       Long memberId, List<MateReview> existReviews) {
        return mates.stream()
                .map(mate -> {
                    Optional<MateReview> mateReview = existReviews.stream()
                            .filter(review -> review.getVisit().getId().equals(response.getVisitId()) &&
                                    review.getReviewer().getId().equals(memberId) &&
                                    review.getReviewee().getId().equals(mate.getId()))
                            .findFirst(); // 해당 조건에 맞는 리뷰를 찾기
                    return mateReview.map(MateReviewResponse::from) // 해당 mate에 대한 리뷰가 있으면 리뷰 채워서 반환
                            .orElseGet(() -> MateReviewResponse.from(mate)); // 리뷰가 없으면 rating, content = null
                })
                .collect(Collectors.toList());
    }

    private PageResponse<MyVisitResponse> createPageResponse(Page<MyTimelineResponse> visitsByIdPage,
                                                             List<MyVisitResponse> responses, Pageable pageable) {
        int totalElements = (int) visitsByIdPage.getTotalElements();
        int totalPages = visitsByIdPage.getTotalPages();

        // 페이징 처리
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        return PageResponse.<MyVisitResponse>builder()
                .content(responses)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .hasNext(pageNumber + 1 < totalPages)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }
}