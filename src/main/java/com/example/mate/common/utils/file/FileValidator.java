package com.example.mate.common.utils.file;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator {

    private static final int MAX_GOODS_POST_IMAGE_COUNT = 10;

    // 굿즈 판매글 이미지 파일 유효성 검사
    public static void validateGoodsPostImages(List<MultipartFile> files) {
        files.forEach(FileValidator::validateNotEmpty);
        files.forEach(FileValidator::isNotImage);

        if (files.size() > MAX_GOODS_POST_IMAGE_COUNT) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_LIMIT_EXCEEDED);
        }
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
