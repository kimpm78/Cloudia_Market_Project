package com.cloudia.backend.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * S3 파일 업로드/삭제 서비스 인터페이스
 */
public interface S3Service {

    /**
     * S3에 파일 업로드
     * 
     * @param file   업로드할 파일
     * @param folder S3 내 폴더명 (예: "images", "documents")
     * @return 업로드된 파일의 전체 URL
     * @throws IOException 파일 읽기 실패 시
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;

    /**
     * S3에서 파일 삭제
     * 
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    void deleteFile(String fileUrl);

    /**
     * S3 내에서 파일 이동 (복사 후 원본 삭제)
     * 
     * @param sourceUrl    원본 파일 URL
     * @param targetFolder 대상 폴더 (예: "images/product/P001")
     * @return 이동된 파일의 전체 URL
     */
    String moveFile(String sourceUrl, String targetFolder);
}