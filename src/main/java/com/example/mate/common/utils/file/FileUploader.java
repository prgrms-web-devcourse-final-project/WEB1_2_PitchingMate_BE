package com.example.mate.common.utils.file;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public class FileUploader {

    // 파일명에서 허용하지 않는 문자들에 대한 정규식
    private static final String FILE_NAME_REGEX = "[^a-zA-Z0-9.\\-_]";
    private static final String FILE_NAME_REPLACEMENT = "_";

    public static String uploadFile(MultipartFile file) {
        String fileName = getFileName(file);

        // TODO : 2024/11/27 - S3 파일 업로드 기능, 경로를 포함한 url 반환

        return "upload/" + fileName;
    }

    public static boolean deleteFile(String imageUrl) {
        // TODO : 2024/11/28 - S3 파일 삭제 기능, 삭제 여부를 boolean 값으로 반환

        return true;
    }

    /**
     * 파일명에서 허용되지 않는 문자를 제거하고, UUID 를 추가한 새로운 파일명을 생성
     *
     * @param file 업로드할 파일
     * @return UUID 를 포함한 새로운 파일명
     */
    private static String getFileName(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        return uuid + FILE_NAME_REPLACEMENT + cleanFileName(file.getOriginalFilename());
    }

    /**
     * 파일 이름에서 허용되지 않는 문자를 대체 문자로 변경합니다.
     *
     * @param fileName 원본 파일명
     * @return 대체된 파일명
     */
    private static String cleanFileName(String fileName) {
        return fileName.replaceAll(FILE_NAME_REGEX, FILE_NAME_REPLACEMENT);
    }
}
