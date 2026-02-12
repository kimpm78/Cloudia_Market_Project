package com.cloudia.backend.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * S3ファイルのアップロード/削除サービスインターフェース（実際利用する場合）
 */
public interface S3Service {

    /**
     * S3へファイルをアップロード
     *
     * @param file   アップロードするファイル
     * @param folder S3上のフォルダ名（例: "images", "documents"）
     * @return アップロードしたファイルの完全URL
     * @throws IOException ファイル読み取りに失敗した場合
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;

    /**
     * S3からファイルを削除
     *
     * @param fileUrl 削除対象ファイルの完全URL
     */
    void deleteFile(String fileUrl);

    /**
     * S3内でファイルを移動（コピー後に元ファイルを削除）
     *
     * @param sourceUrl    元ファイルURL
     * @param targetFolder 移動先フォルダ（例: "images/product/P001"）
     * @return 移動後ファイルの完全URL
     */
    String moveFile(String sourceUrl, String targetFolder);
}