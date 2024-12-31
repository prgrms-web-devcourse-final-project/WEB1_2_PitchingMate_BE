package com.example.mate.domain.member.integration;

import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.goods.dto.response.LocationInfo;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsReview;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goods.repository.GoodsReviewRepository;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.entity.*;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.mate.repository.VisitPartRepository;
import com.example.mate.domain.mate.repository.VisitRepository;
import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.dto.request.MemberInfoUpdateRequest;
import com.example.mate.domain.member.dto.request.MemberLoginRequest;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import com.example.mate.domain.member.service.LogoutRedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.mate.domain.match.entity.MatchStatus.SCHEDULED;
import static com.example.mate.domain.mate.entity.Status.CLOSED;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class MemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private GoodsPostRepository goodsPostRepository;

    @Autowired
    private GoodsReviewRepository goodsReviewRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MateRepository mateRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private MateReviewRepository mateReviewRepository;

    @Autowired
    private VisitPartRepository visitPartRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LogoutRedisService logoutRedisService;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    private Member member;
    private Member member2;
    private GoodsPost goodsPost;
    private GoodsReview goodsReview;
    private Match match;
    private MatePost matePost;
    private Visit visit;
    private MateReview mateReview;

    @BeforeEach
    void setUp() {

        // H2 데이터베이스의 ID 초기화 문법
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        // 테스트용 회원 및 관련 데이터 생성
        member = createMember("홍길동", "tester", "tester@example.com", Gender.MALE, 20);
        member2 = createMember("김철수", "tester2", "test2@example.com", Gender.MALE, 22);

        // 팔로우 관계 설정
        createFollow(member, member2);
        createFollow(member2, member);

        // 상품 리뷰 생성
        goodsPost = createGoodsPost(member, member2);
        goodsReview = createGoodsReview(member2, member);

        // 메이트 리뷰 생성
        match = createMatch(LocalDateTime.now().minusDays(2));
        matePost = createMatePost(member, match);
        visit = createVisit(matePost, List.of(member, member2));
        mateReview = createMateReview(member2, member, matePost);
    }

    private Member createMember(String name, String nickname, String email, Gender gender, int age) {
        return memberRepository.save(Member.builder()
                .name(name)
                .nickname(nickname)
                .email(email)
                .imageUrl("default.jpg")
                .age(age)
                .gender(gender)
                .teamId(1L)
                .manner(0.300F)
                .aboutMe("테스트 회원입니다.")
                .build());
    }

    private void createFollow(Member follower, Member following) {
        followRepository.save(Follow.builder().follower(follower).following(following).build());
    }

    private GoodsPost createGoodsPost(Member seller, Member buyer) {
        return goodsPostRepository.save(GoodsPost.builder()
                .seller(seller)
                .buyer(buyer)
                .teamId(1L)
                .title("상품 제목")
                .content("상품 내용")
                .price(10_000)
                .category(Category.ACCESSORY)
                .location(LocationInfo.toEntity(createLocationInfo()))
                .status(Status.CLOSED)
                .build());
    }

    private GoodsReview createGoodsReview(Member reviewer, Member reviewee) {
        return goodsReviewRepository.save(GoodsReview.builder()
                .goodsPost(goodsPost)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(Rating.GOOD)
                .reviewContent("좋은 상품입니다. 만족합니다.")
                .build());
    }

    private LocationInfo createLocationInfo() {
        return LocationInfo.builder()
                .placeName("스포츠 스타디움")
                .longitude("127.12345")
                .latitude("37.56789")
                .build();
    }

    private JoinRequest createJoinRequest(String name, String nickname, String email, String gender, String birthyear,
                                          Long teamId) {
        return JoinRequest.builder()
                .name(name)
                .email(email)
                .gender(gender)
                .birthyear(birthyear)
                .teamId(teamId)
                .nickname(nickname)
                .build();
    }

    private Match createMatch(LocalDateTime matchTime) {
        return matchRepository.save(Match.builder()
                .homeTeamId(1L)
                .awayTeamId(2L)
                .stadiumId(1L)
                .status(SCHEDULED)
                .matchTime(matchTime)
                .build());
    }

    private MatePost createMatePost(Member author, Match match) {
        return mateRepository.save(MatePost.builder()
                .author(author)
                .teamId(1L)
                .match(match)
                .title("매칭 제목")
                .content("매칭 내용")
                .status(CLOSED)
                .maxParticipants(5)
                .currentParticipants(1)
                .age(Age.ALL)
                .gender(Gender.MALE)
                .transport(TransportType.CAR)
                .imageUrl("mate_image.jpg")
                .build());
    }

    private Visit createVisit(MatePost matePost, List<Member> participants) {
        Visit visit = visitRepository.save(Visit.builder().post(matePost).build());

        participants.forEach(member -> {
            VisitPart visitPart = VisitPart.builder()
                    .member(member)
                    .visit(visit)
                    .build();
            visitPartRepository.save(visitPart);

            // 1차 캐시에서 분리
            entityManager.detach(visitPart);
        });
        return visit;
    }

    private MateReview createMateReview(Member reviewer, Member reviewee, MatePost matePost) {
        return mateReviewRepository.save(MateReview.builder()
                .visit(visit)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .reviewContent("매칭 후기입니다. 아주 즐거웠어요!")
                .rating(Rating.GOOD)
                .build());
    }

    private MemberInfoUpdateRequest createMemberInfoUpdateRequest() {
        return MemberInfoUpdateRequest.builder()
                .teamId(2L)
                .nickname("newTester")
                .aboutMe("새로운 테스터입니다.")
                .memberId(member.getId())
                .build();
    }

    @Nested
    @DisplayName("자체 회원 가입")
    class Join {

        @Test
        @DisplayName("자체 회원 가입 성공")
        void join_success() throws Exception {
            JoinRequest joinRequest = createJoinRequest("이철수", "tester3", "tester3@example.com", "M", "2002", 1L);

            mockMvc.perform(post("/api/members/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(joinRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.name").value("이철수"))
                    .andExpect(jsonPath("$.data.email").value("tester3@example.com"))
                    .andExpect(jsonPath("$.data.age").value(22))
                    .andExpect(jsonPath("$.data.nickname").value("tester3"))
                    .andDo(print());
        }

        @Test
        @DisplayName("자체 회원 가입 실패 - teamId가 유효하지 않으면 오류")
        void join_fail_invalid_teamId() throws Exception {
            JoinRequest invalidJoinRequest = createJoinRequest("김철수", "tester2", "tester2@example.com", "M", "2002",
                    15L);

            mockMvc.perform(post("/api/members/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidJoinRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("teamId: teamId는 10 이하이어야 합니다."))
                    .andExpect(jsonPath("$.code").value(400))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("내 프로필 조회")
    class GetMyProfile {

        @Test
        @DisplayName("내 프로필 조회 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void find_my_info_success() throws Exception {
            System.out.println(member.getId());
            mockMvc.perform(get("/api/members/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.nickname").value("tester"))
                    .andExpect(jsonPath("$.data.imageUrl").value("default.jpg"))
                    .andExpect(jsonPath("$.data.manner").value(0.300F))
                    .andExpect(jsonPath("$.data.aboutMe").value("테스트 회원입니다."))
                    .andExpect(jsonPath("$.data.followingCount").value(1))
                    .andExpect(jsonPath("$.data.followerCount").value(1))
                    .andExpect(jsonPath("$.data.reviewsCount").value(2))
                    .andExpect(jsonPath("$.data.goodsSoldCount").value(1))
                    .andExpect(jsonPath("$.data.goodsBoughtCount").value(0))
                    .andExpect(jsonPath("$.data.visitsCount").value(1))
                    .andDo(print());
        }

        @Test
        @DisplayName("내 프로필 조회 실패 - 존재하지 않는 회원 ID")
        @WithAuthMember(userId = "customUser", memberId = 999L)
        void find_member_info_fail_not_found() throws Exception {
            // 존재하지 않는 memberId를 사용
            mockMvc.perform(get("/api/members/me"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andDo(print());
        }
    }

    @Test
    @DisplayName("다른 회원 프로필 조회 성공")
    void find_member_info_success() throws Exception {
        mockMvc.perform(get("/api/members/" + member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.imageUrl").value("default.jpg"))
                .andExpect(jsonPath("$.data.manner").value(0.300F))
                .andDo(print());
    }

    @Nested
    @DisplayName("회원 정보 수정")
    class UpdateMember {

        @Test
        @DisplayName("회원 정보 수정 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void update_my_profile_success() throws Exception {
            MemberInfoUpdateRequest request = createMemberInfoUpdateRequest();
            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            mockMvc.perform(multipart(HttpMethod.PUT, "/api/members/me")
                            .file(data)
                            .file(image))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.nickname").value("newTester"))
                    .andExpect(jsonPath("$.data.teamName").value("LG"))
                    .andExpect(jsonPath("$.data.aboutMe").value("새로운 테스터입니다."))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 정보 수정 실패 - 필수 파라미터 누락")
        void update_my_profile_missing_parameter() throws Exception {
            // given - nickname 누락
            MemberInfoUpdateRequest request = MemberInfoUpdateRequest.builder()
                    .teamId(1L)
                    .aboutMe("테스터입니다.")
                    .memberId(1L)
                    .build();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart(HttpMethod.PUT, "/api/members/me")
                            .file(data)
                            .file(image))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("nickname: 닉네임은 필수 항목입니다."))
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class DeleteMember {

        @Test
        @DisplayName("회원 탈퇴 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void delete_member_success() throws Exception {
            mockMvc.perform(delete("/api/members/me"))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("회원 로그인")
    class LoginMember {

        @Test
        @DisplayName("회원 로그인 성공")
        void login_member_success() throws Exception {
            // given
            MemberLoginRequest request = MemberLoginRequest.builder()
                    .email("tester@example.com")
                    .build();

            // when & then
            mockMvc.perform(post("/api/members/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.memberId").value("1"))
                    .andExpect(jsonPath("$.data.nickname").value("tester"))
                    .andExpect(jsonPath("$.data.teamId").value("1"))
                    .andExpect(jsonPath("$.data.gender").value("남자"))
                    .andExpect(jsonPath("$.data.age").value("20"))
                    .andDo(print());
        }

        @Test
        @DisplayName("회원 로그인 실패 - 존재하지 않는 이메일")
        void login_member_fail_non_exists_email() throws Exception {
            // given
            MemberLoginRequest request = MemberLoginRequest.builder()
                    .email("test10000@example.com")
                    .build();

            // when & then
            mockMvc.perform(post("/api/members/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("해당 이메일의 회원 정보를 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("회원 로그아웃")
    class LogoutMember {

        @Test
        @DisplayName("회원 로그아웃 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void logout_member_success_with_my_info_denied() throws Exception {
            // given
            String token = "Bearer accessToken";

            // when & then
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            doNothing().when(valueOperations).set(
                    eq("blacklist:" + token.substring(7)),
                    eq("blacklisted"),
                    eq(1L),
                    eq(TimeUnit.MINUTES));

            mockMvc.perform(post("/api/members/logout")
                            .header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("회원 로그아웃 실패 - 잘못된 토큰 형식")
        void catchMiLogout_invalid_token_format() throws Exception {
            // given
            String invalidToken = "InvalidToken";

            // when & then
            mockMvc.perform(post("/api/members/logout")
                            .header(HttpHeaders.AUTHORIZATION, invalidToken))
                    .andExpect(status().isBadRequest());
        }
    }
}