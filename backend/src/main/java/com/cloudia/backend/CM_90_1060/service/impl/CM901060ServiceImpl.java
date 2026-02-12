package com.cloudia.backend.CM_90_1060.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_90_1040.constants.CM901040Constant;
import com.cloudia.backend.CM_90_1040.constants.CM901040MessageConstant;
import com.cloudia.backend.CM_90_1060.mapper.CM901060Mapper;
import com.cloudia.backend.CM_90_1060.model.Attachments;
import com.cloudia.backend.CM_90_1060.model.Categories;
import com.cloudia.backend.CM_90_1060.model.CategoryDetails;
import com.cloudia.backend.CM_90_1060.model.ProductDetails;
import com.cloudia.backend.CM_90_1060.model.ProductUpt;
import com.cloudia.backend.CM_90_1060.model.Products;
import com.cloudia.backend.CM_90_1060.model.RequestModel;
import com.cloudia.backend.CM_90_1060.model.ResponseProducts;
import com.cloudia.backend.CM_90_1060.model.Stock;
import com.cloudia.backend.CM_90_1060.service.CM901060Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.exception.AuthenticationException;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.service.S3Service;
import com.cloudia.backend.common.util.DateCalculator;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901060ServiceImpl implements CM901060Service {
    @Value("${uuid.upload.dir}")
    private String uuidDir;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.type}")
    private String uploadType;

    @Autowired(required = false)
    private S3Service s3Service;

    @Value("${app.upload.s3.base-url:}")
    private String baseUrl;

    private final CM901060Mapper cm901060Mapper;
    private final DateCalculator dateCalculator;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ResponseProducts>>> findAllProduct() {
        try {
            List<ResponseProducts> responseProducts = cm901060Mapper.findAllProduct();
            log.info("取得したカテゴリグループ数: {}", responseProducts == null ? 0 : responseProducts.size());

            return ResponseEntity.ok(createResponseModel(responseProducts, true, "カテゴリグループの取得に成功しました"));
        } catch (DataAccessException dae) {
            // DB関連例外
            log.error("DBアクセス中にエラーが発生しました: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "データベースエラーが発生しました。"));

        } catch (NullPointerException npe) {
            // Null処理例外
            log.error("NullPointerExceptionが発生しました: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "必須データが不足しています。"));

        } catch (Exception e) {
            // その他の一般例外
            log.error("予期しないエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "サーバー内部エラーが発生しました。"));
        }
    }

    /**
     * 特定商品の一覧を取得
     *
     * @param searchTerm 検索キーワード
     * @param searchType 検索タイプ（1: 商品コード、2: 商品名）
     * @return 商品一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ResponseProducts>>> getFindProduct(String searchTerm, int searchType) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.warn("商品検索失敗: 検索語が空です。");
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyList(), false, "検索語を入力してください。"));
        }

        String trimmedSearchTerm = searchTerm.trim();
        log.info("商品検索開始, 検索語: {}", trimmedSearchTerm);

        try {
            List<ResponseProducts> responseProducts = cm901060Mapper.findByProduct(trimmedSearchTerm, searchType);
            if (responseProducts == null) {
                responseProducts = Collections.emptyList();
            }

            return ResponseEntity.ok(createResponseModel(responseProducts, true, "カテゴリグループの取得に成功しました"));
        } catch (DataAccessException dae) {
            log.error("DBアクセス中にエラーが発生しました: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "データベースエラーが発生しました。"));

        } catch (NullPointerException npe) {
            log.error("NullPointerExceptionが発生しました: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "必須データが不足しています。"));

        } catch (Exception e) {
            log.error("予期しないエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "サーバー内部エラーが発生しました。"));
        }
    }

    /**
     * 商品削除
     *
     * @param productIds 削除対象IDのリスト
     * @return 削除結果
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> delProduct(List<Integer> productIds, String userId) {
        if (productIds == null || productIds.isEmpty()) {
            log.warn("商品検索失敗: 検索語が空です。");
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, "検索語を入力してください。"));
        }
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "商品照会" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        log.info("商品検索開始, 検索語: {}");
        try {
            int result = 0;
            result += cm901060Mapper.delProduct(productIds);
            result += cm901060Mapper.delAttachMents(productIds);
            if (result == 0) {
                log.warn(CM901040MessageConstant.BANNER_DELETE_FAILED_NO_RESULT);
                return ResponseEntity
                        .ok(createResponseModel(0, false, CM901040MessageConstant.FAIL_BANNER_NOT_FOUND));
            }
            log.info(CM901040MessageConstant.BANNER_DELETE_SUCCESS, result);
            return ResponseEntity
                    .ok(createResponseModel(result, true, CM901040MessageConstant.SUCCESS_BANNER_DELETE));
        } catch (DataAccessException dae) {
            log.error(CM901040MessageConstant.BANNER_DELETE_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_DELETE_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * カテゴリグループコードの全件リストを取得
     *
     * @return カテゴリグループコードの全件リスト
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        try {
            List<Categories> categoryGroupCodeList = cm901060Mapper.findAllCategoryGroupCode();
            log.info("取得したカテゴリグループ数: {}", categoryGroupCodeList == null ? 0 : categoryGroupCodeList.size());

            return ResponseEntity.ok(createResponseModel(categoryGroupCodeList, true, "カテゴリグループの取得に成功しました"));
        } catch (DataAccessException dae) {
            log.error("DBアクセス中にエラーが発生しました: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "データベースエラーが発生しました。"));

        } catch (NullPointerException npe) {
            log.error("NullPointerExceptionが発生しました: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "必須データが不足しています。"));

        } catch (Exception e) {
            log.error("予期しないエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "サーバー内部エラーが発生しました。"));
        }
    }

    /**
     * 選択したカテゴリグループの下位カテゴリ情報を取得
     *
     * @param categoryGroupCode カテゴリグループコード
     * @return 下位カテゴリ情報
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode) {
        try {
            List<CategoryDetails> CategoryDetailList = cm901060Mapper.findCategory(categoryGroupCode);
            log.info("取得した下位カテゴリ情報数: {}", CategoryDetailList == null ? 0 : CategoryDetailList.size());

            return ResponseEntity.ok(createResponseModel(CategoryDetailList, true, "下位カテゴリ情報の取得に成功しました"));
        } catch (DataAccessException dae) {
            log.error("DBアクセス中にエラーが発生しました: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "データベースエラーが発生しました。"));

        } catch (NullPointerException npe) {
            log.error("NullPointerExceptionが発生しました: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "必須データが不足しています。"));

        } catch (Exception e) {
            log.error("予期しないエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "サーバー内部エラーが発生しました。"));
        }
    }

    /**
     * 登録可能な在庫リストを取得
     *
     * @return 在庫リスト
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<Stock>>> findAllStockCode() {
        try {
            List<Stock> stockCodeList = cm901060Mapper.findAllStockCode();
            log.info("取得した在庫コード数: {}", stockCodeList == null ? 0 : stockCodeList.size());

            return ResponseEntity.ok(createResponseModel(stockCodeList, true, "在庫コードの取得に成功しました"));
        } catch (DataAccessException dae) {
            log.error("DBアクセス中にエラーが発生しました: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "データベースエラーが発生しました。"));

        } catch (NullPointerException npe) {
            log.error("NullPointerExceptionが発生しました: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "必須データが不足しています。"));

        } catch (Exception e) {
            log.error("予期しないエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "サーバー内部エラーが発生しました。"));
        }
    }

    /**
     * 特定商品の取得
     *
     * @param productId 商品ID
     * @return 特定商品情報
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<ProductUpt>> findByProductCode(int productId) {
        try {
            log.info("商品検索開始, 商品コード: {}", productId);
            ProductUpt responseProduct = cm901060Mapper.findByUpdProductById(productId);
            List<Attachments> apAttachments = cm901060Mapper.editorGet(Long.valueOf(productId));
            List<String> detailData = new ArrayList<>();
            for (Attachments data : apAttachments) {
                detailData.add(data.getFilePath());
            }
            responseProduct.setDetailImages(detailData);
            return ResponseEntity.ok(createResponseModel(responseProduct, true, "特定商品の取得に成功しました"));
        } catch (DataAccessException dae) {
            log.error("DBアクセス中にエラーが発生しました: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, "データベースエラーが発生しました。"));

        } catch (NullPointerException npe) {
            log.error("NullPointerExceptionが発生しました: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(null, false, "必須データが不足しています。"));

        } catch (Exception e) {
            log.error("予期しないエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, "サーバー内部エラーが発生しました。"));
        }
    }

    /**
     * 商品登録
     *
     * @param entity 登録する商品情報
     * @return 登録結果
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> productUpload(@ModelAttribute RequestModel entity, String userId) {
        log.info(CM901040MessageConstant.BANNER_UPLOAD_START, entity != null ? entity.getProductCode() : "null");
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "商品照会" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        try {
            if (entity == null) {
                return ResponseEntity.ok(createResponseModel(null, false, "登録する商品情報が存在しません。"));
            }

            int result = cm901060Mapper.findByProductByCode(entity.getProductCode());

            if (result > 0) {
                return ResponseEntity.ok(createResponseModel(null, false, "既に登録されている商品コードです。"));
            }
            if (!entity.getExpectedDeliveryDate().isEmpty()
                    && !dateCalculator.isFutureMonth(entity.getExpectedDeliveryDate())) {
                return ResponseEntity.ok(createResponseModel(null, false, "出荷月を過去の月に設定することはできません。"));
            }
            if (!entity.getReservationDeadline().isEmpty()
                    && !dateCalculator.isFutureDate(entity.getReservationDeadline())) {
                return ResponseEntity.ok(createResponseModel(null, false, "予約日を過去の日付に設定することはできません。"));
            }

            String productFolder = null;
            productFolder = determineProductFolder(entity);

            // サムネイル保存
            String savedThumbnailName = null;
            if (null != entity.getProductFile() && !entity.getProductFile().isEmpty()) {
                savedThumbnailName = saveFile(entity.getProductFile(), "product/".concat(productFolder));
                if (savedThumbnailName == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(0, false, "情報がありません"));
                }
                log.info("サムネイル保存完了", savedThumbnailName);
            }

            List<String> savedDetailNames = new ArrayList<>();

            if (null != entity.getDetailImages()) {
                for (MultipartFile data : entity.getDetailImages()) {
                    String savedDetailName = saveFile(data, "product/".concat(productFolder));
                    if (savedDetailName == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseModel(0, false, "情報がありません"));
                    }
                    savedDetailNames.add("product/".concat(productFolder).concat("/").concat(savedDetailName));
                    log.debug("詳細画像保存完了", savedDetailName);
                }
            }

            // エディタHTML内容での画像処理
            String processedProductNote = null;
            List<String> movedImagePaths = new ArrayList<>();

            if (null != entity.getProductnote() && !entity.getProductnote().isEmpty()) {

                System.out.println("元のProductNote: " + entity.getProductnote());

                // HTMLから一時画像パスを抽出して処理
                processedProductNote = processEditorImages(entity.getProductnote(), productFolder, movedImagePaths);

                // 処理後のHTML内容をentityへ再設定
                entity.setProductnote(processedProductNote);

                System.out.println("処理後のProductNote: " + processedProductNote);
                log.debug("エディタ内容の処理完了。移動した画像数: {}", movedImagePaths.size());

            }

            long productId = cm901060Mapper.getNextProductId();
            insertImages(productId, savedDetailNames, false, userId);
            insertImages(productId, movedImagePaths, true, userId);

            insertProduct(productId, entity, userId);

            insertProductDetail(productId, productFolder, savedThumbnailName, entity.getProductnote(), userId);

            return ResponseEntity.ok(createResponseModel(1, true, "商品登録成功"));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_UPLOAD_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }

    }

    /**
     * 商品更新
     *
     * @param entity 更新する商品情報
     * @return 更新結果
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> productUpdate(@ModelAttribute RequestModel entity, String userId) {
        log.info(CM901040MessageConstant.BANNER_UPLOAD_START, entity != null ? entity.getProductCode() : "null");
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "商品照会" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        try {
            if (entity == null) {
                return ResponseEntity.ok(createResponseModel(null, false, "登録する商品情報が存在しません。"));
            }

            if (!entity.getExpectedDeliveryDate().isEmpty()
                    && !dateCalculator.isFutureMonth(entity.getExpectedDeliveryDate())) {
                return ResponseEntity.ok(createResponseModel(null, false, "出荷月を過去の月に設定することはできません。"));
            }
            if (!entity.getReservationDeadline().isEmpty()
                    && !dateCalculator.isFutureDate(entity.getReservationDeadline())) {
                return ResponseEntity.ok(createResponseModel(null, false, "予約日を過去の日付に設定することはできません。"));
            }

            String productFolder = null;
            productFolder = determineProductFolder(entity);

            // サムネイル保存
            String savedThumbnailName = null;
            if (entity.getProductFile() != null && !entity.getProductFile().isEmpty()) {
                savedThumbnailName = saveFile(entity.getProductFile(), "product/".concat(productFolder));
                if (savedThumbnailName == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(0, false, "情報がありません"));
                }
                log.debug("サムネイル保存完了", savedThumbnailName);
            }

            List<String> savedDetailNames = new ArrayList<>();

            if (entity.getDetailImages() != null) {
                for (MultipartFile data : entity.getDetailImages()) {
                    String savedDetailName = saveFile(data, "product/".concat(productFolder));
                    if (savedDetailName == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseModel(0, false, "情報がありません"));
                    }
                    savedDetailNames.add("product/".concat(productFolder).concat("/").concat(savedDetailName));
                    log.debug("詳細画像保存完了", savedDetailName);
                }
            }
            // エディタHTML内容での画像処理
            String processedProductNote = null;
            List<String> movedImagePaths = new ArrayList<>();

            if (entity.getProductnote() != null && !entity.getProductnote().isEmpty()) {

                System.out.println("元のProductNote: " + entity.getProductnote());

                // HTMLから一時画像パスを抽出して処理
                processedProductNote = processEditorImages(entity.getProductnote(), productFolder, movedImagePaths);

                // 処理後のHTML内容をentityへ再設定
                entity.setProductnote(processedProductNote);

                System.out.println("処理後のProductNote: " + processedProductNote);
                log.debug("エディタ内容の処理完了。移動した画像数: {}", movedImagePaths.size());

            }

            long productId = entity.getProductId();
            if (savedDetailNames != null)
                insertImages(productId, savedDetailNames, false, userId);

            if (movedImagePaths != null)
                insertImages(productId, movedImagePaths, true, userId);

            if (entity.getDeletedDetailImages() != null)
                updateImages(productId, entity.getDeletedDetailImages(), userId);

            updateProduct(productId, entity, userId);

            updateProductDetail(productId, productFolder, savedThumbnailName,
                    entity.getProductnote(), userId);

            return ResponseEntity.ok(createResponseModel(1, true, "商品更新成功"));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_UPLOAD_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }

    }

    /**
     * 画像アップロード
     *
     * @param file アップロードする画像ファイル
     * @return アップロード結果
     */
    public ResponseEntity<ResponseModel<String>> imageUpload(MultipartFile file) {
        // 一時ファイル保存
        String savedFileName = "/tmp/";
        try {
            if (!file.isEmpty()) {
                savedFileName = savedFileName.concat(saveFile(file, "tmp"));
            }
        } catch (Exception e) {
            log.error("予期しないエラーが発生しました: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, "サーバー内部エラーが発生しました。"));
        }
        return ResponseEntity
                .ok(createResponseModel(savedFileName, true, CM901040MessageConstant.SUCCESS_BANNER_UPDATE));
    }

    /**
     * ファイル保存
     *
     * @param file ファイル情報
     * @param path 保存先パス
     * @return 保存されたファイル名
     * @throws IOException       ファイル保存中にエラーが発生した場合
     * @throws SecurityException セキュリティ検証に失敗した場合
     */
    private String saveFile(MultipartFile file, String path) throws IOException, SecurityException {
        // 追加のセキュリティ検証
        if (!isValidImageFile(file)) {
            throw new SecurityException(CM901040MessageConstant.FAIL_INVALID_FILE_TYPE);
        }

        if ("s3".equals(uploadType) && s3Service != null) {
            try {
                String tmpUrl = "images/".concat(path);
                String fileUrl = s3Service.uploadFile(file, tmpUrl);
                log.debug("S3アップロード完了: {}", fileUrl);
                String fileName = Paths.get(fileUrl).getFileName().toString();
                return fileName;
            } catch (IOException e) {
                log.error("S3アップロード失敗", e);
                throw new IOException(CMMessageConstant.ERROR_FILE_SAVE_FAILED, e);
            }
        }

        Path uploadPath = Paths.get(uploadDir, path);
        log.debug(CM901040MessageConstant.FILE_UPLOAD_PATH_DEBUG, uploadPath.toString());

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.debug(CM901040MessageConstant.FILE_UPLOAD_DIR_CREATED, uploadPath.toString());
            }
        } catch (IOException e) {
            log.error(CM901040MessageConstant.FILE_UPLOAD_DIR_CREATE_FAILED, uploadPath.toString(), e);
            throw new IOException(CMMessageConstant.ERROR_FILE_UPLOAD_DIR_CREATE, e);
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }

        // セキュリティのためSecureRandomを使用
        String timeStamp = dateCalculator.tokyoTime().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = generateSecureRandomString(8);
        String savedFileName = String.format("%s_%s%s", timeStamp, uuid, fileExtension);

        Path filePath = uploadPath.resolve(savedFileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug(CM901040MessageConstant.FILE_SAVE_COMPLETE, savedFileName);
            return savedFileName;
        } catch (IOException e) {
            log.error(CM901040MessageConstant.FILE_SAVE_FAILED, savedFileName, e);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException deleteException) {
                log.warn(CM901040MessageConstant.FILE_DELETE_FAILED_CLEANUP, filePath, deleteException);
            }
            throw new IOException(CMMessageConstant.ERROR_FILE_SAVE_FAILED, e);
        }
    }

    /**
     * セキュリティのためのランダム文字列生成
     *
     * @param length 生成する文字列の長さ
     * @return ランダム文字列
     */
    private String generateSecureRandomString(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        String chars = uuidDir;

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    /**
     * 画像ファイル形式の検証
     *
     * @param file ファイル
     * @return 有効な画像ファイルかどうか
     */
    private boolean isValidImageFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return false;
        }

        String lowerCaseFileName = originalFileName.toLowerCase();
        for (String extension : CM901040Constant.ALLOWED_EXTENSIONS) {
            if (lowerCaseFileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 商品フォルダ名の決定
     *
     * @param entity 商品情報
     * @return フォルダ名
     */
    private String determineProductFolder(RequestModel entity) {
        // 商品コードを使用
        if (entity.getProductCode() != null && !entity.getProductCode().isEmpty()) {
            return entity.getProductCode();
        }

        // デフォルト: 現在日付（年月）
        return dateCalculator.tokyoTime().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    /**
     * エディタHTML内の一時画像を処理
     *
     * @param htmlContent     元のHTML内容
     * @param productFolder   商品フォルダ名
     * @param movedImagePaths 移動された画像パス一覧（参照渡し）
     * @return 処理後のHTML内容
     */
    private String processEditorImages(String htmlContent, String productFolder, List<String> movedImagePaths) {
        // imgタグ内のtmpパスを検出する正規表現
        Pattern pattern = Pattern.compile("<img[^>]+src=[\"']([^\"']*tmp/[^\"']*)[\"'][^>]*>",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlContent);

        StringBuffer processedContent = new StringBuffer();

        while (matcher.find()) {
            String fullImgTag = matcher.group(0); // 전체 img 태그
            String tmpImageUrl = matcher.group(1); // src 속성값

            log.debug("検出した一時画像: {}", tmpImageUrl);

            // URLからファイル名を抽出
            String fileName = extractFileNameFromUrl(tmpImageUrl);
            if ("s3".equals(uploadType) && s3Service != null && tmpImageUrl.contains(baseUrl)) {
                // S3ファイルを移動
                String newImageUrl = s3Service.moveFile(tmpImageUrl, "images/product/" + productFolder);

                if (newImageUrl != null) {
                    // imgタグのsrc属性を置換
                    String newImgTag = fullImgTag.replace(tmpImageUrl, newImageUrl);
                    matcher.appendReplacement(processedContent, Matcher.quoteReplacement(newImgTag));

                    movedImagePaths.add(newImageUrl);
                    log.debug("S3画像移動完了: {} → {}", tmpImageUrl, newImageUrl);
                } else {
                    log.warn("S3画像移動失敗: {}", tmpImageUrl);
                    matcher.appendReplacement(processedContent, Matcher.quoteReplacement(fullImgTag));
                }
            } else {
                if (fileName != null) {
                    // tmpファイルをproductフォルダに移動します
                    String newImagePath = moveTmpImageToProduct("/tmp/" + fileName, productFolder);

                    if (newImagePath != null) {
                        // 新しいURLを生成（既存ドメイン部分は維持）
                        String newImageUrl = tmpImageUrl.replace("/tmp/", "/product/" + productFolder + "/");

                        // imgタグのsrc属性を置換
                        String newImgTag = fullImgTag.replace(tmpImageUrl, newImageUrl);

                        // 置換した内容で反映
                        matcher.appendReplacement(processedContent, Matcher.quoteReplacement(newImgTag));

                        movedImagePaths.add(newImagePath);
                        log.debug("画像処理完了: {} → {}", tmpImageUrl, newImageUrl);
                    } else {
                        log.warn("画像移動失敗: {}", fileName);
                        // 移動失敗時は元のまま
                        matcher.appendReplacement(processedContent, Matcher.quoteReplacement(fullImgTag));
                    }
                } else {
                    log.warn("ファイル名の抽出に失敗しました: {}", tmpImageUrl);
                    matcher.appendReplacement(processedContent, Matcher.quoteReplacement(fullImgTag));
                }
            }
        }

        matcher.appendTail(processedContent);
        return processedContent.toString();
    }

    /**
     * tmp画像を product/{動的フォルダ} 配下へ移動
     *
     * @param tmpImagePath  一時画像パス
     * @param productFolder 商品フォルダ名
     * @return 移動後の画像パス（失敗時はnull）
     */
    private String moveTmpImageToProduct(String tmpImagePath, String productFolder) {
        try {
            if (tmpImagePath == null || tmpImagePath.trim().isEmpty()) {
                return null;
            }

            String fileName = tmpImagePath.startsWith("/tmp/") ? tmpImagePath.substring("/tmp/".length())
                    : tmpImagePath;

            Path sourcePath = Paths.get(uploadDir, "tmp", fileName);

            Path targetDir = Paths.get(uploadDir, "product", productFolder);
            Path targetPath = targetDir.resolve(fileName);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
                log.debug("ディレクトリ作成: {}", targetDir);
            }

            if (Files.exists(sourcePath)) {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(sourcePath);

                log.debug("ファイル移動完了: {} -> {}", sourcePath, targetPath);
                return "product/" + productFolder + "/" + fileName;
            } else {
                log.warn("ソースファイルが存在しません: {}", sourcePath);
                return null;
            }
        } catch (IOException e) {
            log.error("ファイル移動中にエラーが発生しました: {}", tmpImagePath, e);
            return null;
        }
    }

    /**
     * URLからファイル名を抽出
     *
     * @param imageUrl 画像URL
     * @return ファイル名（失敗時はnull）
     */
    private String extractFileNameFromUrl(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return null;
            }

            int tmpIndex = imageUrl.indexOf("/tmp/");
            if (tmpIndex != -1) {
                return imageUrl.substring(tmpIndex + 5);
            }

            int lastSlashIndex = imageUrl.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < imageUrl.length() - 1) {
                return imageUrl.substring(lastSlashIndex + 1);
            }

            return null;
        } catch (Exception e) {
            log.warn("ファイル名抽出中にエラーが発生しました: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * 商品詳細登録
     *
     * @param productId          商品ID
     * @param productFolder      サムネイル保存フォルダ
     * @param savedThumbnailName サムネイルファイル名
     * @param description        商品詳細内容
     * @return 登録結果
     */
    private int insertProductDetail(long productId, String productFolder, String savedThumbnailName,
            String description, String userId) {
        ProductDetails productDetails = new ProductDetails();
        productDetails.setProductId(productId);
        productDetails.setThumbnailUrl("product/" + productFolder + "/" + savedThumbnailName);
        productDetails.setDescription(description);
        productDetails.setCreatedBy(userId);
        productDetails.setCreatedAt(dateCalculator.tokyoTime());
        productDetails.setUpdatedBy(userId);
        productDetails.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901060Mapper.insertProductDetail(productDetails);
    }

    /**
     * 商品詳細更新
     *
     * @param productId          商品ID
     * @param productFolder      サムネイル保存フォルダ
     * @param savedThumbnailName サムネイルファイル名
     * @param description        商品詳細内容
     * @return 更新結果
     */
    private int updateProductDetail(long productId, String productFolder, String savedThumbnailName,
            String description, String userId) {
        ProductDetails productDetails = new ProductDetails();
        productDetails.setProductId(productId);
        if (savedThumbnailName != null)
            productDetails.setThumbnailUrl("product/" + productFolder + "/" + savedThumbnailName);
        productDetails.setDescription(description);
        productDetails.setUpdatedBy(userId);
        productDetails.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901060Mapper.updateProductDetail(productDetails);
    }

    /**
     * 商品登録
     *
     * @param productId 商品ID
     * @param entity    保存する商品情報
     * @return 登録結果
     */
    private int insertProduct(long productId, @ModelAttribute RequestModel entity, String userId) {
        Products products = new Products();

        products.setProductId(productId);
        products.setProductCode(entity.getProductCode());
        products.setName(entity.getProductName());
        products.setPrice(entity.getProductPrice());
        products.setDeliveryPrice(entity.getShippingFee());
        products.setReleaseDate(entity.getExpectedDeliveryDate());
        products.setReservationDeadline(entity.getReservationDeadline());
        if (entity.getReservationDeadline().isEmpty()) {
            products.setCodeValue(1);
        } else {
            products.setCodeValue(3);
        }
        products.setCategory(entity.getCategoryGroup());
        products.setPurchaseLimit(entity.getPurchaseLimit());
        products.setWeight(entity.getWeight());
        products.setCreatedBy(userId);
        products.setCreatedAt(dateCalculator.tokyoTime());
        products.setUpdatedBy(userId);
        products.setUpdatedAt(dateCalculator.tokyoTime());
        return cm901060Mapper.productInsert(products);
    }

    /**
     * 商品更新
     *
     * @param productId 商品ID
     * @param entity    更新する商品情報
     * @return 更新結果
     */
    private int updateProduct(long productId, @ModelAttribute RequestModel entity, String userId) {
        Products products = new Products();

        products.setProductId(productId);
        products.setPrice(entity.getProductPrice());
        products.setDeliveryPrice(entity.getShippingFee());
        products.setReleaseDate(entity.getExpectedDeliveryDate());
        products.setReservationDeadline(entity.getReservationDeadline());
        if (entity.getReservationDeadline().isEmpty()) {
            products.setCodeValue(1);
        } else {
            products.setCodeValue(3);
        }
        products.setCategory(entity.getCategoryGroup());
        products.setPurchaseLimit(entity.getPurchaseLimit());
        products.setWeight(entity.getWeight());
        products.setUpdatedBy(userId);
        products.setUpdatedAt(dateCalculator.tokyoTime());
        return cm901060Mapper.productUpdate(products);
    }

    /**
     * エディタ画像の削除（DB更新）
     *
     * @param productId           商品ID
     * @param deletedDetailImages 削除対象のファイルパス
     * @return 更新結果
     */
    private int updateImages(long productId, List<String> deletedDetailImages, String userId) {
        int result = 0;
        for (String img : deletedDetailImages) {
            Attachments attachments = new Attachments();
            Path path = Paths.get(img);

            attachments.setBoardId(productId);
            attachments.setFileName(path.getFileName().toString());
            attachments.setUpdatedBy(userId);
            attachments.setUpdatedAt(dateCalculator.tokyoTime());

            result += cm901060Mapper.editorUpdate(attachments);
        }
        return result;
    }

    /**
     * エディタ画像パスの登録
     *
     * @param productId       商品ID
     * @param movedImagePaths 保存するファイルパス
     * @return 登録結果
     */
    private int insertImages(long productId, List<String> movedImagePaths, Boolean flag, String userId) {
        int result = 0;
        for (String move : movedImagePaths) {
            Attachments attachments = new Attachments();
            Path path = Paths.get(move);

            attachments.setBoardId(productId);
            attachments.setFileName(path.getFileName().toString());
            attachments.setContentType(getFileExtension(move));
            attachments.setFilePath(move);
            if (flag) {
                attachments.setCodeValue(2);
            } else {
                attachments.setCodeValue(3);
            }
            attachments.setCreatedBy(userId);
            attachments.setCreatedAt(dateCalculator.tokyoTime());
            attachments.setUpdatedBy(userId);
            attachments.setUpdatedAt(dateCalculator.tokyoTime());

            result += cm901060Mapper.editorInsert(attachments);
        }
        return result;
    }

    /**
     * ファイルパスから拡張子を取得
     *
     * @param filePath ファイルパス
     * @return 拡張子（ドット除外）。存在しない場合は空文字
     */
    private String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        int lastDotIndex = filePath.lastIndexOf('.');
        int lastSlashIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        if (lastDotIndex > lastSlashIndex && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1).toLowerCase();
        }

        return "";
    }

    /**
     * ResponseModel生成
     *
     * @param resultList 結果データ
     * @param result     処理結果
     * @param message    メッセージ
     * @return ResponseModel
     */
    private <T> ResponseModel<T> createResponseModel(T resultList, boolean result, String message) {
        return ResponseModel.<T>builder()
                .resultList(resultList)
                .result(result)
                .message(Objects.requireNonNull(message, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED))
                .build();
    }
}
