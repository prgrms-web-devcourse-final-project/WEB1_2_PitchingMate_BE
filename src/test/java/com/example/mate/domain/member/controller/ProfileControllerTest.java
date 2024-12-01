package com.example.mate.domain.member.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.member.dto.response.MyGoodsRecordResponse;
import com.example.mate.domain.member.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    private MyGoodsRecordResponse createMyGoodsRecordResponse() {
        return MyGoodsRecordResponse.builder()
                .postId(1L)
                .title("test title")
                .imageUrl("test.png")
                .price(10000)
                .author("tester1")
                .createdAt(LocalDateTime.now().minusDays(7))
                .build();
    }

    @Nested
    @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회")
    class ProfileSoldGoodsPage {

        @Test
        @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회 성공")
        void get_sold_goods_page_success() throws Exception {
            // given
            Long memberId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            MyGoodsRecordResponse responseDTO = createMyGoodsRecordResponse();
            List<MyGoodsRecordResponse> content = List.of(responseDTO);
            PageImpl<MyGoodsRecordResponse> soldGoodsPage = new PageImpl<>(content);

            PageResponse<MyGoodsRecordResponse> response = PageResponse.<MyGoodsRecordResponse>builder()
                    .content(content)
                    .totalPages(soldGoodsPage.getTotalPages())
                    .totalElements(soldGoodsPage.getTotalElements())
                    .hasNext(soldGoodsPage.hasNext())
                    .pageNumber(soldGoodsPage.getNumber())
                    .pageSize(soldGoodsPage.getSize())
                    .build();

            given(profileService.getSoldGoodsPage(memberId, pageable)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/goods/sold", memberId)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content.size()").value(content.size()))
                    .andExpect(jsonPath("$.data.content[0].postId").value(responseDTO.getPostId()))
                    .andExpect(jsonPath("$.data.content[0].price").value(responseDTO.getPrice()))
                    .andExpect(jsonPath("$.data.totalPages").value(response.getTotalPages()))
                    .andExpect(jsonPath("$.data.totalElements").value(response.getTotalElements()))
                    .andExpect(jsonPath("$.data.pageNumber").value(response.getPageNumber()))
                    .andExpect(jsonPath("$.data.pageSize").value(response.getPageSize()))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 프로필 굿즈 판매기록 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_sold_goods_page_invalid_member_id() throws Exception {
            // given
            Long memberId = 999L; // 존재 하지 않는 아이디
            Pageable pageable = PageRequest.of(0, 10);

            willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID))
                    .given(profileService).getSoldGoodsPage(memberId, pageable);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/goods/sold", memberId)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage()));

            verify(profileService, times(1)).getSoldGoodsPage(memberId, pageable);
        }
    }

    @Nested
    @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회")
    class ProfileBoughtGoodsPage {

        @Test
        @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회 성공")
        void get_bought_goods_page_success() throws Exception {
            // given
            Long memberId = 2L;
            Pageable pageable = PageRequest.of(0, 10);
            MyGoodsRecordResponse responseDTO = createMyGoodsRecordResponse();
            List<MyGoodsRecordResponse> content = List.of(responseDTO);
            PageImpl<MyGoodsRecordResponse> boughtGoodsPage = new PageImpl<>(content);

            PageResponse<MyGoodsRecordResponse> response = PageResponse.<MyGoodsRecordResponse>builder()
                    .content(content)
                    .totalPages(boughtGoodsPage.getTotalPages())
                    .totalElements(boughtGoodsPage.getTotalElements())
                    .hasNext(boughtGoodsPage.hasNext())
                    .pageNumber(boughtGoodsPage.getNumber())
                    .pageSize(boughtGoodsPage.getSize())
                    .build();

            given(profileService.getBoughtGoodsPage(memberId, pageable)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/goods/bought", memberId)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content.size()").value(content.size()))
                    .andExpect(jsonPath("$.data.content[0].postId").value(responseDTO.getPostId()))
                    .andExpect(jsonPath("$.data.content[0].price").value(responseDTO.getPrice()))
                    .andExpect(jsonPath("$.data.totalPages").value(response.getTotalPages()))
                    .andExpect(jsonPath("$.data.totalElements").value(response.getTotalElements()))
                    .andExpect(jsonPath("$.data.pageNumber").value(response.getPageNumber()))
                    .andExpect(jsonPath("$.data.pageSize").value(response.getPageSize()))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("회원 프로필 굿즈 구매기록 페이징 조회 실패 - 유효하지 않은 회원 아이디로 조회")
        void get_bought_goods_page_invalid_member_id() throws Exception {
            // given
            Long memberId = 999L; // 존재 하지 않는 아이디
            Pageable pageable = PageRequest.of(0, 10);

            willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID))
                    .given(profileService).getBoughtGoodsPage(memberId, pageable);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/goods/bought", memberId)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage()));

            verify(profileService, times(1)).getBoughtGoodsPage(memberId, pageable);
        }
    }
}