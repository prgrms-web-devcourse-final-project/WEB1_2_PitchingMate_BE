package com.example.mate.domain.member.integration;

import static com.example.mate.domain.match.entity.MatchStatus.SCHEDULED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.goodsPost.dto.response.LocationInfo;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.GoodsPostImage;
import com.example.mate.domain.goodsReview.entity.GoodsReview;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.repository.GoodsPostImageRepository;
import com.example.mate.domain.goodsPost.repository.GoodsPostRepository;
import com.example.mate.domain.goodsReview.repository.GoodsReviewRepository;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.matePost.entity.Age;
import com.example.mate.domain.matePost.entity.MatePost;
import com.example.mate.domain.mateReview.entity.MateReview;
import com.example.mate.domain.matePost.entity.TransportType;
import com.example.mate.domain.matePost.entity.Visit;
import com.example.mate.domain.matePost.entity.VisitPart;
import com.example.mate.domain.matePost.repository.MatePostRepository;
import com.example.mate.domain.mateReview.repository.MateReviewRepository;
import com.example.mate.domain.matePost.repository.VisitPartRepository;
import com.example.mate.domain.matePost.repository.VisitRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class ProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GoodsPostRepository goodsPostRepository;

    @Autowired
    private GoodsPostImageRepository imageRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatePostRepository matePostRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private VisitPartRepository visitPartRepository;

    @Autowired
    private MateReviewRepository mateReviewRepository;

    @Autowired
    private GoodsReviewRepository goodsReviewRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Autowired
    private ObjectMapper objectMapper;

    private Member member1;
    private Member member2;
    private Member member3;

    private GoodsPost goodsPost1;
    private GoodsPost goodsPost2;

    private Match match;

    private MatePost matePost;

    private Visit visit;

    @BeforeEach
    void setUp() {
        // H2 데이터베이스의 ID 초기화 문법
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        createMember();
        goodsPost1 = createGoodsPost();
        createGoodsPostImage(goodsPost1);
        goodsPost2 = createGoodsPost();
        createGoodsPostImage(goodsPost2);
        createMatch(LocalDateTime.now().minusDays(7));
        createMatePost();
        createVisit(matePost, List.of(member1, member2, member3));
        createVisitPart();
        createMateReview(member2, member1);
        createMateReview(member3, member1);
        createGoodsReview(member2, member1, goodsPost1);
        createGoodsReview(member3, member1, goodsPost2);
    }

    private void createMember() {
        member1 = memberRepository.save(Member.builder()
                .name("홍길동")
                .email("test@gmail.com")
                .nickname("테스터")
                .imageUrl("upload/test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
        member2 = memberRepository.save(Member.builder()
                .name("김철수")
                .email("test2@gmail.com")
                .nickname("테스터2")
                .imageUrl("upload/test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
        member3 = memberRepository.save(Member.builder()
                .name("김영희")
                .email("test3@gmail.com")
                .nickname("테스터3")
                .imageUrl("upload/test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
    }

    private GoodsPost createGoodsPost() {
        return goodsPostRepository.save(GoodsPost.builder()
                .seller(member1)
                .teamId(1L)
                .title("test title")
                .content("test content")
                .price(10_000)
                .category(Category.ACCESSORY)
                .location(LocationInfo.toEntity(createLocationInfo()))
                .status(Status.CLOSED)
                .build());
    }

    private void createGoodsPostImage(GoodsPost goodsPost) {
        GoodsPostImage image = GoodsPostImage.builder()
                .imageUrl("upload/test_img_url")
                .build();

        goodsPost.changeImages(List.of(image));
        goodsPostRepository.save(goodsPost);
    }

    private LocationInfo createLocationInfo() {
        return LocationInfo.builder()
                .placeName("Stadium Plaza")
                .longitude("127.12345")
                .latitude("37.56789")
                .build();
    }

    private void createMatch(LocalDateTime matchTime) {
        match = matchRepository.save(Match.builder()
                .homeTeamId(1L)
                .awayTeamId(2L)
                .stadiumId(1L)
                .status(SCHEDULED)
                .matchTime(matchTime)
                .build());
    }

    private void createMatePost() {
        matePost = matePostRepository.save(MatePost.builder()
                .author(member1)
                .teamId(1L)
                .match(match)
                .title("테스트 제목")
                .content("테스트 내용")
                .status(com.example.mate.domain.matePost.entity.Status.CLOSED)
                .maxParticipants(3)
                .age(Age.TWENTIES)
                .gender(Gender.FEMALE)
                .transport(TransportType.PUBLIC)
                .build());
    }

    private void createVisit(MatePost post, List<Member> participants) {
        visit = visitRepository.save(Visit.createForComplete(post, participants));
    }

    private void createVisitPart() {
        visitPartRepository.save(VisitPart.builder()
                .member(member1)
                .visit(visit)
                .build());
        visitPartRepository.save(VisitPart.builder()
                .member(member2)
                .visit(visit)
                .build());
        visitPartRepository.save(VisitPart.builder()
                .member(member3)
                .visit(visit)
                .build());
    }

    private void createMateReview(Member reviewer, Member reviewee) {
        mateReviewRepository.save(MateReview.builder()
                .visit(visit)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .reviewContent("good")
                .rating(Rating.GOOD)
                .build());
    }

    private void createGoodsReview(Member reviewer, Member reviewee, GoodsPost goodsPost) {
        goodsReviewRepository.save(GoodsReview.builder()
                .goodsPost(goodsPost)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .rating(Rating.GOOD)
                .reviewContent("good")
                .build());
    }

    @Nested
    @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회")
    class ProfileSoldGoodsPage {

        @Test
        @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회 성공")
        void get_sold_goods_page_success() throws Exception {
            // given
            Long memberId = member1.getId();
            int page = 0;
            int size = 10;

            GoodsPost post = GoodsPost.builder()
                    .seller(member1)
                    .teamId(10L)
                    .title("Test Title ")
                    .content("Test Content ")
                    .price(1000)
                    .category(Category.ACCESSORY)
                    .location(LocationInfo.toEntity(createLocationInfo()))
                    .status(Status.CLOSED)
                    .build();

            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl("upload/test_img_url ")
                    .post(post)
                    .build();

            post.changeImages(List.of(image));
            goodsPostRepository.save(post);

            // when
            mockMvc.perform(get("/api/profile/{memberId}/goods/sold", memberId)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_sold_goods_page_invalid_member_id() throws Exception {
            // given
            Long invalidMemberId = member1.getId() + 999L; // 존재하지 않는 회원 ID
            int page = 0;
            int size = 10;

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/goods/sold", invalidMemberId)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다")); // 커스텀 에러 메시지
        }
    }

    @Nested
    @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회")
    class ProfileBoughtGoodsPage {

        @Test
        @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회 성공")
        @WithAuthMember(userId = "customUser", memberId = 4L)
        void get_bought_goods_page_success() throws Exception {
            // given
            Member buyer = memberRepository.save(Member.builder()
                    .name("김희민")
                    .email("test4@gmail.com")
                    .nickname("테스터4")
                    .imageUrl("upload/test.jpg")
                    .gender(Gender.MALE)
                    .age(25)
                    .manner(0.3f)
                    .build());
            int page = 0;
            int size = 10;

            GoodsPost post = GoodsPost.builder()
                    .seller(member1)
                    .buyer(buyer)
                    .teamId(10L)
                    .title("Test Title ")
                    .content("Test Content ")
                    .price(1000)
                    .category(Category.ACCESSORY)
                    .location(LocationInfo.toEntity(createLocationInfo()))
                    .status(Status.CLOSED)
                    .build();

            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl("upload/test_img_url ")
                    .post(post)
                    .build();

            post.changeImages(List.of(image));
            goodsPostRepository.save(post);

            // when
            mockMvc.perform(get("/api/profile/{memberId}/goods/bought", buyer.getId())
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void get_bought_goods_page_invalid_member_id() throws Exception {
            // given
            Long invalidMemberId = member1.getId() + 999L; // 존재하지 않는 회원 ID
            int page = 0;
            int size = 10;

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/goods/bought", invalidMemberId)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(403))
                    .andExpect(jsonPath("$.message").value("해당 회원의 접근 권한이 없습니다.")); // 커스텀 에러 메시지
        }
    }

    @Nested
    @DisplayName("회원 프로필 메이트 후기 페이징 조회")
    class ProfileMateReviewPage {

        @Test
        @DisplayName("회원 프로필 메이트 후기 페이징 조회 성공")
        void get_mate_review_page_success() throws Exception {
            // given
            Long memberId = member1.getId();
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/review/mate", memberId)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 프로필 메이트 후기 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_mate_review_page_fail_invalid_member_id() throws Exception {
            // given
            Long invalidMemberId = member1.getId() + 999L; // 존재 하지 않는 아이디
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/review/mate", invalidMemberId)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageNumber())))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다"));
        }
    }

    @Nested
    @DisplayName("회원 프로필 굿즈 후기 페이징 조회")
    class ProfileGoodsReviewPage {

        @Test
        @DisplayName("회원 프로필 굿즈 후기 페이징 조회 성공")
        void get_goods_review_page_success() throws Exception {
            // given
            Long memberId = member1.getId();
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/review/goods", memberId)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 프로필 굿즈 후기 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_goods_review_page_fail_invalid_member_id() throws Exception {
            // given
            Long invalidMemberId = member1.getId() + 999L; // 존재 하지 않는 아이디
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/review/goods", invalidMemberId)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageNumber())))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다"));
        }
    }

    @Nested
    @DisplayName("회원 타임라인 페이징 조회")
    class ProfileTimelinePage {

        @Test
        @DisplayName("회원 타임라인 페이징 조회 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void get_my_visit_page_success() throws Exception {
            // given
            Long memberId = member1.getId();
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            mockMvc.perform(get("/api/profile/timeline")
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("작성한 굿즈 거래글 페이징 조회")
    class ProfileGoodsPostsPage {

        @Test
        @DisplayName("작성한 굿즈 거래글 페이징 조회 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void get_my_goods_posts_page_success() throws Exception {
            // given
            int page = 0;
            int size = 10;

            GoodsPost post = GoodsPost.builder()
                    .seller(member1)
                    .teamId(10L)
                    .title("Test Title ")
                    .content("Test Content ")
                    .price(1000)
                    .category(Category.ACCESSORY)
                    .location(LocationInfo.toEntity(createLocationInfo()))
                    .status(Status.CLOSED)
                    .build();

            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl("upload/test_img_url ")
                    .post(post)
                    .build();

            post.changeImages(List.of(image));
            goodsPostRepository.save(post);

            // when & then
            mockMvc.perform(get("/api/profile/posts/goods")
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("작성한 메이트 구인글 페이징 조회")
    class ProfileMatePostsPage {

        @Test
        @DisplayName("작성한 메이트 구인글 페이징 조회 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void get_my_mate_posts_page_success() throws Exception {
            // given
            int page = 0;
            int size = 10;

            MatePost post = MatePost.builder()
                    .author(member1)
                    .teamId(1L)
                    .match(matchRepository.save(Match.builder()
                            .homeTeamId(1L)
                            .awayTeamId(2L)
                            .stadiumId(1L)
                            .status(SCHEDULED)
                            .matchTime(LocalDateTime.now().plusDays(7))
                            .build()))
                    .title("new title")
                    .content("new content")
                    .status(com.example.mate.domain.matePost.entity.Status.OPEN)
                    .maxParticipants(10)
                    .age(Age.ALL)
                    .gender(Gender.ANY)
                    .transport(TransportType.ANY)
                    .build();

            matePostRepository.save(post);

            // when & then
            mockMvc.perform(get("/api/profile/posts/mate")
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}