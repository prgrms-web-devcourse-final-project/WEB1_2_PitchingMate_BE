package com.example.mate.domain.mate.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.mate.dto.request.MatePostCompleteRequest;
import com.example.mate.domain.mate.dto.request.MatePostStatusRequest;
import com.example.mate.domain.mate.dto.response.MatePostCompleteResponse;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
class MateStatusServiceTest {

    @InjectMocks
    private MateService mateService;

    @Mock
    private MateRepository mateRepository;

    @Mock
    private MemberRepository memberRepository;

    private static final Long TEST_MEMBER_ID = 1L;

    private Member createTestMember() {
        return Member.builder()
                .id(TEST_MEMBER_ID)
                .name("테스트유저")
                .email("test@test.com")
                .nickname("테스트계정")
                .imageUrl("test.jpg")
                .build();
    }

    private Match createTestMatch() {
        return Match.builder()
                .homeTeamId(1L)
                .awayTeamId(2L)
                .stadiumId(1L)
                .matchTime(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Nested
    @DisplayName("메이트 게시글 모집 상태 변경")
    class UpdateMatePostStatus {
        private static final Long POST_ID = 1L;

        @Test
        @DisplayName("메이트 게시글 모집 상태 변경 성공 - OPEN에서 CLOSED로 변경")
        void updateMatePostStatus_Success() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();
            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            List<Long> participantIds = Arrays.asList(2L, 3L);
            List<Member> participants = Arrays.asList(
                    Member.builder().id(2L).build(),
                    Member.builder().id(3L).build()
            );

            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findAllById(participantIds))
                    .willReturn(participants);

            // when
            MatePostResponse response = mateService.updateMatePostStatus(TEST_MEMBER_ID, POST_ID, request);

            // then
            assertThat(response.getId()).isEqualTo(POST_ID);
            assertThat(response.getStatus()).isEqualTo(Status.CLOSED);
            verify(mateRepository).findById(POST_ID);
            verify(memberRepository).findAllById(participantIds);
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 존재하지 않는 게시글")
        void updateMatePostStatus_FailWithInvalidPostId() {
            // given
            List<Long> participantIds = Arrays.asList(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);
            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePostStatus(TEST_MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_NOT_FOUND_BY_ID);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 작성자가 아닌 경우")
        void updateMatePostStatus_FailWithNotAuthor() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();
            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            Long differentMemberId = 999L;
            List<Long> participantIds = Arrays.asList(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePostStatus(differentMemberId, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_UPDATE_NOT_ALLOWED);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - COMPLETE로 변경 시도")
        void updateMatePostStatus_FailWithCompleteStatus() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();
            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            List<Long> participantIds = Arrays.asList(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.VISIT_COMPLETE, participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePostStatus(TEST_MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", DIRECT_VISIT_COMPLETE_FORBIDDEN);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 이미 완료된 게시글")
        void updateMatePostStatus_FailWithAlreadyCompleted() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();
            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.VISIT_COMPLETE)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            List<Long> participantIds = Arrays.asList(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePostStatus(TEST_MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ALREADY_COMPLETED_POST);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 존재하지 않는 참여자 ID")
        void updateMatePostStatus_FailWithInvalidParticipantIds() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();
            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            List<Long> participantIds = Arrays.asList(2L, 3L);
            List<Member> participants = Collections.singletonList(Member.builder().id(2L).build()); // 하나만 존재

            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findAllById(participantIds))
                    .willReturn(participants);

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePostStatus(TEST_MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", INVALID_MATE_POST_PARTICIPANT_IDS);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository).findAllById(participantIds);
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 최대 참여자 수 초과")
        void updateMatePostStatus_FailWithExceededMaxParticipants() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();
            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(3) // 방장 포함 3명까지
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            List<Long> participantIds = Arrays.asList(2L, 3L, 4L); // 3명 추가 시도
            List<Member> participants = Arrays.asList(
                    Member.builder().id(2L).build(),
                    Member.builder().id(3L).build(),
                    Member.builder().id(4L).build()
            );

            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findAllById(participantIds))
                    .willReturn(participants);

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePostStatus(TEST_MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_MAX_PARTICIPANTS_EXCEEDED);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository).findAllById(participantIds);
        }
    }

    @Nested
    @DisplayName("직관 완료 처리")
    class CompleteVisit {
        private static final Long POST_ID = 1L;
        private static final Long MEMBER_ID = 1L;

        private Member createParticipant(Long id) {
            return Member.builder()
                    .id(id)
                    .name("참여자" + id)
                    .email("participant" + id + "@test.com")
                    .nickname("참여자" + id)
                    .imageUrl("participant" + id + ".jpg")
                    .build();
        }

        @Test
        @DisplayName("직관 완료 처리 성공")
        void completeVisit_Success() {
            // given
            Member author = createTestMember();
            Match testMatch = Match.builder()
                    .homeTeamId(1L)
                    .awayTeamId(2L)
                    .stadiumId(1L)
                    .matchTime(LocalDateTime.now().minusHours(3)) // 경기 시작 3시간 후
                    .build();

            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(author)
                    .teamId(1L)
                    .match(testMatch)
                    .status(Status.CLOSED)
                    .maxParticipants(3)
                    .build();

            List<Long> participantIds = List.of(1L, 2L);
            List<Member> participants = participantIds.stream()
                    .map(this::createParticipant)
                    .toList();

            MatePostCompleteRequest request = new MatePostCompleteRequest(participantIds);

            given(mateRepository.findById(POST_ID)).willReturn(Optional.of(matePost));
            given(memberRepository.findAllById(participantIds)).willReturn(participants);

            // when
            MatePostCompleteResponse response = mateService.completeVisit(MEMBER_ID, POST_ID, request);

            // then
            assertThat(response.getId()).isEqualTo(POST_ID);
            assertThat(response.getStatus()).isEqualTo(Status.VISIT_COMPLETE);
            assertThat(response.getParticipantIds()).containsExactlyInAnyOrderElementsOf(participantIds);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository).findAllById(participantIds);
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 존재하지 않는 게시글")
        void completeVisit_FailPostNotFound() {
            // given
            List<Long> participantIds = List.of(1L, 2L);
            MatePostCompleteRequest request = new MatePostCompleteRequest(participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateService.completeVisit(MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_NOT_FOUND_BY_ID);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 권한 없음")
        void completeVisit_FailNotAuthorized() {
            // given
            Member author = createTestMember();
            Match testMatch = createTestMatch();
            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(author)
                    .teamId(1L)
                    .match(testMatch)
                    .status(Status.CLOSED)
                    .maxParticipants(3)
                    .build();

            Long differentMemberId = 999L;
            List<Long> participantIds = List.of(1L, 2L);
            MatePostCompleteRequest request = new MatePostCompleteRequest(participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when & then
            assertThatThrownBy(() -> mateService.completeVisit(differentMemberId, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_UPDATE_NOT_ALLOWED);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 경기 시작 전")
        void completeVisit_FailMatchNotStarted() {
            // given
            Member author = createTestMember();
            Match futureMatch = Match.builder()
                    .homeTeamId(1L)
                    .awayTeamId(2L)
                    .stadiumId(1L)
                    .matchTime(LocalDateTime.now().plusDays(1))
                    .build();

            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(author)
                    .teamId(1L)
                    .match(futureMatch)
                    .status(Status.CLOSED)
                    .maxParticipants(3)
                    .build();

            List<Long> participantIds = List.of(1L, 2L);
            MatePostCompleteRequest request = new MatePostCompleteRequest(participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when & then
            assertThatThrownBy(() -> mateService.completeVisit(MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_COMPLETE_TIME_NOT_ALLOWED);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 모집 완료 상태가 아님")
        void completeVisit_FailNotClosedStatus() {
            // given
            Member author = createTestMember();
            Match testMatch = Match.builder()
                    .homeTeamId(1L)
                    .awayTeamId(2L)
                    .stadiumId(1L)
                    .matchTime(LocalDateTime.now().minusHours(3))
                    .build();

            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(author)
                    .teamId(1L)
                    .match(testMatch)
                    .status(Status.OPEN) // 모집중 상태
                    .maxParticipants(3)
                    .build();

            List<Long> participantIds = List.of(1L, 2L);
            MatePostCompleteRequest request = new MatePostCompleteRequest(participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when & then
            assertThatThrownBy(() -> mateService.completeVisit(MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", NOT_CLOSED_STATUS_FOR_COMPLETION);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository, never()).findAllById(any());
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 존재하지 않는 참여자")
        void completeVisit_FailInvalidParticipants() {
            // given
            Member author = createTestMember();
            Match testMatch = Match.builder()
                    .homeTeamId(1L)
                    .awayTeamId(2L)
                    .stadiumId(1L)
                    .matchTime(LocalDateTime.now().minusHours(3))
                    .build();

            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(author)
                    .teamId(1L)
                    .match(testMatch)
                    .status(Status.CLOSED)
                    .maxParticipants(3)
                    .build();

            List<Long> participantIds = List.of(1L, 999L);
            List<Member> participants = List.of(createParticipant(1L)); // 한 명만 존재
            MatePostCompleteRequest request = new MatePostCompleteRequest(participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findAllById(participantIds))
                    .willReturn(participants);

            // when & then
            assertThatThrownBy(() -> mateService.completeVisit(MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", INVALID_MATE_POST_PARTICIPANT_IDS);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository).findAllById(participantIds);
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 최대 참여 인원 초과")
        void completeVisit_FailExceedMaxParticipants() {
            // given
            Member author = createTestMember();
            Match testMatch = Match.builder()
                    .homeTeamId(1L)
                    .awayTeamId(2L)
                    .stadiumId(1L)
                    .matchTime(LocalDateTime.now().minusHours(3))
                    .build();

            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(author)
                    .teamId(1L)
                    .match(testMatch)
                    .status(Status.CLOSED)
                    .maxParticipants(2)
                    .build();

            List<Long> participantIds = List.of(1L, 2L); // 2명 (방장 포함 시 최대 인원 초과)
            List<Member> participants = participantIds.stream()
                    .map(this::createParticipant)
                    .toList();
            MatePostCompleteRequest request = new MatePostCompleteRequest(participantIds);

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));
            given(memberRepository.findAllById(participantIds))
                    .willReturn(participants);

            // when & then
            assertThatThrownBy(() -> mateService.completeVisit(MEMBER_ID, POST_ID, request))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_MAX_PARTICIPANTS_EXCEEDED);

            verify(mateRepository).findById(POST_ID);
            verify(memberRepository).findAllById(participantIds);
        }
    }
}
