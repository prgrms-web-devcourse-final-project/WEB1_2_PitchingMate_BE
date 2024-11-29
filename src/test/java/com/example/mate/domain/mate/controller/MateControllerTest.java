package com.example.mate.domain.mate.controller;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.dto.response.MatePostSummaryResponse;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.service.MateService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MateController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class MateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MateService mateService;

    private MatePostSummaryResponse createMatePostSummaryResponse() {
        return MatePostSummaryResponse.builder()
                .imageUrl("test-image.jpg")
                .title("테스트 제목")
                .status(Status.OPEN)
                .rivalTeamName("두산")
                .rivalMatchTime(LocalDateTime.now().plusDays(1))
                .location("잠실야구장")
                .maxParticipants(4)
                .age(Age.TWENTIES)
                .gender(Gender.FEMALE)
                .transportType(TransportType.PUBLIC)
                .build();
    }

    @Nested
    @DisplayName("메이트 게시글 작성")
    class CreateMatePost {

        private MatePostCreateRequest createMatePostRequest() {
            return MatePostCreateRequest.builder()
                    .memberId(1L)
                    .teamId(1L)
                    .matchId(1L)
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .age(Age.TWENTIES)
                    .maxParticipants(4)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();
        }

        private MatePostResponse createMatePostResponse() {
            return MatePostResponse.builder()
                    .id(1L)
                    .status(Status.OPEN)
                    .build();
        }

        @Test
        @DisplayName("메이트 게시글 작성 성공")
        void createMatePost_success() throws Exception {
            // given
            MatePostCreateRequest request = createMatePostRequest();
            MatePostResponse response = createMatePostResponse();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            given(mateService.createMatePost(any(MatePostCreateRequest.class), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(multipart("/api/mates")
                            .file(file)
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value("OPEN"))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("메이트 게시글 작성 성공 - 이미지 없음")
        void createMatePost_successWithoutImage() throws Exception {
            // given
            MatePostCreateRequest request = createMatePostRequest();
            MatePostResponse response = createMatePostResponse();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(mateService.createMatePost(any(MatePostCreateRequest.class), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(multipart("/api/mates")
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value("OPEN"))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("메인 페이지 메이트 게시글 조회")
    class GetMainPageMatePosts {

        @Test
        @DisplayName("메인 페이지 메이트 게시글 목록 조회 성공 - 팀 ID 있음")
        void getMatePostsMain_successWithTeamId() throws Exception {
            // given
            Long teamId = 1L;
            List<MatePostSummaryResponse> responses = List.of(
                    createMatePostSummaryResponse(),
                    createMatePostSummaryResponse()
            );

            given(mateService.getMainPagePosts(eq(teamId)))
                    .willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/mates/main")
                            .param("teamId", String.valueOf(teamId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("메인 페이지 메이트 게시글 목록 조회 성공 - 팀 ID 없음")
        void getMatePostsMain_successWithoutTeamId() throws Exception {
            // given
            List<MatePostSummaryResponse> responses = List.of(
                    createMatePostSummaryResponse(),
                    createMatePostSummaryResponse(),
                    createMatePostSummaryResponse()
            );

            given(mateService.getMainPagePosts(any()))
                    .willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/mates/main")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("메이트 페이지 게시글 조회")
    class GetMatePagePosts {

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 기본 파라미터")
        void getMatePagePosts_successWithDefaultParameters() throws Exception {
            // given
            PageResponse<MatePostSummaryResponse> pageResponse = PageResponse.<MatePostSummaryResponse>builder()
                    .content(List.of(createMatePostSummaryResponse()))
                    .totalPages(1)
                    .totalElements(1L)
                    .hasNext(false)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            given(mateService.getMatePagePosts(any(), any()))
                    .willReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/mates")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 모든 필터 적용")
        void getMatePagePosts_successWithAllFilters() throws Exception {
            // given
            PageResponse<MatePostSummaryResponse> pageResponse = PageResponse.<MatePostSummaryResponse>builder()
                    .content(List.of(createMatePostSummaryResponse()))
                    .totalPages(1)
                    .totalElements(1L)
                    .hasNext(false)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            given(mateService.getMatePagePosts(any(), any()))
                    .willReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("teamId", "1")
                            .param("sortType", "최근 작성일 순")
                            .param("age", "20대")
                            .param("gender", "여자")
                            .param("maxParticipants", "4")
                            .param("transportType", "대중교통")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content[0].age").value("20대"))
                    .andExpect(jsonPath("$.data.content[0].gender").value("여자"))
                    .andExpect(jsonPath("$.data.content[0].maxParticipants").value(4))
                    .andExpect(jsonPath("$.data.content[0].transportType").value("대중교통"))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 실패 - 잘못된 팀 ID")
        void getMatePagePosts_failWithInvalidTeamId() throws Exception {
            // given
            given(mateService.getMatePagePosts(any(), any()))
                    .willThrow(new CustomException(ErrorCode.TEAM_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("teamId", "999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 실패 - 잘못된 정렬 기준")
        void getMatePagePosts_failWithInvalidSortType() throws Exception {
            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("sortType", "INVALID_SORT")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 페이징 처리")
        void getMatePagePosts_successWithPaging() throws Exception {
            // given
            List<MatePostSummaryResponse> content = List.of(
                    createMatePostSummaryResponse(),
                    createMatePostSummaryResponse()
            );

            PageResponse<MatePostSummaryResponse> pageResponse = PageResponse.<MatePostSummaryResponse>builder()
                    .content(content)
                    .totalPages(5)
                    .totalElements(50L)
                    .hasNext(true)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            given(mateService.getMatePagePosts(any(), any()))
                    .willReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalPages").value(5))
                    .andExpect(jsonPath("$.data.totalElements").value(50))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}