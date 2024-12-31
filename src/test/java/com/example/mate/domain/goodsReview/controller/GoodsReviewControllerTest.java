package com.example.mate.domain.goodsReview.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.goodsReview.dto.request.GoodsReviewRequest;
import com.example.mate.domain.goodsReview.dto.response.GoodsReviewResponse;
import com.example.mate.domain.goodsReview.service.GoodsReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GoodsReviewController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@WithAuthMember
class GoodsReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoodsReviewService goodsReviewService;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    @Test
    @DisplayName("굿즈 거래 후기 등록 - API 테스트")
    void register_goods_review_success() throws Exception {
        // given
        Long reviewerId = 1L;
        Long goodsPostId = 1L;

        GoodsReviewRequest request = new GoodsReviewRequest(Rating.GREAT, "Great seller!");
        GoodsReviewResponse response = GoodsReviewResponse.builder()
                .reviewId(1L)
                .reviewerNickname("Reviewer")
                .rating(Rating.GREAT)
                .reviewContent("Great seller!")
                .goodsPostId(goodsPostId)
                .goodsPostTitle("Sample Goods")
                .build();

        given(goodsReviewService.registerGoodsReview(eq(reviewerId), eq(goodsPostId),
                any(GoodsReviewRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/goods/review/{goodsPostId}", goodsPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.reviewId").value(response.getReviewId()))
                .andExpect(jsonPath("$.data.reviewerNickname").value(response.getReviewerNickname()))
                .andExpect(jsonPath("$.data.rating").value(response.getRating().getValue()))
                .andExpect(jsonPath("$.data.reviewContent").value(response.getReviewContent()))
                .andExpect(jsonPath("$.data.goodsPostTitle").value(response.getGoodsPostTitle()))
                .andExpect(jsonPath("$.code").value(200));

        verify(goodsReviewService).registerGoodsReview(eq(reviewerId), eq(goodsPostId), any(GoodsReviewRequest.class));
    }
}