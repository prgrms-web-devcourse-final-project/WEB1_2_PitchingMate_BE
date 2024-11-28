package com.example.mate.domain.mate.controller;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.dto.response.MatePostResponse;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.service.MateService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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