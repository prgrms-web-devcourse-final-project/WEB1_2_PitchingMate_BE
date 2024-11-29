package com.example.mate.domain.goods.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.dto.response.GoodsPostSummaryResponse;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.service.GoodsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@WebMvcTest(GoodsController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class GoodsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GoodsService goodsService;

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

        given(goodsService.registerGoodsPost(eq(memberId), any(GoodsPostRequest.class), anyList())).willReturn(response);

        // when
        MockMultipartHttpServletRequestBuilder multipartRequest = multipart("/api/goods/{memberId}", memberId).file(data);
        files.forEach(multipartRequest::file);

        // then
        mockMvc.perform(multipartRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(response.getId()))
                .andExpect(jsonPath("$.data.status").value(response.getStatus()))
                .andExpect(jsonPath("$.code").value(200));

        verify(goodsService).registerGoodsPost(eq(memberId), any(GoodsPostRequest.class), anyList());
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

        given(goodsService.updateGoodsPost(eq(memberId), eq(goodsPostId), any(GoodsPostRequest.class), anyList()))
                .willReturn(response);

        // when
        MockMultipartHttpServletRequestBuilder multipartRequest = multipart("/api/goods/{memberId}/post/{goodsPostId}", memberId, goodsPostId)
                .file(data);
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

        verify(goodsService).updateGoodsPost(eq(memberId), eq(goodsPostId), any(GoodsPostRequest.class), anyList());
    }

    @Test
    @DisplayName("굿즈 판매글 삭제 - API 테스트")
    void delete_goods_post_success() throws Exception {
        // given
        Long memberId = 1L;
        Long goodsPostId = 1L;

        willDoNothing().given(goodsService).deleteGoodsPost(memberId, goodsPostId);

        // when & then
        mockMvc.perform(delete("/api/goods/{memberId}/post/{goodsPostId}", memberId, goodsPostId))
                .andDo(print())
                .andExpect(status().isNoContent());


        verify(goodsService).deleteGoodsPost(memberId, goodsPostId);
    }

    @Test
    @DisplayName("굿즈 판매글 상세 조회 - API 테스트")
    void get_goods_post_success() throws Exception {
        // given
        Long goodsPostId = 1L;
        GoodsPostResponse response = createGoodsPostResponse();

        given(goodsService.getGoodsPost(goodsPostId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/goods/{goodsPostId}", goodsPostId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(response.getId()))
                .andExpect(jsonPath("$.data.status").value(response.getStatus()));

        verify(goodsService).getGoodsPost(goodsPostId);
    }

    @Test
    @DisplayName("메인페이지 굿즈 판매글 리스트 조회 - API 테스트")
    void get_main_goods_posts_success() throws Exception {
        // given
        Long teamId = 1L;
        GoodsPostSummaryResponse response = createGoodsPostSummaryResponse();
        List<GoodsPostSummaryResponse> goodsPosts = List.of(response);

        given(goodsService.getMainGoodsPosts(teamId)).willReturn(goodsPosts);

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

        verify(goodsService).getMainGoodsPosts(teamId);
    }
}