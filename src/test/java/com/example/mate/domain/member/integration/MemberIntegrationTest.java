package com.example.mate.domain.member.integration;

import static com.example.mate.domain.match.entity.MatchStatus.SCHEDULED;
import static com.example.mate.domain.mate.entity.Status.CLOSED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsReview;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.goods.repository.GoodsReviewRepository;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.MateReview;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.entity.Visit;
import com.example.mate.domain.mate.entity.VisitPart;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.mate.repository.MateReviewRepository;
import com.example.mate.domain.mate.repository.VisitPartRepository;
import com.example.mate.domain.mate.repository.VisitRepository;
import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
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
            visit.getParticipants().add(visitPart);
            visitPartRepository.save(visitPart);
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
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("자체 회원 가입 - 성공")
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
    @DisplayName("회원 가입 - teamId가 유효하지 않으면 오류")
    void join_fail_invalid_teamId() throws Exception {
        JoinRequest invalidJoinRequest = createJoinRequest("김철수", "tester2", "tester2@example.com", "M", "2002", 15L);

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidJoinRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("teamId: teamId는 10 이하이어야 합니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("내 프로필 조회 - 성공")
    void find_my_info_success() throws Exception {
        mockMvc.perform(get("/api/members/me")
                        .param("memberId", member.getId().toString()))
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
    @DisplayName("내 프로필 조회 - 존재하지 않는 회원 ID 실패")
    void find_member_info_fail_not_found() throws Exception {
        // 존재하지 않는 memberId를 사용
        mockMvc.perform(get("/api/members/me")
                        .param("memberId", "99999999"))  // 존재하지 않는 ID
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다"))
                .andExpect(jsonPath("$.code").value(404))
                .andDo(print());
    }

    @Test
    @DisplayName("내 프로필 조회 - 회원 ID 누락 실패")
    void find_member_info_fail_missing_memberId() throws Exception {
        // memberId 파라미터를 누락
        mockMvc.perform(get("/api/members/me"))
                .andExpect(status().isBadRequest())  // 400 상태 코드
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("요청에 'memberId' 파라미터가 누락되었습니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("다른 회원 프로필 조회 - 성공")
    void find_member_info_success() throws Exception {
        mockMvc.perform(get("/api/members/" + member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.imageUrl").value("default.jpg"))
                .andExpect(jsonPath("$.data.manner").value(0.300F))
                .andDo(print());
    }
}
