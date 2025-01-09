package com.example.mate.domain.mateReview.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.domain.mateReview.dto.request.MateReviewCreateRequest;
import com.example.mate.domain.mateReview.dto.response.MateReviewCreateResponse;
import com.example.mate.domain.matePost.entity.MatePost;
import com.example.mate.domain.matePost.entity.Visit;
import com.example.mate.domain.matePost.repository.MatePostRepository;
import com.example.mate.domain.mateReview.entity.MateReview;
import com.example.mate.domain.mateReview.repository.MateReviewRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.mate.common.error.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MateReviewService {

    private final MatePostRepository matePostRepository;
    private final MemberRepository memberRepository;
    private final MateReviewRepository mateReviewRepository;

    public MateReviewCreateResponse createReview(Long postId, Long reviewerId, MateReviewCreateRequest request) {
        MatePost matePost = matePostRepository.findById(postId)
                .orElseThrow(() -> new CustomException(MATE_POST_NOT_FOUND_BY_ID));

        Member reviewer = memberRepository.findById(reviewerId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND_BY_ID));

        Member reviewee = memberRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND_BY_ID));

        validateReviewEligibility(matePost, reviewer, reviewee);

        MateReview review = matePost.getVisit().createReview(reviewer, reviewee, request);
        MateReview savedReview = mateReviewRepository.save(review);

        return MateReviewCreateResponse.from(savedReview);
    }

    private void validateReviewEligibility(MatePost matePost, Member reviewer, Member reviewee) {
        // 리뷰어와 리뷰 대상자 모두 참여자(또는 방장) 여부 검증
        validateParticipant(matePost, reviewer);
        validateParticipant(matePost, reviewee);
    }

    private void validateParticipant(MatePost matePost, Member member) {
        boolean isParticipant = isAuthor(matePost, member) ||
                isVisitParticipant(matePost.getVisit(), member);

        if (!isParticipant) {
            throw new CustomException(NOT_PARTICIPANT_OR_AUTHOR);
        }
    }

    private boolean isAuthor(MatePost matePost, Member member) {
        return matePost.getAuthor().equals(member);
    }

    private boolean isVisitParticipant(Visit visit, Member member) {
        return visit.getParticipants().stream()
                .anyMatch(part -> part.getMember().equals(member));
    }
}
