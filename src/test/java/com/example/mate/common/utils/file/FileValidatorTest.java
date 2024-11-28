package com.example.mate.common.utils.file;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileValidatorTest {

    @Mock
    private MultipartFile file;

    @Test
    @DisplayName("파일이 비어 있다면, CustomException 을 발생시킨다.")
    void should_throw_CustomException_when_file_is_empty() {
        // given
        MultipartFile mockFile = mock(MultipartFile.class);

        // when
        when(mockFile.isEmpty()).thenReturn(true);

        // then
        assertThatThrownBy(() -> FileValidator.validateGoodsPostImages(List.of(mockFile)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.FILE_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("파일의 MIME 타입이 없다면, CustomException 을 발생시킨다.")
    void should_throw_CustomException_when_contentType_is_missing() {
        // given
        MultipartFile mockFile = mock(MultipartFile.class);

        // when
        when(mockFile.getContentType()).thenReturn(null);

        // then
        assertThatThrownBy(() -> FileValidator.validateGoodsPostImages(List.of(mockFile)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.FILE_MISSING_MIME_TYPE.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/**", "Application/**", "audio/**", "multipart/**"})
    @DisplayName("지원하지 않는 MIME 타입이라면, CustomException 을 발생시킨다.")
    void should_throw_CustomException_when_contentType_is_unsupported_type(String contentType) {
        // given
        MultipartFile mockFile = mock(MultipartFile.class);

        // when
        when(mockFile.getContentType()).thenReturn(contentType);

        // then
        assertThatThrownBy(() -> FileValidator.validateGoodsPostImages(List.of(mockFile)))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.FILE_UNSUPPORTED_TYPE.getMessage());
    }

    @Test
    @DisplayName("파일 개수가 10개를 초과한다면, CustomException 을 발생시킨다.")
    void should_throw_CustomException_when_file_size_is_over_10() {
        // given
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> files = new ArrayList<>(Collections.nCopies(11, mockFile));

        // when
        when(mockFile.getContentType()).thenReturn("image/jpg");

        // then
        assertThatThrownBy(() -> FileValidator.validateGoodsPostImages(files))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.FILE_UPLOAD_LIMIT_EXCEEDED.getMessage());
    }
}