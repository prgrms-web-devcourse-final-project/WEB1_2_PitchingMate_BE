package com.example.mate.domain.file;

import static com.example.mate.domain.file.FileUtils.getFileName;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileService {

    private static final double THUMBNAIL_RATIO = 2;
    private static final String THUMBNAIL_PREFIX = "t_";

    private final AmazonS3 amazonS3;

    @Getter
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 이미지 파일을 S3 버킷에 업로드하는 메서드
     *
     * @param file 업로드할 MultipartFile
     * @return 업로드된 파일의 이름
     */
    public String uploadFile(MultipartFile file) {
        // UUID 를 포함한 새로운 파일명 반환
        String fileName = getFileName(file);

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);  // 파일 저장
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        return fileName;
    }

    /**
     * 원본 이미지와 썸네일 이미지를 S3 버킷에 업로드하는 메서드
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드된 원본 파일의 이름
     */
    public String uploadImageWithThumbnail(MultipartFile file) {
        // 원본 파일 S3 업로드
        String originalFileName = uploadFile(file);

        // 썸네일 생성
        ByteArrayInputStream thumbnailStream = createThumbnailStream(file);

        // 썸네일 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(thumbnailStream.available());
        metadata.setContentType(file.getContentType());

        // 썸네일 이미지 업로드
        try {
            amazonS3.putObject(bucket, THUMBNAIL_PREFIX + originalFileName, thumbnailStream, metadata);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
        return originalFileName;
    }

    /**
     * 파일의 썸네일 이미지를 생성하는 메서드
     *
     * @param file 원본 이미지 파일
     * @return 썸네일 이미지의 ByteArrayInputStream
     */
    private ByteArrayInputStream createThumbnailStream(MultipartFile file) {
        // try-with-resources 문으로 auto close
        try (ByteArrayOutputStream thumbnailOutputStream = new ByteArrayOutputStream();
             InputStream fileInputStream = file.getInputStream()) {

            // BufferedImage 를 이용해 이미지를 메모리에서 로드
            BufferedImage originalImage = ImageIO.read(fileInputStream);
            int width = (int) (originalImage.getWidth() / THUMBNAIL_RATIO);
            int height = (int) (originalImage.getHeight() / THUMBNAIL_RATIO);

            // 썸네일 생성
            Thumbnails.of(originalImage)
                    .size(width, height)
                    .outputFormat("jpg")
                    .toOutputStream(thumbnailOutputStream);
            return new ByteArrayInputStream(thumbnailOutputStream.toByteArray());
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * S3에 업로드된 파일과 해당 파일의 썸네일을 삭제하는 메서드
     * - 썸네일 이미지가 없으면 무시
     *
     * @param fileName 삭제할 파일 이름
     */
    public void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(bucket, fileName);
            amazonS3.deleteObject(bucket, THUMBNAIL_PREFIX + fileName);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_ERROR);
        }
    }
}
