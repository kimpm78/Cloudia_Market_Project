package com.cloudia.backend.common.service.impl;

import com.cloudia.backend.common.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * S3 파일 업로드/삭제 서비스 구현체
 */
@Service
@Profile({ "dev", "prod" })
@RequiredArgsConstructor
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${app.upload.s3.bucket}")
    private String bucketName;

    @Value("${app.upload.s3.base-url}")
    private String baseUrl;

    /**
     * S3에 파일 업로드
     * 파일명 형식: folder/yyyy/MM/dd/UUID.확장자
     * 
     * @param file   업로드할 파일
     * @param folder S3 내 폴더명
     * @return 업로드된 파일의 전체 URL
     * @throws IOException 파일 읽기 실패 시
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // 원본 파일명에서 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 날짜별 폴더 생성 (yyyy/MM/dd)
        // String dateFolder =
        // LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // UUID를 사용한 고유 파일명 생성
        String fileName = UUID.randomUUID().toString() + extension;

        // S3 키 생성 (전체 경로)
        String s3Key = folder + "/" + fileName;

        // S3 업로드 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        // S3에 파일 업로드
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("S3 업로드 완료: {}", s3Key);

        // 전체 URL 반환
        return baseUrl + "/" + s3Key;
    }

    /**
     * S3에서 파일 삭제
     * 
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // URL에서 S3 키 추출
            String s3Key = fileUrl.replace(baseUrl + "/", "");

            // S3 삭제 요청 생성
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // S3에서 파일 삭제
            s3Client.deleteObject(deleteObjectRequest);

            log.info("S3 파일 삭제 완료: {}", s3Key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", fileUrl, e);
        }
    }

    /**
     * S3 내에서 파일 이동 (복사 후 원본 삭제)
     * 
     * @param sourceUrl    원본 파일 URL
     * @param targetFolder 대상 폴더
     * @return 이동된 파일의 전체 URL
     */
    @Override
    public String moveFile(String sourceUrl, String targetFolder) {
        try {
            // URL에서 S3 키 추출
            String sourceKey = sourceUrl.replace(baseUrl + "/", "");

            // 파일명 추출
            String fileName = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);

            // 새 경로 생성
            String targetKey = targetFolder + "/" + fileName;

            // S3 복사
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(targetKey)
                    .build();

            s3Client.copyObject(copyRequest);

            log.info("S3 파일 복사 완료: {} → {}", sourceKey, targetKey);

            // 원본 삭제
            deleteFile(sourceUrl);

            // 새 URL 반환
            return baseUrl + "/" + targetKey;

        } catch (Exception e) {
            log.error("S3 파일 이동 실패: {}", sourceUrl, e);
            return null;
        }
    }
}