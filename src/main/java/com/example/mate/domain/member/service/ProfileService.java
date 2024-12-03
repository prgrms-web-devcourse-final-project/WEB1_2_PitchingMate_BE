package com.example.mate.domain.member.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goods.repository.GoodsReviewRepositoryCustom;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.mate.entity.MateReview;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.mate.repository.MateReviewRepositoryCustom;
import com.example.mate.domain.mate.repository.VisitPartRepository;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.dto.response.MyTimelineResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse;
import com.example.mate.domain.member.dto.response.MyVisitResponse.MateReviewResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.example.mate.domain.member.repository.TimelineRepositoryCustom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final MateReviewRepositoryCustom mateReviewRepositoryCustom;
    private final GoodsReviewRepositoryCustom goodsReviewRepositoryCustom;
    private final TimelineRepositoryCustom timelineRepositoryCustom;
    private final MateRepository mateRepository;
    private final VisitPartRepository visitPartRepository;
    private final MateReviewRepository mateReviewRepository;

    // 굿즈 판매기록 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyGoodsRecordResponse> getSoldGoodsPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<GoodsPost> soldGoodsPage = goodsPostRepository.findGoodsPostsBySellerId(memberId, Status.CLOSED,
                pageable);

        List<MyGoodsRecordResponse> content = soldGoodsPage.getContent().stream()
                .map(this::convertToRecordResponse).toList();

        return PageResponse.<MyGoodsRecordResponse>builder()
                .content(content)
                .totalPages(soldGoodsPage.getTotalPages())
                .totalElements(soldGoodsPage.getTotalElements())
                .hasNext(soldGoodsPage.hasNext())
                .pageNumber(soldGoodsPage.getNumber())
                .pageSize(soldGoodsPage.getSize())
                .build();
    }

    // 굿즈 구매기록 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyGoodsRecordResponse> getBoughtGoodsPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<GoodsPost> boughtGoodsPage = goodsPostRepository.findGoodsPostsByBuyerId(memberId, Status.CLOSED,
                pageable);

        List<MyGoodsRecordResponse> content = boughtGoodsPage.getContent().stream()
                .map(this::convertToRecordResponse).toList();

        return PageResponse.<MyGoodsRecordResponse>builder()
                .content(content)
                .totalPages(boughtGoodsPage.getTotalPages())
                .totalElements(boughtGoodsPage.getTotalElements())
                .hasNext(boughtGoodsPage.hasNext())
                .pageNumber(boughtGoodsPage.getNumber())
                .pageSize(boughtGoodsPage.getSize())
                .build();
    }

    private void validateMemberId(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private MyGoodsRecordResponse convertToRecordResponse(GoodsPost goodsPost) {
        String mainImageUrl = getMainImageUrl(goodsPost);
        return MyGoodsRecordResponse.of(goodsPost, mainImageUrl);
    }

    private String getMainImageUrl(GoodsPost goodsPost) {
        return goodsPost.getGoodsPostImages().stream()
                .filter(GoodsPostImage::getIsMainImage)
                .findFirst()
                .map(GoodsPostImage::getImageUrl)
                .orElse("upload/default.jpg");
    }

    // 메이트 후기 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyReviewResponse> getMateReviewPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<MyReviewResponse> mateReviewPage = mateReviewRepositoryCustom.findMateReviewsByRevieweeId(
                memberId, pageable);

        return PageResponse.<MyReviewResponse>builder()
                .content(mateReviewPage.getContent())
                .totalPages(mateReviewPage.getTotalPages())
                .totalElements(mateReviewPage.getTotalElements())
                .hasNext(mateReviewPage.hasNext())
                .pageNumber(mateReviewPage.getNumber())
                .pageSize(mateReviewPage.getSize())
                .build();
    }

    // 굿즈거래 후기 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyReviewResponse> getGoodsReviewPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<MyReviewResponse> goodsReviewPage = goodsReviewRepositoryCustom.findGoodsReviewsByRevieweeId(
                memberId, pageable);

        return PageResponse.<MyReviewResponse>builder()
                .content(goodsReviewPage.getContent())
                .totalPages(goodsReviewPage.getTotalPages())
                .totalElements(goodsReviewPage.getTotalElements())
                .hasNext(goodsReviewPage.hasNext())
                .pageNumber(goodsReviewPage.getNumber())
                .pageSize(goodsReviewPage.getSize())
                .build();
    }

    // TODO : 쿼리가 너무 많이 나오는 문제 -> 멘토링 및 리팩토링 필요
    // 직관 타임라인 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<MyVisitResponse> getMyVisitPage(Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        // 회원이 참여한 직관을 페이징하여 가져오기
        Page<MyTimelineResponse> visitsByIdPage = timelineRepositoryCustom.findVisitsById(memberId, pageable);

        // 응답 객체 생성
        List<MyVisitResponse> responses = visitsByIdPage.getContent().stream()
                .map(response -> createVisitResponse(response, memberId))
                .collect(Collectors.toList());

        // 페이징 정보 처리
        return createPageResponse(visitsByIdPage, responses, pageable);
    }

    private MyVisitResponse createVisitResponse(MyTimelineResponse response, Long memberId) {
        // 경기 정보 가져오기
        Match match = mateRepository.findMatchByMatePostId(response.getMatePostId());

        // 회원 본인을 제외한 직관 참여 리스트 가져오기
        List<Member> mates = visitPartRepository.findMembersByVisitIdExcludeMember(response.getVisitId(), memberId);

        // 각 메이트에 대한 리뷰 생성
        List<MateReviewResponse> reviews = createMateReviews(response, mates, memberId);

        return MyVisitResponse.of(match, reviews);
    }

    private List<MateReviewResponse> createMateReviews(MyTimelineResponse response, List<Member> mates, Long memberId) {
        return mates.stream()
                .map(mate -> {
                    Optional<MateReview> mateReview = mateReviewRepository.findMateReviewByVisitIdAndReviewerIdAndRevieweeId(
                            response.getVisitId(), memberId, mate.getId());
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
        int start = Math.min(pageNumber * pageSize, totalElements);
        int end = Math.min(start + pageSize, totalElements);
        List<MyVisitResponse> content = responses.subList(start, end);

        return PageResponse.<MyVisitResponse>builder()
                .content(content)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .hasNext(pageNumber + 1 < totalPages)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
    }

}