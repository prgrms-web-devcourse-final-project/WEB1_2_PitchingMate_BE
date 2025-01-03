package com.example.mate.domain.file;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUtils {

    private static final String FILE_NAME_REGEX = "[^a-zA-Z0-9.\\-_]";
    private static final String FILE_NAME_REPLACEMENT = "_";
    private static final String THUMBNAIL_PREFIX = "t_";

    private static String AWS_BUCKET_URL;

    @Value("${cloud.aws.s3.bucket}")
    public void setAwsBucketUrl(String bucket) {
        AWS_BUCKET_URL = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/";
    }

    public static String getImageUrl(String fileName) {
        return AWS_BUCKET_URL + fileName;
    }

    public static String getThumbnailImageUrl(String fileName) {
        return AWS_BUCKET_URL + THUMBNAIL_PREFIX  + fileName;
    }

    /**
     * 파일명에서 허용되지 않는 문자를 제거하고, UUID 를 추가한 새로운 파일명을 생성
     *
     * @param file 업로드할 파일
     * @return UUID 를 포함한 새로운 파일명
     */
    public static String getFileName(MultipartFile file) {
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
