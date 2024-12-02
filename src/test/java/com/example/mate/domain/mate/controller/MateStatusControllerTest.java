package com.example.mate.domain.mate.controller;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.mate.dto.request.MatePostCompleteRequest;
import com.example.mate.domain.mate.dto.request.MatePostStatusRequest;
import com.example.mate.domain.mate.dto.response.MatePostCompleteResponse;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.entity.Status;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.mate.common.error.ErrorCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MateController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class MateStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MateService mateService;

    @Nested
    @DisplayName("메이트 게시글 상태 변경")
    class UpdateMatePostStatus {

        @Test
        @DisplayName("메이트 게시글 상태 변경 성공 - OPEN 상태로 변경")
        void updateMatePostStatus_successToOpen() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            List<Long> participantIds = List.of(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.OPEN, participantIds);
            MatePostResponse response = MatePostResponse.builder()
                    .id(postId)
                    .status(Status.OPEN)
                    .build();

            given(mateService.updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(postId))
                    .andExpect(jsonPath("$.data.status").value("모집중"))
                    .andExpect(jsonPath("$.code").value(200));

            verify(mateService).updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class));
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 성공 - CLOSED 상태로 변경")
        void updateMatePostStatus_successToClosed() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            List<Long> participantIds = List.of(2L, 3L, 4L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);
            MatePostResponse response = MatePostResponse.builder()
                    .id(postId)
                    .status(Status.CLOSED)
                    .build();

            given(mateService.updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(postId))
                    .andExpect(jsonPath("$.data.status").value("모집완료"))
                    .andExpect(jsonPath("$.code").value(200));

            verify(mateService).updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class));
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 존재하지 않는 게시글")
        void updateMatePostStatus_failPostNotFound() throws Exception {
            // given
            Long memberId = 1L;
            Long nonExistentPostId = 999L;
            List<Long> participantIds = List.of(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateService.updateMatePostStatus(eq(memberId), eq(nonExistentPostId), any(MatePostStatusRequest.class)))
                    .willThrow(new CustomException(MATE_POST_NOT_FOUND_BY_ID));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, nonExistentPostId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(404));

            verify(mateService).updateMatePostStatus(eq(memberId), eq(nonExistentPostId), any(MatePostStatusRequest.class));
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 권한 없음")
        void updateMatePostStatus_failNotAuthorized() throws Exception {
            // given
            Long memberId = 2L;
            Long postId = 1L;
            List<Long> participantIds = List.of(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateService.updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class)))
                    .willThrow(new CustomException(ErrorCode.MATE_POST_UPDATE_NOT_ALLOWED));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(403));

            verify(mateService).updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class));
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - VISIT_COMPLETE로 변경 시도")
        void updateMatePostStatus_failWithCompleteStatus() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            List<Long> participantIds = List.of(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.VISIT_COMPLETE, participantIds);

            given(mateService.updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class)))
                    .willThrow(new CustomException(ALREADY_COMPLETED_POST));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(403));

            verify(mateService).updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class));
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 이미 완료된 게시글")
        void updateMatePostStatus_failAlreadyCompleted() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            List<Long> participantIds = List.of(2L, 3L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateService.updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class)))
                    .willThrow(new CustomException(ALREADY_COMPLETED_POST));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(403));

            verify(mateService).updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class));
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 참여자 수 초과")
        void updateMatePostStatus_failMaxParticipantsExceeded() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            // @Size(max = 9) 제약조건을 통과하도록 9명으로 수정
            List<Long> participantIds = List.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateService.updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class)))
                    .willThrow(new CustomException(MATE_POST_MAX_PARTICIPANTS_EXCEEDED));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_MAX_PARTICIPANTS_EXCEEDED.getMessage()))
                    .andExpect(jsonPath("$.code").value(400));

            verify(mateService).updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class));
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 참여자 수 validation 실패")
        void updateMatePostStatus_failMaxParticipantsValidation() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            // @Size(max = 9) 제약조건을 초과하는 10명
            List<Long> participantIds = List.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("participantIds: 방장 포함 최대 10명까지만 참여할 수 있습니다"))
                    .andExpect(jsonPath("$.code").value(400));

            // validation 실패로 서비스 호출되지 않음
            verify(mateService, never()).updateMatePostStatus(any(), any(), any());
        }

        @Test
        @DisplayName("메이트 게시글 상태 변경 실패 - 잘못된 참여자 ID")
        void updateMatePostStatus_failInvalidParticipantIds() throws Exception {
            // given
            Long memberId = 1L;
            Long postId = 1L;
            List<Long> participantIds = List.of(999L, 998L); // 존재하지 않는 참여자 ID
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            given(mateService.updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class)))
                    .willThrow(new CustomException(INVALID_MATE_POST_PARTICIPANT_IDS));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/status", memberId, postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(400));

            verify(mateService).updateMatePostStatus(eq(memberId), eq(postId), any(MatePostStatusRequest.class));
        }
    }

    @Nested
    @DisplayName("메이트 게시글 직관 완료")
    class CompleteVisit {
        private static final Long MEMBER_ID = 1L;
        private static final Long POST_ID = 1L;

        private MatePostCompleteRequest createRequest() {
            return new MatePostCompleteRequest(List.of(2L, 3L));
        }

        private MatePostCompleteResponse createResponse() {
            return MatePostCompleteResponse.builder()
                    .id(POST_ID)
                    .status(Status.VISIT_COMPLETE)
                    .participantIds(List.of(2L, 3L))
                    .build();
        }

        @Test
        @DisplayName("메이트 게시글 직관 완료 처리 성공")
        void completeVisit_success() throws Exception {
            // given
            MatePostCompleteRequest request = createRequest();
            MatePostCompleteResponse response = createResponse();

            given(mateService.completeVisit(eq(MEMBER_ID), eq(POST_ID), any(MatePostCompleteRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/complete", MEMBER_ID, POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(POST_ID))
                    .andExpect(jsonPath("$.data.status").value("직관완료"))
                    .andExpect(jsonPath("$.data.participantIds").isArray())
                    .andExpect(jsonPath("$.data.participantIds.length()").value(2))
                    .andExpect(jsonPath("$.code").value(200));

            verify(mateService).completeVisit(eq(MEMBER_ID), eq(POST_ID), any(MatePostCompleteRequest.class));
        }

        @Test
        @DisplayName("메이트 게시글 직관 완료 처리 실패 - 존재하지 않는 게시글")
        void completeVisit_failPostNotFound() throws Exception {
            // given
            MatePostCompleteRequest request = createRequest();

            given(mateService.completeVisit(eq(MEMBER_ID), eq(POST_ID), any(MatePostCompleteRequest.class)))
                    .willThrow(new CustomException(MATE_POST_NOT_FOUND_BY_ID));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/complete", MEMBER_ID, POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_NOT_FOUND_BY_ID.getMessage()))
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("메이트 게시글 직관 완료 처리 실패 - 권한 없음")
        void completeVisit_failNotAuthorized() throws Exception {
            // given
            MatePostCompleteRequest request = createRequest();

            given(mateService.completeVisit(eq(MEMBER_ID), eq(POST_ID), any(MatePostCompleteRequest.class)))
                    .willThrow(new CustomException(MATE_POST_UPDATE_NOT_ALLOWED));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/complete", MEMBER_ID, POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_UPDATE_NOT_ALLOWED.getMessage()))
                    .andExpect(jsonPath("$.code").value(403));
        }

        @Test
        @DisplayName("메이트 게시글 직관 완료 처리 실패 - 경기 시작 전")
        void completeVisit_failBeforeMatchTime() throws Exception {
            // given
            MatePostCompleteRequest request = createRequest();

            given(mateService.completeVisit(eq(MEMBER_ID), eq(POST_ID), any(MatePostCompleteRequest.class)))
                    .willThrow(new CustomException(MATE_POST_COMPLETE_TIME_NOT_ALLOWED));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/complete", MEMBER_ID, POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(403));
        }

        @Test
        @DisplayName("메이트 게시글 직관 완료 처리 실패 - 모집완료 상태가 아님")
        void completeVisit_failNotClosedStatus() throws Exception {
            // given
            MatePostCompleteRequest request = createRequest();

            given(mateService.completeVisit(eq(MEMBER_ID), eq(POST_ID), any(MatePostCompleteRequest.class)))
                    .willThrow(new CustomException(NOT_CLOSED_STATUS_FOR_COMPLETION));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/complete", MEMBER_ID, POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(400));
        }

        @Test
        @DisplayName("메이트 게시글 직관 완료 처리 실패 - 참여자 수 초과")
        void completeVisit_failExceededParticipants() throws Exception {
            // given
            MatePostCompleteRequest request = createRequest();

            given(mateService.completeVisit(eq(MEMBER_ID), eq(POST_ID), any(MatePostCompleteRequest.class)))
                    .willThrow(new CustomException(MATE_POST_MAX_PARTICIPANTS_EXCEEDED));

            // when & then
            mockMvc.perform(patch("/api/mates/{memberId}/{postId}/complete", MEMBER_ID, POST_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }
}
