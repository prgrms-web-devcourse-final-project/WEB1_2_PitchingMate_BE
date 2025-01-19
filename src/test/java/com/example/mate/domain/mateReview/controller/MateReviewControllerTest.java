package com.example.mate.domain.mateReview.controller;

import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.config.securityConfig.WithAuthMember;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.mateReview.dto.request.MateReviewCreateRequest;
import com.example.mate.domain.mateReview.dto.response.MateReviewCreateResponse;
import com.example.mate.domain.mateReview.service.MateReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MateReviewController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@WithAuthMember
class MateReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MateReviewService mateReviewService;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    @Nested
    @DisplayName("메이트 직관 후기 작성")
    class CreateMateReview {

        private MateReviewCreateRequest createMateReviewRequest() {
            return MateReviewCreateRequest.builder()
                    .revieweeId(2L)
                    .rating(Rating.GOOD)
                    .content("함께해서 즐거웠어요!")
                    .build();
        }

        private MateReviewCreateResponse createMateReviewResponse() {
            return MateReviewCreateResponse.builder()
                    .reviewId(1L)
                    .reviewerId(1L)
                    .revieweeId(2L)
                    .revieweeNickName("테스트닉네임")
                    .content("함께해서 즐거웠어요!")
                    .rating("좋아요!")
                    .build();
        }

        @Test
        @DisplayName("메이트 직관 후기 작성 성공")
        void createMateReview_success() throws Exception {
            // given
            Long postId = 1L;
            MateReviewCreateRequest request = createMateReviewRequest();
            MateReviewCreateResponse response = createMateReviewResponse();

            given(mateReviewService.createReview(any(), any(), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/mates/review/{postId}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.reviewId").value(1L))
                    .andExpect(jsonPath("$.data.reviewerId").value(1L))
                    .andExpect(jsonPath("$.data.revieweeId").value(2L))
                    .andExpect(jsonPath("$.data.revieweeNickName").value("테스트닉네임"))
                    .andExpect(jsonPath("$.data.content").value("함께해서 즐거웠어요!"))
                    .andExpect(jsonPath("$.data.rating").value("좋아요!"))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("메이트 직관 후기 작성 실패 - 리뷰 대상자 ID 누락")
        void createMateReview_failWithoutRevieweeId() throws Exception {
            // given
            Long postId = 1L;
            MateReviewCreateRequest request = MateReviewCreateRequest.builder()
                    .rating(Rating.GOOD)
                    .content("함께해서 즐거웠어요!")
                    .build();

            // when & then
            mockMvc.perform(post("/api/mates/review/{postId}", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("revieweeId: 리뷰 대상자 ID는 필수입니다."))
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("메이트 직관 후기 작성 실패 - 리뷰 내용 길이 초과")
        void createMateReview_failWithLongContent() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            String longContent = "a".repeat(101);
            MateReviewCreateRequest request = MateReviewCreateRequest.builder()
                    .revieweeId(2L)
                    .rating(Rating.GOOD)
                    .content(longContent)
                    .build();

            // when & then
            mockMvc.perform(post("/api/mates/review/{postId}", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("content: 리뷰 내용은 100자를 초과할 수 없습니다."))
                    .andExpect(jsonPath("$.code").value(400));
        }
    }
}
