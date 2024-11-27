package com.example.mate.common.utils.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileUploaderTest {

    @Test
    @DisplayName("파일 업로드 시 저장할 수 없는 파일명은 대체 문자로 변경되어야 한다.")
    void uploadFile_should_replace_invalid_characters() {
        // given
        String originalFileName = "test file name @123.jpg";
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(originalFileName);

        // when
        String uploadUrl = FileUploader.uploadFile(mockFile);
        // 파일명 추출
        String fileName = uploadUrl.substring(uploadUrl.lastIndexOf("/") + 1);
        String cleanedFileName = fileName.substring(fileName.indexOf('_') + 1);

        // then
        assertThat(cleanedFileName).isEqualTo("test_file_name__123.jpg");
    }

    @Test
    @DisplayName("파일 업로드 시 파일명에 UUID 가 포함되어야 한다.")
    void uploadFile_should_contain_uuid() {
        // given
        String originalFileName = "test_filename.jpg";
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(originalFileName);

        // when
        String uploadUrl = FileUploader.uploadFile(mockFile);
        String fileName = uploadUrl.substring(uploadUrl.lastIndexOf("/") + 1);
        String uuid = fileName.substring(0, fileName.indexOf('_'));

        // then
        assertThat(uuid).matches("[0-9a-fA-F-]{36}");
        assertThat(uuid).hasSize(36);
    }
}