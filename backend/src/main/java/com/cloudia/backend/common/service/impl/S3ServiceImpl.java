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
 * S3ファイルのアップロード／削除サービス実装
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
     * S3へファイルをアップロード
     * ファイル名形式: folder/yyyy/MM/dd/UUID.拡張子
     *
     * @param file   アップロードするファイル
     * @param folder S3上のフォルダ名
     * @return アップロードしたファイルの完全URL
     * @throws IOException ファイル読み取りに失敗した場合
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // 元のファイル名から拡張子を抽出
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 日付別フォルダ生成（yyyy/MM/dd）
        // String dateFolder =
        // LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // UUIDを用いた一意なファイル名を生成
        String fileName = UUID.randomUUID().toString() + extension;

        // S3キーを生成（フルパス）
        String s3Key = folder + "/" + fileName;

        // S3アップロードリクエストを作成
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        // S3へファイルをアップロード
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("S3アップロード完了: {}", s3Key);

        // 完全URLを返却
        return baseUrl + "/" + s3Key;
    }

    /**
     * S3からファイルを削除
     *
     * @param fileUrl 削除対象ファイルの完全URL
     */
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // URLからS3キーを抽出
            String s3Key = fileUrl.replace(baseUrl + "/", "");

            // S3削除リクエストを作成
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // S3からファイルを削除
            s3Client.deleteObject(deleteObjectRequest);

            log.info("S3ファイル削除完了: {}", s3Key);
        } catch (Exception e) {
            log.error("S3ファイル削除失敗: {}", fileUrl, e);
        }
    }

    /**
     * S3内でファイルを移動（コピー後に元ファイルを削除）
     *
     * @param sourceUrl    元ファイルURL
     * @param targetFolder 移動先フォルダ
     * @return 移動後ファイルの完全URL
     */
    @Override
    public String moveFile(String sourceUrl, String targetFolder) {
        try {
            // URLからS3キーを抽出
            String sourceKey = sourceUrl.replace(baseUrl + "/", "");

            // ファイル名を抽出
            String fileName = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);

            // 新しいパスを作成
            String targetKey = targetFolder + "/" + fileName;

            // S3へコピー
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(targetKey)
                    .build();

            s3Client.copyObject(copyRequest);

            log.info("S3ファイルコピー完了: {} → {}", sourceKey, targetKey);

            // 元ファイルを削除
            deleteFile(sourceUrl);

            // 新しいURLを返却
            return baseUrl + "/" + targetKey;

        } catch (Exception e) {
            log.error("S3ファイル移動失敗: {}", sourceUrl, e);
            return null;
        }
    }
}