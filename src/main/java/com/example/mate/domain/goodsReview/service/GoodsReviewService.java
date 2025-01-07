package com.example.mate.domain.goodsReview.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.goodsReview.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goodsReview.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goodsReview.entity.GoodsReview;
import com.example.mate.domain.goodsReview.repository.GoodsReviewRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GoodsReviewService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final GoodsReviewRepository reviewRepository;

    public GoodsReviewResponse registerGoodsReview(Long reviewerId, Long goodsPostId, GoodsReviewRequest request) {
        Member reviewer = findMemberById(reviewerId);
        GoodsPost goodsPost = findGoodsPostById(goodsPostId);
        validateReviewEligibility(goodsPost, reviewer);

        Member seller = goodsPost.getSeller();
        GoodsReview review = request.toEntity(goodsPost, reviewer, seller);
        GoodsReview savedReview = reviewRepository.save(review);
        seller.updateManner(request.getRating());
        memberRepository.save(seller);

        return GoodsReviewResponse.of(savedReview);
    }

    private GoodsPost findGoodsPostById(Long goodsPostId) {
        return goodsPostRepository.findById(goodsPostId).orElseThrow(() ->
                new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(()
                -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private void validateReviewEligibility(GoodsPost goodsPost, Member reviewer) {
        if (goodsPost.getStatus() != Status.CLOSED) {
            throw new CustomException(ErrorCode.GOODS_REVIEW_STATUS_NOT_CLOSED);
        }

        if (!goodsPost.getBuyer().equals(reviewer)) {
            throw new CustomException(ErrorCode.GOODS_REVIEW_NOT_ALLOWED_FOR_NON_BUYER);
        }

        if (reviewRepository.existsByGoodsPostIdAndReviewerId(goodsPost.getId(), reviewer.getId())) {
            throw new CustomException(ErrorCode.GOODS_REVIEW_ALREADY_EXISTS);
        }
    }
}
