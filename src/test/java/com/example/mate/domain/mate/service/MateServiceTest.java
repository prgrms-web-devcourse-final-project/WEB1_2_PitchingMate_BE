package com.example.mate.domain.mate.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.mate.common.error.ErrorCode.MATCH_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.MEMBER_NOT_FOUND_BY_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MateServiceTest {

    @InjectMocks
    private MateService mateService;

    @Mock
    private MateRepository mateRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MemberRepository memberRepository;

    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_MATCH_ID = 1L;

    private Member createTestMember() {
        return Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .nickname("테스트계정")
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

    private MatePostCreateRequest createTestRequest() {
        return MatePostCreateRequest.builder()
                .memberId(TEST_MEMBER_ID)
                .teamId(TEST_MATCH_ID)
                .matchId(1L)
                .title("테스트 제목")
                .content("테스트 내용")
                .age(Age.TWENTIES)
                .maxParticipants(4)
                .gender(Gender.FEMALE)
                .transportType(TransportType.PUBLIC)
                .build();
    }

    private MatePost createTestMatePost(Member author, Match match) {
        return MatePost.builder()
                .id(1L)
                .author(author)
                .teamId(1L)
                .match(match)
                .title("테스트 제목")
                .content("테스트 내용")
                .status(Status.OPEN)
                .maxParticipants(4)
                .age(Age.TWENTIES)
                .gender(Gender.FEMALE)
                .transport(TransportType.PUBLIC)
                .build();
    }

    @Test
    @DisplayName("메이트 게시글 작성 성공")
    void createMatePost_Success() {
        // given
        Member testMember = createTestMember();
        Match testMatch = createTestMatch();
        MatePostCreateRequest request = createTestRequest();
        MatePost matePost = createTestMatePost(testMember, testMatch);

        given(memberRepository.findById(request.getMemberId()))
                .willReturn(Optional.of(testMember));
        given(matchRepository.findById(request.getMatchId()))
                .willReturn(Optional.of(testMatch));
        given(mateRepository.save(any(MatePost.class)))
                .willReturn(matePost);

        // when
        MatePostResponse response = mateService.createMatePost(request, null);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.OPEN);

        verify(memberRepository).findById(TEST_MEMBER_ID);
        verify(matchRepository).findById(TEST_MATCH_ID);
        verify(mateRepository).save(any(MatePost.class));
    }

    @Test
    @DisplayName("메이트 게시글 작성 실패 - 존재하지 않는 회원")
    void createMatePost_FailWithInvalidMember() {
        // given
        MatePostCreateRequest request = createTestRequest();
        given(memberRepository.findById(request.getMemberId()))
                .willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> mateService.createMatePost(request, null));
        assertThat(exception.getErrorCode()).isEqualTo(MEMBER_NOT_FOUND_BY_ID);

        verify(memberRepository).findById(TEST_MEMBER_ID);
        verify(matchRepository, never()).findById(any());
        verify(mateRepository, never()).save(any());
    }

    @Test
    @DisplayName("메이트 게시글 작성 실패 - 존재하지 않는 경기")
    void createMatePost_FailWithInvalidMatch() {
        // given
        Member testMember = createTestMember();
        MatePostCreateRequest request = createTestRequest();

        given(memberRepository.findById(request.getMemberId()))
                .willReturn(Optional.of(testMember));
        given(matchRepository.findById(request.getMatchId()))
                .willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> mateService.createMatePost(request, null));
        assertThat(exception.getErrorCode()).isEqualTo(MATCH_NOT_FOUND_BY_ID);

        verify(memberRepository).findById(TEST_MEMBER_ID);
        verify(matchRepository).findById(TEST_MATCH_ID);
        verify(mateRepository, never()).save(any());
    }
}