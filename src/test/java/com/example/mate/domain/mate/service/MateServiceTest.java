package com.example.mate.domain.mate.service;

import static com.example.mate.common.error.ErrorCode.ALREADY_COMPLETED_POST;
import static com.example.mate.common.error.ErrorCode.MATCH_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.MATE_POST_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.MATE_POST_UPDATE_NOT_ALLOWED;
import static com.example.mate.common.error.ErrorCode.MEMBER_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.TEAM_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.file.FileService;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.dto.request.MatePostSearchRequest;
import com.example.mate.domain.mate.dto.request.MatePostUpdateRequest;
import com.example.mate.domain.mate.dto.response.MatePostDetailResponse;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.dto.response.MatePostSummaryResponse;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.SortType;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.entity.Visit;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomMemberRepository;
import com.example.mate.domain.mateChat.repository.MateChatRoomRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Mock
    private FileService fileService;

    @Mock
    private MateChatRoomRepository chatRoomRepository;

    @Mock
    private MateChatRoomMemberRepository chatRoomMemberRepository;

    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_MATCH_ID = 1L;

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
    @DisplayName("메이트 게시글 작성")
    class CreateMatePost {

        @Test
        @DisplayName("메이트 게시글 작성 성공")
        void createMatePost_Success() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            MatePostCreateRequest request = MatePostCreateRequest.builder()
                    .teamId(TEST_MATCH_ID)
                    .matchId(1L)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .age(Age.TWENTIES)
                    .maxParticipants(4)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();

            MatePost matePost = MatePost.builder()
                    .id(1L)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.FEMALE)
                    .transport(TransportType.PUBLIC)
                    .build();

            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(testMember));
            given(matchRepository.findById(request.getMatchId()))
                    .willReturn(Optional.of(testMatch));
            given(mateRepository.save(any(MatePost.class)))
                    .willReturn(matePost);

            // when
            MatePostResponse response = mateService.createMatePost(request, null, TEST_MEMBER_ID);

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
            MatePostCreateRequest request = MatePostCreateRequest.builder()
                    .teamId(TEST_MATCH_ID)
                    .matchId(1L)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .age(Age.TWENTIES)
                    .maxParticipants(4)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();

            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateService.createMatePost(request, null, TEST_MEMBER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MEMBER_NOT_FOUND_BY_ID);

            verify(memberRepository).findById(TEST_MEMBER_ID);
            verify(matchRepository, never()).findById(any());
            verify(mateRepository, never()).save(any());
        }

        @Test
        @DisplayName("메이트 게시글 작성 실패 - 존재하지 않는 경기")
        void createMatePost_FailWithInvalidMatch() {
            // given
            Member testMember = createTestMember();
            MatePostCreateRequest request = MatePostCreateRequest.builder()
                    .teamId(TEST_MATCH_ID)
                    .matchId(1L)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .age(Age.TWENTIES)
                    .maxParticipants(4)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();

            given(memberRepository.findById(TEST_MEMBER_ID))
                    .willReturn(Optional.of(testMember));
            given(matchRepository.findById(request.getMatchId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateService.createMatePost(request, null, TEST_MEMBER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATCH_NOT_FOUND_BY_ID);

            verify(memberRepository).findById(TEST_MEMBER_ID);
            verify(matchRepository).findById(TEST_MATCH_ID);
            verify(mateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("메인 페이지 메이트 게시글 조회")
    class GetMatePosts {

        private List<MatePost> createTestMatePostList() {
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            return List.of(
                    MatePost.builder()
                            .id(1L)
                            .author(testMember)
                            .teamId(1L)
                            .match(testMatch)
                            .title("테스트 제목1")
                            .content("테스트 내용1")
                            .status(Status.OPEN)
                            .maxParticipants(4)
                            .age(Age.TWENTIES)
                            .gender(Gender.ANY)
                            .transport(TransportType.PUBLIC)
                            .build(),
                    MatePost.builder()
                            .id(2L)
                            .author(testMember)
                            .teamId(1L)
                            .match(testMatch)
                            .title("테스트 제목2")
                            .content("테스트 내용2")
                            .status(Status.OPEN)
                            .maxParticipants(3)
                            .age(Age.THIRTIES)
                            .gender(Gender.ANY)
                            .transport(TransportType.CAR)
                            .build()
            );
        }

        @Test
        @DisplayName("메이트 게시글 목록 조회 성공 - 팀 ID 없음")
        void getMatePostMain_SuccessWithoutTeamId() {
            // given
            List<MatePost> testPosts = createTestMatePostList();
            given(mateRepository.findMainPagePosts(
                    eq(null),
                    any(LocalDateTime.class),
                    eq(List.of(Status.OPEN, Status.CLOSED)),
                    any(Pageable.class)))
                    .willReturn(testPosts);

            // when
            List<MatePostSummaryResponse> result = mateService.getMainPagePosts(null);

            // then
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.get(0).getTitle()).isEqualTo("테스트 제목1");
            assertThat(result.get(1).getTitle()).isEqualTo("테스트 제목2");

            verify(mateRepository).findMainPagePosts(
                    eq(null),
                    any(LocalDateTime.class),
                    eq(List.of(Status.OPEN, Status.CLOSED)),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("메이트 게시글 목록 조회 성공 - 특정 팀")
        void getMatePostMain_SuccessWithTeamId() {
            // given
            Long teamId = 1L;
            List<MatePost> testPosts = createTestMatePostList();
            given(mateRepository.findMainPagePosts(
                    eq(teamId),
                    any(LocalDateTime.class),
                    eq(List.of(Status.OPEN, Status.CLOSED)),
                    any(Pageable.class)))
                    .willReturn(testPosts);

            // when
            List<MatePostSummaryResponse> result = mateService.getMainPagePosts(teamId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("title")
                    .containsExactly("테스트 제목1", "테스트 제목2");
            assertThat(result).extracting("maxParticipants")
                    .containsExactly(4, 3);

            verify(mateRepository).findMainPagePosts(
                    eq(teamId),
                    any(LocalDateTime.class),
                    eq(List.of(Status.OPEN, Status.CLOSED)),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("메이트 게시글 목록 조회 - 결과 없음")
        void getMatePostMain_EmptyResult() {
            // given
            given(mateRepository.findMainPagePosts(
                    any(),
                    any(LocalDateTime.class),
                    any(),
                    any(Pageable.class)))
                    .willReturn(Collections.emptyList());

            // when
            List<MatePostSummaryResponse> result = mateService.getMainPagePosts(1L);

            // then
            assertThat(result).isEmpty();

            verify(mateRepository).findMainPagePosts(
                    any(),
                    any(LocalDateTime.class),
                    any(),
                    any(Pageable.class));
        }

        @Test
        @DisplayName("메이트 게시글 목록 조회 실패 - 존재하지 않는 팀")
        void getMatePostMain_FailWithInvalidTeamId() {
            // given
            Long invalidTeamId = 999L;

            // when & then
            assertThatThrownBy(() -> mateService.getMainPagePosts(invalidTeamId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", TEAM_NOT_FOUND);

            verify(mateRepository, never()).findMainPagePosts(
                    any(),
                    any(LocalDateTime.class),
                    any(),
                    any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("메이트 페이지 게시글 조회")
    class GetMatePagePosts {

        private List<MatePost> createFilteredTestMatePostList() {
            Member testMember = createTestMember();
            Match testMatch = Match.builder()
                    .homeTeamId(1L)
                    .awayTeamId(2L)
                    .stadiumId(StadiumInfo.JAMSIL.id)
                    .matchTime(LocalDateTime.now().plusDays(1))
                    .build();

            return List.of(
                    MatePost.builder()
                            .author(testMember)
                            .teamId(1L)
                            .match(testMatch)
                            .title("테스트 제목1")
                            .content("테스트 내용1")
                            .status(Status.OPEN)
                            .maxParticipants(4)
                            .age(Age.TWENTIES)
                            .gender(Gender.ANY)
                            .transport(TransportType.PUBLIC)
                            .build(),
                    MatePost.builder()
                            .author(testMember)
                            .teamId(1L)
                            .match(testMatch)
                            .title("테스트 제목2")
                            .content("테스트 내용2")
                            .status(Status.OPEN)
                            .maxParticipants(3)
                            .age(Age.THIRTIES)
                            .gender(Gender.ANY)
                            .transport(TransportType.CAR)
                            .build()
            );
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 필터 없음")
        void getMatePagePosts_SuccessWithoutFilters() {
            // given
            List<MatePost> testPosts = createFilteredTestMatePostList();
            Page<MatePost> testPage = new PageImpl<>(testPosts);
            Pageable pageable = PageRequest.of(0, 10);
            MatePostSearchRequest request = MatePostSearchRequest.builder().build();

            given(mateRepository.findMatePostsByFilter(request, pageable))
                    .willReturn(testPage);

            // when
            PageResponse<MatePostSummaryResponse> result = mateService.getMatePagePosts(request, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 제목1");
            assertThat(result.getContent().get(1).getTitle()).isEqualTo("테스트 제목2");
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getPageNumber()).isZero();

            verify(mateRepository).findMatePostsByFilter(request, pageable);
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 모든 필터 적용")
        void getMatePagePosts_SuccessWithAllFilters() {
            // given
            Member testMember = createTestMember();
            Match testMatch = Match.builder()
                    .homeTeamId(1L)
                    .awayTeamId(2L)
                    .stadiumId(StadiumInfo.JAMSIL.id)
                    .matchTime(LocalDateTime.now().plusDays(1))
                    .build();

            MatePost filteredPost = MatePost.builder()
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("테스트 제목1")
                    .content("테스트 내용1")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            Page<MatePost> testPage = new PageImpl<>(List.of(filteredPost));
            Pageable pageable = PageRequest.of(0, 10);

            MatePostSearchRequest request = MatePostSearchRequest.builder()
                    .teamId(1L)
                    .sortType(SortType.LATEST)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .maxParticipants(4)
                    .transportType(TransportType.PUBLIC)
                    .build();

            given(mateRepository.findMatePostsByFilter(request, pageable))
                    .willReturn(testPage);

            // when
            PageResponse<MatePostSummaryResponse> result = mateService.getMatePagePosts(request, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getPageNumber()).isZero();
            assertThat(result.getContent().get(0).getTransportType()).isEqualTo(TransportType.PUBLIC);

            verify(mateRepository).findMatePostsByFilter(request, pageable);
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 - 결과 없음")
        void getMatePagePosts_EmptyResult() {
            // given
            Page<MatePost> emptyPage = new PageImpl<>(Collections.emptyList());
            Pageable pageable = PageRequest.of(0, 10);
            MatePostSearchRequest request = MatePostSearchRequest.builder().build();

            given(mateRepository.findMatePostsByFilter(request, pageable))
                    .willReturn(emptyPage);

            // when
            PageResponse<MatePostSummaryResponse> result = mateService.getMatePagePosts(request, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();

            verify(mateRepository).findMatePostsByFilter(request, pageable);
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 실패 - 존재하지 않는 팀")
        void getMatePagePosts_FailWithInvalidTeamId() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            MatePostSearchRequest request = MatePostSearchRequest.builder()
                    .teamId(999L)
                    .build();

            // when & then
            assertThatThrownBy(() -> mateService.getMatePagePosts(request, pageable))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", TEAM_NOT_FOUND);

            verify(mateRepository, never()).findMatePostsByFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("메이트 게시글 상세 조회")
    class GetMatePostDetail {
        private static final Long POST_ID = 1L;

        @Test
        @DisplayName("메이트 게시글 상세 조회 성공")
        void getMatePostDetail_Success() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            MatePost testPost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .imageUrl("post-image.jpg")
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(testPost));

            // when
            MatePostDetailResponse response = mateService.getMatePostDetail(POST_ID);

            // then
            assertThat(response.getPostImageUrl()).isEqualTo(FileUtils.getImageUrl("post-image.jpg"));
            assertThat(response.getTitle()).isEqualTo("테스트 제목");
            assertThat(response.getStatus()).isEqualTo(Status.OPEN);
            assertThat(response.getRivalTeamName()).isEqualTo("LG");  // KIA가 홈팀이므로 LG가 상대팀
            assertThat(response.getLocation()).isEqualTo("광주-기아 챔피언스 필드");
            assertThat(response.getAge()).isEqualTo(Age.TWENTIES);
            assertThat(response.getGender()).isEqualTo(Gender.ANY);
            assertThat(response.getTransportType()).isEqualTo(TransportType.PUBLIC);
            assertThat(response.getMaxParticipants()).isEqualTo(4);
            assertThat(response.getUserImageUrl()).isEqualTo(FileUtils.getThumbnailImageUrl("test.jpg"));
            assertThat(response.getNickname()).isEqualTo("테스트계정");
            assertThat(response.getManner()).isEqualTo(0.3f);
            assertThat(response.getContent()).isEqualTo("테스트 내용");
            assertThat(response.getPostId()).isEqualTo(POST_ID);
            assertThat(response.getAuthorId()).isEqualTo(testMember.getId());
            assertThat(response.getMatchId()).isEqualTo(testMatch.getId());

            verify(mateRepository).findById(POST_ID);
        }

        @Test
        @DisplayName("메이트 게시글 상세 조회 - 원정팀 팬의 게시글인 경우")
        void getMatePostDetail_SuccessWithAwayTeamFan() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            MatePost testPost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(2L)  // LG 팬의 게시글
                    .match(testMatch)
                    .imageUrl("post-image.jpg")
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(testPost));

            // when
            MatePostDetailResponse response = mateService.getMatePostDetail(POST_ID);

            // then
            assertThat(response.getRivalTeamName()).isEqualTo("KIA");  // LG 팬의 게시글이므로 KIA가 상대팀
            verify(mateRepository).findById(POST_ID);
        }

        @Test
        @DisplayName("메이트 게시글 상세 조회 실패 - 존재하지 않는 게시글")
        void getMatePostDetail_FailWithInvalidPostId() {
            // given
            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateService.getMatePostDetail(POST_ID))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_NOT_FOUND_BY_ID);

            verify(mateRepository).findById(POST_ID);
        }
    }

    @Nested
    @DisplayName("메이트 게시글 수정")
    class UpdateMatePost {

        @Test
        @DisplayName("메이트 게시글 수정 성공")
        void updateMatePost_Success() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            MatePost originalPost = MatePost.builder()
                    .id(1L)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("원본 제목")
                    .content("원본 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.FEMALE)
                    .transport(TransportType.PUBLIC)
                    .build();

            MatePostUpdateRequest request = MatePostUpdateRequest.builder()
                    .teamId(2L)
                    .matchId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .age(Age.THIRTIES)
                    .maxParticipants(6)
                    .gender(Gender.MALE)
                    .transportType(TransportType.CAR)
                    .build();

            given(mateRepository.findById(1L))
                    .willReturn(Optional.of(originalPost));
            given(matchRepository.findById(request.getMatchId()))
                    .willReturn(Optional.of(testMatch));

            // when
            MatePostResponse response = mateService.updateMatePost(TEST_MEMBER_ID, 1L, request, null);

            // then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getStatus()).isEqualTo(Status.OPEN);
            assertThat(originalPost.getTitle()).isEqualTo("수정된 제목");
            assertThat(originalPost.getContent()).isEqualTo("수정된 내용");
            assertThat(originalPost.getTeamId()).isEqualTo(2L);
            assertThat(originalPost.getAge()).isEqualTo(Age.THIRTIES);
            assertThat(originalPost.getMaxParticipants()).isEqualTo(6);
            assertThat(originalPost.getGender()).isEqualTo(Gender.MALE);
            assertThat(originalPost.getTransport()).isEqualTo(TransportType.CAR);

            verify(mateRepository).findById(1L);
            verify(matchRepository).findById(1L);
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 존재하지 않는 게시글")
        void updateMatePost_FailWithInvalidPost() {
            // given
            MatePostUpdateRequest request = MatePostUpdateRequest.builder()
                    .teamId(2L)
                    .matchId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .age(Age.THIRTIES)
                    .maxParticipants(6)
                    .gender(Gender.MALE)
                    .transportType(TransportType.CAR)
                    .build();

            given(mateRepository.findById(1L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePost(TEST_MEMBER_ID, 1L, request, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_NOT_FOUND_BY_ID);

            verify(mateRepository).findById(1L);
            verify(matchRepository, never()).findById(any());
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 권한 없음")
        void updateMatePost_FailWithUnauthorized() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            MatePost originalPost = MatePost.builder()
                    .id(1L)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("원본 제목")
                    .content("원본 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.FEMALE)
                    .transport(TransportType.PUBLIC)
                    .build();

            MatePostUpdateRequest request = MatePostUpdateRequest.builder()
                    .teamId(2L)
                    .matchId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .age(Age.THIRTIES)
                    .maxParticipants(6)
                    .gender(Gender.MALE)
                    .transportType(TransportType.CAR)
                    .build();

            given(mateRepository.findById(1L))
                    .willReturn(Optional.of(originalPost));

            Long unauthorizedMemberId = 999L;

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePost(unauthorizedMemberId, 1L, request, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_UPDATE_NOT_ALLOWED);

            verify(mateRepository).findById(1L);
            verify(matchRepository, never()).findById(any());
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 이미 완료된 게시글")
        void updateMatePost_FailWithCompletedPost() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            MatePost completedPost = MatePost.builder()
                    .id(1L)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("원본 제목")
                    .content("원본 내용")
                    .status(Status.VISIT_COMPLETE)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.FEMALE)
                    .transport(TransportType.PUBLIC)
                    .build();

            MatePostUpdateRequest request = MatePostUpdateRequest.builder()
                    .teamId(2L)
                    .matchId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .age(Age.THIRTIES)
                    .maxParticipants(6)
                    .gender(Gender.MALE)
                    .transportType(TransportType.CAR)
                    .build();

            given(mateRepository.findById(1L))
                    .willReturn(Optional.of(completedPost));

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePost(TEST_MEMBER_ID, 1L, request, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ALREADY_COMPLETED_POST);

            verify(mateRepository).findById(1L);
            verify(matchRepository, never()).findById(any());
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 존재하지 않는 팀")
        void updateMatePost_FailWithInvalidTeam() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            MatePost originalPost = MatePost.builder()
                    .id(1L)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("원본 제목")
                    .content("원본 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.FEMALE)
                    .transport(TransportType.PUBLIC)
                    .build();

            Long invalidTeamId = 999L;
            MatePostUpdateRequest request = MatePostUpdateRequest.builder()
                    .teamId(invalidTeamId)
                    .matchId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .age(Age.THIRTIES)
                    .maxParticipants(6)
                    .gender(Gender.MALE)
                    .transportType(TransportType.CAR)
                    .build();

            given(mateRepository.findById(1L))
                    .willReturn(Optional.of(originalPost));

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePost(TEST_MEMBER_ID, 1L, request, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", TEAM_NOT_FOUND);

            verify(mateRepository).findById(1L);
            verify(matchRepository, never()).findById(any());
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 존재하지 않는 매치")
        void updateMatePost_FailWithInvalidMatch() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();

            MatePost originalPost = MatePost.builder()
                    .id(1L)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .title("원본 제목")
                    .content("원본 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.FEMALE)
                    .transport(TransportType.PUBLIC)
                    .build();

            MatePostUpdateRequest request = MatePostUpdateRequest.builder()
                    .teamId(2L)
                    .matchId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
                    .age(Age.THIRTIES)
                    .maxParticipants(6)
                    .gender(Gender.MALE)
                    .transportType(TransportType.CAR)
                    .build();

            given(mateRepository.findById(1L))
                    .willReturn(Optional.of(originalPost));
            given(matchRepository.findById(request.getMatchId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mateService.updateMatePost(TEST_MEMBER_ID, 1L, request, null))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATCH_NOT_FOUND_BY_ID);

            verify(mateRepository).findById(1L);
            verify(matchRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("메이트 게시글 삭제")
    class DeleteMatePost {
        private static final Long POST_ID = 1L;

        @Test
        @DisplayName("메이트 게시글 삭제 성공")
        void deleteMatePost_Success() {
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
                    .imageUrl("image.png")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .build();

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when
            mateService.deleteMatePost(TEST_MEMBER_ID, POST_ID);

            // then
            verify(mateRepository).findById(POST_ID);
            verify(mateRepository).delete(matePost);
        }

        @Test
        @DisplayName("직관 완료된 메이트 게시글 삭제 성공")
        void deleteMatePost_SuccessWithCompletedPost() {
            // given
            Member testMember = createTestMember();
            Match testMatch = createTestMatch();
            Visit visit = Visit.builder().build();

            MatePost matePost = MatePost.builder()
                    .id(POST_ID)
                    .author(testMember)
                    .teamId(1L)
                    .match(testMatch)
                    .imageUrl("image.png")
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .status(Status.OPEN)
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.ANY)
                    .transport(TransportType.PUBLIC)
                    .visit(visit)
                    .build();

            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when
            mateService.deleteMatePost(TEST_MEMBER_ID, POST_ID);

            // then
            verify(mateRepository).findById(POST_ID);
            verify(mateRepository).delete(matePost);
            assertThat(visit.getPost()).isNull();
        }

        @Test
        @DisplayName("메이트 게시글 삭제 실패 - 작성자가 아닌 경우")
        void deleteMatePost_FailWithNotAuthor() {
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
            given(mateRepository.findById(POST_ID))
                    .willReturn(Optional.of(matePost));

            // when & then
            assertThatThrownBy(() -> mateService.deleteMatePost(differentMemberId, POST_ID))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", MATE_POST_UPDATE_NOT_ALLOWED);

            verify(mateRepository).findById(POST_ID);
            verify(mateRepository, never()).delete(any(MatePost.class));
        }
    }
}