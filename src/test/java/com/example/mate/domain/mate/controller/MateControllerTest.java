package com.example.mate.domain.mate.controller;

import static com.example.mate.common.error.ErrorCode.ALREADY_COMPLETED_POST;
import static com.example.mate.common.error.ErrorCode.MATE_POST_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.MATE_POST_UPDATE_NOT_ALLOWED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.dto.request.MatePostUpdateRequest;
import com.example.mate.domain.mate.dto.response.MatePostDetailResponse;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.dto.response.MatePostSummaryResponse;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.service.MateService;
import com.example.mate.domain.member.service.LogoutRedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


@WebMvcTest(MateController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@WithAuthMember
class MateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MateService mateService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private LogoutRedisService logoutRedisService;

    private MatePostSummaryResponse createMatePostSummaryResponse() {
        return MatePostSummaryResponse.builder()
                .imageUrl("test-image.jpg")
                .title("테스트 제목")
                .status(Status.OPEN)
                .rivalTeamName("두산")
                .matchTime(LocalDateTime.now().plusDays(1))
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

            given(mateService.createMatePost(any(MatePostCreateRequest.class), any(), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(multipart("/api/mates")
                            .file(file)
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value("모집중"))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("메이트 게시글 작성 성공 - 이미지 없음")
        void createMatePost_successWithoutImage() throws Exception {
            // given
            Long memberId = 1L;
            MatePostCreateRequest request = createMatePostRequest();
            MatePostResponse response = createMatePostResponse();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(mateService.createMatePost(any(MatePostCreateRequest.class), any(), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(multipart("/api/mates")
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value("모집중"))
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

    @Nested
    @DisplayName("메이트 게시글 상세 조회")
    class GetMatePostDetail {

        private MatePostDetailResponse createMatePostDetailResponse() {
            return MatePostDetailResponse.builder()
                    .postImageUrl("test-image.jpg")
                    .title("테스트 제목")
                    .status(Status.OPEN)
                    .rivalTeamName("두산")
                    .rivalMatchTime(LocalDateTime.now().plusDays(1))
                    .location("잠실야구장")
                    .age(Age.TWENTIES)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .maxParticipants(4)
                    .userImageUrl("user-image.jpg")
                    .nickname("테스트닉네임")
                    .manner(36.5f)
                    .content("테스트 내용입니다.")
                    .postId(1L)
                    .matchId(1L)
                    .authorId(1L)
                    .build();
        }

        @Test
        @DisplayName("메이트 게시글 상세 조회 성공")
        void getMatePostDetail_success() throws Exception {
            // given
            Long postId = 1L;
            MatePostDetailResponse response = createMatePostDetailResponse();

            given(mateService.getMatePostDetail(postId))
                    .willReturn(response);

            // when & then
            mockMvc.perform(get("/api/mates/{postId}", postId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.postId").value(postId))
                    .andExpect(jsonPath("$.data.title").value(response.getTitle()))
                    .andExpect(jsonPath("$.data.status").value(response.getStatus().getValue()))
                    .andExpect(jsonPath("$.data.rivalTeamName").value(response.getRivalTeamName()))
                    .andExpect(jsonPath("$.data.location").value(response.getLocation()))
                    .andExpect(jsonPath("$.data.age").value(response.getAge().getValue()))
                    .andExpect(jsonPath("$.data.gender").value(response.getGender().getValue()))
                    .andExpect(jsonPath("$.data.transportType").value(response.getTransportType().getValue()))
                    .andExpect(jsonPath("$.data.maxParticipants").value(response.getMaxParticipants()))
                    .andExpect(jsonPath("$.data.nickname").value(response.getNickname()))
                    .andExpect(jsonPath("$.data.manner").value(response.getManner()))
                    .andExpect(jsonPath("$.data.matchId").value(response.getMatchId()))
                    .andExpect(jsonPath("$.data.authorId").value(response.getAuthorId()))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("메이트 게시글 상세 조회 실패 - 존재하지 않는 게시글")
        void getMatePostDetail_failPostNotFound() throws Exception {
            // given
            Long nonExistentPostId = 999L;
            given(mateService.getMatePostDetail(nonExistentPostId))
                    .willThrow(new CustomException(MATE_POST_NOT_FOUND_BY_ID));

            // when & then
            mockMvc.perform(get("/api/mates/{postId}", nonExistentPostId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(404));
        }
    }

    @Nested
    @DisplayName("메이트 게시글 수정")
    class UpdateMatePost {

        private MatePostUpdateRequest createMatePostUpdateRequest() {
            return MatePostUpdateRequest.builder()
                    .teamId(1L)
                    .matchId(1L)
                    .title("수정된 제목")
                    .content("수정된 내용")
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
        @DisplayName("메이트 게시글 수정 성공")
        void updateMatePost_Success() throws Exception {
            // given
            Long postId = 1L;
            MatePostUpdateRequest request = createMatePostUpdateRequest();
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

            given(mateService.updateMatePost(any(), eq(postId), any(MatePostUpdateRequest.class), any()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders
                            .multipart(HttpMethod.PUT, "/api/mates/{postId}", postId)
                            .file(file)
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value("모집중"))
                    .andExpect(jsonPath("$.code").value(200));

            verify(mateService).updateMatePost(any(), eq(postId), any(MatePostUpdateRequest.class), any());
        }

        @Test
        @DisplayName("메이트 게시글 수정 성공 - 이미지 없음")
        void updateMatePost_SuccessWithoutImage() throws Exception {
            // given
            Long postId = 1L;
            MatePostUpdateRequest request = createMatePostUpdateRequest();
            MatePostResponse response = createMatePostResponse();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(mateService.updateMatePost(any(), eq(postId), any(MatePostUpdateRequest.class), isNull()))
                    .willReturn(response);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders
                            .multipart(HttpMethod.PUT, "/api/mates/{postId}", postId)
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value("모집중"))
                    .andExpect(jsonPath("$.code").value(200));

            verify(mateService).updateMatePost(any(), eq(postId), any(MatePostUpdateRequest.class), isNull());
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 유효하지 않은 요청 데이터")
        void updateMatePost_FailWithInvalidRequest() throws Exception {
            // given
            Long postId = 1L;
            MatePostUpdateRequest request = MatePostUpdateRequest.builder()
                    .teamId(null)  // 필수 값 누락
                    .matchId(1L)
                    .title("")     // 빈 문자열
                    .content("수정된 내용")
                    .age(Age.TWENTIES)
                    .maxParticipants(1)  // 최소값 위반
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            // when & then
            mockMvc.perform(MockMvcRequestBuilders
                            .multipart(HttpMethod.PUT, "/api/mates/{postId}", postId)
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(mateService, never()).updateMatePost(any(), any(), any(), any());
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 존재하지 않는 게시글")
        void updateMatePost_FailWithPostNotFound() throws Exception {
            // given
            Long postId = 999L;
            MatePostUpdateRequest request = createMatePostUpdateRequest();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(mateService.updateMatePost(any(), any(), any(), any()))
                    .willThrow(new CustomException(MATE_POST_NOT_FOUND_BY_ID));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders
                            .multipart(HttpMethod.PUT, "/api/mates/{postId}", postId)
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("해당 ID의 메이트 게시글을 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 권한 없음")
        void updateMatePost_FailWithUnauthorized() throws Exception {
            // given
            Long postId = 1L;
            MatePostUpdateRequest request = createMatePostUpdateRequest();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(mateService.updateMatePost(any(), any(), any(), any()))
                    .willThrow(new CustomException(MATE_POST_UPDATE_NOT_ALLOWED));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders
                            .multipart(HttpMethod.PUT, "/api/mates/{postId}", postId)
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("메이트 게시글의 작성자가 아니라면, 게시글을 수정할 수 없습니다"));
        }

        @Test
        @DisplayName("메이트 게시글 수정 실패 - 이미 완료된 게시글")
        void updateMatePost_FailWithCompletedPost() throws Exception {
            // given
            Long postId = 1L;
            MatePostUpdateRequest request = createMatePostUpdateRequest();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            given(mateService.updateMatePost(any(), any(), any(), any()))
                    .willThrow(new CustomException(ALREADY_COMPLETED_POST));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders
                            .multipart(HttpMethod.PUT, "/api/mates/{postId}", postId)
                            .file(data))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(ALREADY_COMPLETED_POST.getMessage()));
        }
    }

    @Nested
    @DisplayName("메이트 게시글 삭제")
    class DeleteMatePost {

        @Test
        @DisplayName("메이트 게시글 삭제 성공")
        void deleteMatePost_success() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;

            // when & then
            mockMvc.perform(delete("/api/mates/{postId}", postId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            verify(mateService).deleteMatePost(memberId, postId);
        }

        @Test
        @DisplayName("메이트 게시글 삭제 실패 - 존재하지 않는 게시글")
        void deleteMatePost_failPostNotFound() throws Exception {
            // given
            Long memberId = 1L;
            Long nonExistentPostId = 999L;

            doThrow(new CustomException(MATE_POST_NOT_FOUND_BY_ID))
                    .when(mateService)
                    .deleteMatePost(memberId, nonExistentPostId);

            // when & then
            mockMvc.perform(delete("/api/mates/{postId}", nonExistentPostId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("메이트 게시글 삭제 실패 - 삭제 권한 없음")
        @WithAuthMember(memberId = 2L)
        void deleteMatePost_failNotAllowed() throws Exception {
            // given
            Long memberId = 2L;  // 작성자가 아닌 다른 사용자
            Long postId = 1L;

            doThrow(new CustomException(MATE_POST_UPDATE_NOT_ALLOWED))
                    .when(mateService)
                    .deleteMatePost(memberId, postId);

            // when & then
            mockMvc.perform(delete("/api/mates/{postId}", postId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(403));
        }
    }
}