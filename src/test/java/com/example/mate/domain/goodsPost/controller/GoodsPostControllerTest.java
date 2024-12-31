package com.example.mate.domain.goodsPost.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.goodsPost.dto.request.GoodsPostRequest;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostResponse;
import com.example.mate.domain.goodsPost.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goodsPost.dto.response.LocationInfo;
import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.Status;
import com.example.mate.domain.goodsPost.service.GoodsPostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@WebMvcTest(GoodsPostController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@WithAuthMember
class GoodsPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoodsPostService goodsPostService;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    private GoodsPostRequest createGoodsPostRequest() {
        LocationInfo location = LocationInfo.builder()
                .placeName("Stadium Plaza")
                .longitude("127.12345")
                .latitude("37.56789")
                .build();

        return new GoodsPostRequest(1L, "title", Category.ACCESSORY, 10_000, "content", location);
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile(
                "files",
                "test_photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );
    }

    private GoodsPostResponse createGoodsPostResponse() {
        return GoodsPostResponse.builder()
                .id(1L)
                .status(Status.OPEN.getValue())
                .build();
    }

    private GoodsPostSummaryResponse createGoodsPostSummaryResponse() {
        return GoodsPostSummaryResponse.builder()
                .id(1L)
                .teamName("KIA")
                .title("test title")
                .category(Category.CLOTHING.getValue())
                .price(10_000)
                .imageUrl("test.jpg")
                .build();
    }

    @Test
    @DisplayName("굿즈 판매글 작성 - API 테스트")
    void register_goods_post_success() throws Exception {
        // given
        Long memberId = 1L;
        GoodsPostRequest postRequest = createGoodsPostRequest();
        GoodsPostResponse response = createGoodsPostResponse();
        List<MockMultipartFile> files = List.of(createFile(), createFile());

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(postRequest)
        );

        given(goodsPostService.registerGoodsPost(eq(memberId), any(GoodsPostRequest.class), anyList())).willReturn(
                response);

        // when
        MockMultipartHttpServletRequestBuilder multipartRequest = multipart("/api/goods").file(data);
        files.forEach(multipartRequest::file);

        // then
        mockMvc.perform(multipartRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(response.getId()))
                .andExpect(jsonPath("$.data.status").value(response.getStatus()))
                .andExpect(jsonPath("$.code").value(200));

        verify(goodsPostService).registerGoodsPost(eq(memberId), any(GoodsPostRequest.class), anyList());
    }

    @Test
    @DisplayName("굿즈 판매글 수정 - API 테스트")
    void update_goods_post_success() throws Exception {
        // given
        Long memberId = 1L;
        Long goodsPostId = 1L;
        GoodsPostRequest postRequest = createGoodsPostRequest();
        GoodsPostResponse response = createGoodsPostResponse();
        List<MockMultipartFile> files = List.of(createFile(), createFile());

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(postRequest)
        );

        given(goodsPostService.updateGoodsPost(eq(memberId), eq(goodsPostId), any(GoodsPostRequest.class), anyList()))
                .willReturn(response);

        // when
        MockMultipartHttpServletRequestBuilder multipartRequest = multipart("/api/goods/{goodsPostId}", goodsPostId).file(data);
        files.forEach(multipartRequest::file);
        multipartRequest.with(request -> {
            request.setMethod("PUT"); // PUT 메서드로 변경
            return request;
        });

        // then
        mockMvc.perform(multipartRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(response.getId()))
                .andExpect(jsonPath("$.data.status").value(response.getStatus()))
                .andExpect(jsonPath("$.code").value(200));

        verify(goodsPostService).updateGoodsPost(eq(memberId), eq(goodsPostId), any(GoodsPostRequest.class), anyList());
    }

    @Test
    @DisplayName("굿즈 판매글 삭제 - API 테스트")
    void delete_goods_post_success() throws Exception {
        // given
        Long memberId = 1L;
        Long goodsPostId = 1L;

        willDoNothing().given(goodsPostService).deleteGoodsPost(memberId, goodsPostId);

        // when & then
        mockMvc.perform(delete("/api/goods/{goodsPostId}", goodsPostId)).andDo(print()).andExpect(status().isNoContent());

        verify(goodsPostService).deleteGoodsPost(memberId, goodsPostId);
    }

    @Test
    @DisplayName("굿즈 판매글 상세 조회 - API 테스트")
    void get_goods_post_success() throws Exception {
        // given
        Long goodsPostId = 1L;
        GoodsPostResponse response = createGoodsPostResponse();

        given(goodsPostService.getGoodsPost(goodsPostId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/goods/{goodsPostId}", goodsPostId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(response.getId()))
                .andExpect(jsonPath("$.data.status").value(response.getStatus()));

        verify(goodsPostService).getGoodsPost(goodsPostId);
    }

    @Test
    @DisplayName("메인페이지 굿즈 판매글 리스트 조회 - API 테스트")
    void get_main_goods_posts_success() throws Exception {
        // given
        Long teamId = 1L;
        GoodsPostSummaryResponse response = createGoodsPostSummaryResponse();
        List<GoodsPostSummaryResponse> goodsPosts = List.of(response);

        given(goodsPostService.getMainGoodsPosts(teamId)).willReturn(goodsPosts);

        // when & then
        mockMvc.perform(get("/api/goods/main")
                        .param("teamId", String.valueOf(teamId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.size()").value(goodsPosts.size()))
                .andExpect(jsonPath("$.data[0].id").value(response.getId()))
                .andExpect(jsonPath("$.data[0].price").value(response.getPrice()))
                .andExpect(jsonPath("$.code").value(200));

        verify(goodsPostService).getMainGoodsPosts(teamId);
    }

    @Test
    @DisplayName("메인페이지 굿즈 판매글 페이징 조회 - API 테스트")
    void get_goods_posts_page_success() throws Exception {
        // given
        Long teamId = 1L;
        String categoryVal = Category.CLOTHING.getValue();
        PageRequest pageRequest = PageRequest.of(1, 10);

        GoodsPostSummaryResponse responseDTO = createGoodsPostSummaryResponse();
        List<GoodsPostSummaryResponse> responses = List.of(responseDTO);
        PageImpl<GoodsPostSummaryResponse> pageGoodsPosts = new PageImpl<>(responses);

        PageResponse<GoodsPostSummaryResponse> pageResponse = PageResponse.<GoodsPostSummaryResponse>builder()
                .content(responses)
                .totalPages(pageGoodsPosts.getTotalPages())
                .totalElements(pageGoodsPosts.getTotalElements())
                .hasNext(pageGoodsPosts.hasNext())
                .pageNumber(pageGoodsPosts.getNumber())
                .pageSize(pageGoodsPosts.getSize())
                .build();

        given(goodsPostService.getPageGoodsPosts(teamId, categoryVal, pageRequest)).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/goods")
                        .param("teamId", String.valueOf(teamId))
                        .param("category", categoryVal)
                        .param("page", String.valueOf(pageRequest.getPageNumber()))
                        .param("size", String.valueOf(pageRequest.getPageSize())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.size()").value(responses.size()))
                .andExpect(jsonPath("$.data.content[0].id").value(responseDTO.getId()))
                .andExpect(jsonPath("$.data.content[0].price").value(responseDTO.getPrice()))
                .andExpect(jsonPath("$.data.totalPages").value(pageResponse.getTotalPages()))
                .andExpect(jsonPath("$.data.totalElements").value(pageResponse.getTotalElements()))
                .andExpect(jsonPath("$.data.pageNumber").value(pageResponse.getPageNumber()))
                .andExpect(jsonPath("$.data.pageSize").value(pageResponse.getPageSize()))
                .andExpect(jsonPath("$.code").value(200));

        verify(goodsPostService).getPageGoodsPosts(teamId, categoryVal, pageRequest);
    }

    @Test
    @DisplayName("굿즈 판매글 거래 완료 - API 테스트")
    void complete_goods_post_success() throws Exception {
        // given
        Long memberId = 1L;
        Long goodsPostId = 1L;
        Long buyerId = 2L;

        willDoNothing().given(goodsPostService).completeTransaction(memberId, goodsPostId, buyerId);

        // when & then
        mockMvc.perform(post("/api/goods/{goodsPostId}/complete", goodsPostId).param("buyerId", String.valueOf(buyerId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200));

        verify(goodsPostService).completeTransaction(memberId, goodsPostId, buyerId);
    }
}