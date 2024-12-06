package com.example.mate.domain.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    // 파일명에서 허용하지 않는 문자들에 대한 정규식
    private static final String FILE_NAME_REGEX = "[^a-zA-Z0-9.\\-_]";
    private static final String FILE_NAME_REPLACEMENT = "_";

    private final AmazonS3 amazonS3;

    @Getter
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 파일 업로드
    public String uploadFile(MultipartFile file) {
        String fileName = getFileName(file);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);  // 파일 저장
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        return amazonS3.getUrl(bucket, fileName).toString();    // 파일 저장된 URL 반환
    }

    // 파일 삭제
    public void deleteFile(String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);
        try {
            amazonS3.deleteObject(bucket, key);    // 파일 삭제
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_ERROR);
        }
    }

    /**
     * S3 URL에서 객체 키를 추출
     *
     * @param imageUrl S3 URL
     * @return 추출된 객체 키
     */
    private String extractKeyFromUrl(String imageUrl) {
        return imageUrl.replace("https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/", "");
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
