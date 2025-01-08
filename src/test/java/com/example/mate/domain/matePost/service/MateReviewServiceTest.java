package com.example.mate.domain.matePost.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.matePost.dto.request.MateReviewCreateRequest;
import com.example.mate.domain.matePost.dto.response.MateReviewCreateResponse;
import com.example.mate.domain.matePost.entity.*;
import com.example.mate.domain.matePost.repository.MateRepository;
import com.example.mate.domain.matePost.repository.MateReviewRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.mate.common.error.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MateReviewServiceTest {

    @InjectMocks
    private MatePostService matePostService;

    @Mock
    private MateRepository mateRepository;

    @Mock
    private MateReviewRepository mateReviewRepository;

    @Mock
    private MemberRepository memberRepository;

    private static final Long TEST_POST_ID = 1L;
    private static final Long TEST_REVIEWER_ID = 1L;
    private static final Long TEST_REVIEWEE_ID = 2L;

    private Member createReviewer() {
        return Member.builder()
                .id(TEST_REVIEWER_ID)
                .name("리뷰어")
                .email("reviewer@test.com")
                .nickname("리뷰어닉네임")
                .imageUrl("reviewer.jpg")
                .build();
    }

    private Member createReviewee() {
        return Member.builder()
                .id(TEST_REVIEWEE_ID)
                .name("리뷰대상자")
                .email("reviewee@test.com")
                .nickname("리뷰대상자닉네임")
                .imageUrl("reviewee.jpg")
                .build();
    }

    private MatePost createMatePost(Member author) {
        return MatePost.builder()
                .id(TEST_POST_ID)
                .author(author)
                .teamId(1L)
                .match(Match.builder().build())
                .title("테스트 제목")
                .content("테스트 내용")
                .status(Status.VISIT_COMPLETE)
                .maxParticipants(4)
                .currentParticipants(2)
                .age(Age.TWENTIES)
                .gender(Gender.ANY)
                .transport(TransportType.PUBLIC)
                .build();
    }

    private Visit createVisit(MatePost post, List<Member> participants) {
        return Visit.createForComplete(post, participants);
    }

    private MateReviewCreateRequest createRequest() {
        return MateReviewCreateRequest.builder()
                .revieweeId(TEST_REVIEWEE_ID)
                .rating(Rating.GOOD)
                .content("좋은 메이트였습니다!")
                .build();
    }

    @Nested
    @DisplayName("메이트 후기 작성")
    class CreateMateReview {

        @Test
        @DisplayName("메이트 후기 작성 성공")
        void createMateReview_Success() {
            // given
            Member reviewer = createReviewer();
            Member reviewee = createReviewee();
            MatePost matePost = createMatePost(reviewer);
            List<Member> participants = List.of(reviewer, reviewee);
            Visit visit = createVisit(matePost, participants);
            matePost.complete(participants);

            MateReviewCreateRequest request = createRequest();

            MateReview mateReview = MateReview.builder()
                    .id(1L)
                    .visit(visit)
                    .reviewer(reviewer)
                    .reviewee(reviewee)
                    .reviewContent(request.getContent())
                    .rating(request.getRating())
                    .build();

            given(mateRepository.findById(TEST_POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findById(TEST_REVIEWER_ID))
                    .willReturn(Optional.of(reviewer));
            given(memberRepository.findById(TEST_REVIEWEE_ID))
                    .willReturn(Optional.of(reviewee));
            given(mateReviewRepository.save(any(MateReview.class)))
                    .willReturn(mateReview);

            // when
            MateReviewCreateResponse response = matePostService.createReview(
                    TEST_POST_ID,
                    TEST_REVIEWER_ID,
                    request
            );

            // then
            assertThat(response.getReviewerId()).isEqualTo(TEST_REVIEWER_ID);
            assertThat(response.getRevieweeId()).isEqualTo(TEST_REVIEWEE_ID);
            assertThat(response.getRating()).isEqualTo(Rating.GOOD.getValue());
            verify(mateRepository).findById(TEST_POST_ID);
            verify(memberRepository).findById(TEST_REVIEWER_ID);
            verify(memberRepository).findById(TEST_REVIEWEE_ID);
            verify(mateReviewRepository).save(any(MateReview.class));
        }

        @Test
        @DisplayName("메이트 후기 작성 실패 - 존재하지 않는 게시글")
        void createMateReview_FailWithInvalidPost() {
            // given
            MateReviewCreateRequest request = createRequest();

            given(mateRepository.findById(TEST_POST_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> matePostService.createReview(TEST_POST_ID, TEST_REVIEWER_ID, request)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_NOT_FOUND_BY_ID);

            verify(mateRepository).findById(TEST_POST_ID);
            verify(memberRepository, never()).findById(any());
            verify(mateReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("메이트 후기 작성 실패 - 존재하지 않는 리뷰어")
        void createMateReview_FailWithInvalidReviewer() {
            // given
            Member reviewee = createReviewee();
            MatePost matePost = createMatePost(reviewee);
            MateReviewCreateRequest request = createRequest();

            given(mateRepository.findById(TEST_POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findById(TEST_REVIEWER_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> matePostService.createReview(TEST_POST_ID, TEST_REVIEWER_ID, request)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MEMBER_NOT_FOUND_BY_ID);

            verify(mateRepository).findById(TEST_POST_ID);
            verify(memberRepository).findById(TEST_REVIEWER_ID);
            verify(mateReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("메이트 후기 작성 실패 - 존재하지 않는 리뷰 대상자")
        void createMateReview_FailWithInvalidReviewee() {
            // given
            Member reviewer = createReviewer();
            MatePost matePost = createMatePost(reviewer);
            MateReviewCreateRequest request = createRequest();

            given(mateRepository.findById(TEST_POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findById(TEST_REVIEWER_ID))
                    .willReturn(Optional.of(reviewer));
            given(memberRepository.findById(TEST_REVIEWEE_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> matePostService.createReview(TEST_POST_ID, TEST_REVIEWER_ID, request)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MEMBER_NOT_FOUND_BY_ID);

            verify(mateRepository).findById(TEST_POST_ID);
            verify(memberRepository).findById(TEST_REVIEWER_ID);
            verify(memberRepository).findById(TEST_REVIEWEE_ID);
            verify(mateReviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("메이트 후기 작성 실패 - 참여하지 않은 사용자")
        void createMateReview_FailWithNonParticipant() {
            // given
            Member reviewer = createReviewer();
            Member reviewee = createReviewee();
            Member author = Member.builder().id(3L).build();

            MatePost matePost = createMatePost(author);
            List<Member> participants = List.of(author, reviewee); // reviewer는 참여자 목록에 없음
            matePost.complete(participants);

            MateReviewCreateRequest request = createRequest();

            given(mateRepository.findById(TEST_POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findById(TEST_REVIEWER_ID))
                    .willReturn(Optional.of(reviewer));
            given(memberRepository.findById(TEST_REVIEWEE_ID))
                    .willReturn(Optional.of(reviewee));

            // when & then
            assertThatThrownBy(
                    () -> matePostService.createReview(TEST_POST_ID, TEST_REVIEWER_ID, request)
            )
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", NOT_PARTICIPANT_OR_AUTHOR);

            verify(mateRepository).findById(TEST_POST_ID);
            verify(memberRepository).findById(TEST_REVIEWER_ID);
            verify(memberRepository).findById(TEST_REVIEWEE_ID);
            verify(mateReviewRepository, never()).save(any());
        }
    }
}
