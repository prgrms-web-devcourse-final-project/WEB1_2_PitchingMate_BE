package com.example.mate.domain.file;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidator {

    private static final int MAX_GOODS_POST_IMAGE_COUNT = 10;

    // 여러 파일 유효성 검사 : 굿즈 판매글
    public static void validateGoodsPostImages(List<MultipartFile> files) {
        if (files.size() > MAX_GOODS_POST_IMAGE_COUNT) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_LIMIT_EXCEEDED);
        }

        files.forEach(FileValidator::validateNotEmpty);
        files.forEach(FileValidator::isNotImage);
    }

    // 단일 파일 유효성 검사 : 회원 프로필, 메이트 구인글
    public static void validateSingleImage(MultipartFile file) {
        isNotImage(file);
    }

    private static void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_IS_EMPTY);
        }
    }

    private static void isNotImage(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new CustomException(ErrorCode.FILE_MISSING_MIME_TYPE);
        }
        if (!contentType.startsWith("image")) {
            throw new CustomException(ErrorCode.FILE_UNSUPPORTED_TYPE);
        }
    }
}
