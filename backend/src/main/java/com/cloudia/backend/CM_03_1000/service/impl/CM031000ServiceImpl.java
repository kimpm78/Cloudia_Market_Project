package com.cloudia.backend.CM_03_1000.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_03_1000.constants.CM031000MessageConstant;
import com.cloudia.backend.CM_03_1000.mapper.CM031000Mapper;
import com.cloudia.backend.CM_03_1000.model.CartRequest;
import com.cloudia.backend.CM_03_1000.model.Categories;
import com.cloudia.backend.CM_03_1000.model.CategoryDetails;
import com.cloudia.backend.CM_03_1000.model.CategoryGroupForCheckbox;
import com.cloudia.backend.CM_03_1000.model.CategoryItem;
import com.cloudia.backend.CM_03_1000.model.ProductInfo;
import com.cloudia.backend.CM_03_1000.service.CM031000Service;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.util.DateCalculator;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM031000ServiceImpl implements CM031000Service {

    private final CM031000Mapper cm031000Mapper;
    private final DateCalculator dateCalculator;

    @Value("${uuid.upload.dir}")
    private String uuidDir;

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * 新商品一覧取得
     *
     * @param categories カテゴリ一覧
     * @return 新商品一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductList(List<String> categories) {
        try {
            List<ProductInfo> list = Optional.ofNullable(cm031000Mapper.selectNewProductList(categories))
                .orElse(Collections.emptyList());
            applyProductStatus(list);
            return ResponseEntity.ok(setResponseDto(list, true, CM031000MessageConstant.SUCCESS_PRODUCT_FIND));
        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CMMessageConstant.ERROR_DATABASE));
        }
    }

    /**
     * 商品詳細取得
     *
     * @param productId 商品ID
     * @return 商品詳細情報
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ProductInfo>>> getProductDetail(Long productId) {
        try {
            List<ProductInfo> list = Optional.ofNullable(cm031000Mapper.selectProductDetail(productId))
                .map(product -> {
                    List<String> detailImages = cm031000Mapper.selectProductDetailImages(productId);
                    product.setDetailImages(detailImages);
                    return Collections.singletonList(product);
                })
                .orElse(Collections.emptyList());
            applyProductStatus(list);
            log.info(CM031000MessageConstant.PRODUCT_FIND_BY_ID_COMPLETE, productId, list.size());
            return ResponseEntity.ok(setResponseDto(list, true, CM031000MessageConstant.SUCCESS_PRODUCT_FIND));
        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(setResponseDto(null, false, CMMessageConstant.ERROR_DATABASE));
        }
    }

    /**
     * カート追加
     *
     * @param cartRequest カート追加リクエスト情報
     * @return 追加可否
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Void>> addToCart(CartRequest cartRequest) {
        try {
            String auditUser = resolveAuditUser(cartRequest.getUserId());

            // 商品情報を取得
            ProductInfo product = cm031000Mapper
                    .selectProductDetail(Long.valueOf(cartRequest.getProductId()));

            if (product == null) {
                return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, CM031000MessageConstant.FAIL_CART_ADD));
            }

            int status = product.getCodeValue();

            // 商品ステータス検証（003基準）
            // 2: 品切れ、4: 予約締切 → カート追加不可
            if (status == 2 || status == 4) {
                return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, CM031000MessageConstant.FAIL_CART_ADD));
            }

            // カート処理
            if (cm031000Mapper.findCartItem(
                    cartRequest.getUserId(), cartRequest.getProductId()) != null) {

                cm031000Mapper.updateQuantity(
                        cartRequest.getUserId(),
                        cartRequest.getProductId(),
                        cartRequest.getQuantity(),
                        auditUser);
            } else {
                cm031000Mapper.insertCartItem(
                        cartRequest.getUserId(),
                        cartRequest.getProductId(),
                        cartRequest.getQuantity(),
                        auditUser,
                        auditUser);
            }

            return ResponseEntity.ok(
                setResponseDto(null, true, CM031000MessageConstant.SUCCESS_CART_ADD));

        } catch (Exception e) {
            log.error(CMMessageConstant.ERROR_DATABASE + " - {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(setResponseDto(null, false, CM031000MessageConstant.FAIL_CART_ADD));
        }
    }

    
    /**
     * 監査ユーザー名を解決
     *
     * @param userId ユーザーID
     * @return 監査ユーザー名
     */
    private String resolveAuditUser(Long userId) {
        final int MAX_LENGTH = 10;
        if (userId != null) {
            String asString = String.valueOf(userId);
            return asString.length() > MAX_LENGTH ? asString.substring(0, MAX_LENGTH) : asString;
        }
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                String name = auth.getName();
                if (name != null && !name.isBlank()) {
                    return name.length() > MAX_LENGTH ? name.substring(0, MAX_LENGTH) : name;
                }
            }
        } catch (Exception ignore) {
        }
        return "system";
    }

    /**
     * カテゴリーグループコード全件取得
     *
     * @return カテゴリーグループコード一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        try {
            List<Categories> categoryGroupCodeList = cm031000Mapper.findAllCategoryGroupCode();
            log.info(CM031000MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS, categoryGroupCodeList == null ? 0 : categoryGroupCodeList.size());

            return ResponseEntity.ok(createResponseModel(categoryGroupCodeList, true, CM031000MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM031000MessageConstant.CATEGORY_GROUP_FETCH_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (NullPointerException npe) {
            log.error(CM031000MessageConstant.CATEGORY_GROUP_FETCH_NULL, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED));
        } catch (Exception e) {
            log.error(CM031000MessageConstant.CATEGORY_GROUP_FETCH_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * カテゴリー詳細取得
     *
     * @param categoryGroupCode カテゴリーグループコード一覧
     * @return カテゴリー詳細一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode) {
        try {
            List<CategoryDetails> CategoryDetailList = cm031000Mapper.findCategory(categoryGroupCode);
            log.info(CM031000MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS, CategoryDetailList == null ? 0 : CategoryDetailList.size());

            return ResponseEntity.ok(createResponseModel(CategoryDetailList, true, CM031000MessageConstant.CATEGORY_DETAIL_FETCH_SUCCESS));
        } catch (DataAccessException dae) {
            log.error(CM031000MessageConstant.CATEGORY_DETAIL_FETCH_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (NullPointerException npe) {
            log.error(CM031000MessageConstant.CATEGORY_DETAIL_FETCH_NULL, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.MESSAGE_NULL_NOT_ALLOWED));
        } catch (Exception e) {
            log.error(CM031000MessageConstant.CATEGORY_DETAIL_FETCH_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * チェックボックス用のカテゴリーグループ取得
     *
     * @return チェックボックス用カテゴリーグループ一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<CategoryGroupForCheckbox>>> findAllCategoryGroupForCheckbox() {
        try {
            List<Categories> rawGroups = findAllCategoryGroupCode().getBody().getResultList();

            List<CategoryGroupForCheckbox> result = rawGroups.stream().map(group ->
                CategoryGroupForCheckbox.builder()
                    .groupCode(group.getCategoryGroupCode())
                    .groupName(group.getCategoryGroupName())
                    .categories(
                        group.getDetails().stream().map(detail ->
                            CategoryItem.builder()
                                .code(detail.getCategoryCode())
                                .name(detail.getCategoryName())
                                .build()
                        ).toList()
                    )
                    .build()
            ).toList();

            return ResponseEntity.ok(createResponseModel(result, true, CM031000MessageConstant.CATEGORY_GROUP_FETCH_SUCCESS));
        } catch (Exception e) {
            log.error(CM031000MessageConstant.CATEGORY_GROUP_FETCH_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    private <T> ResponseModel<T> setResponseDto(T resultList, boolean ret, String msg) {
        return ResponseModel.<T>builder()
            .resultList(resultList)
            .result(ret)
            .message(msg)
            .build();
    }

    private <T> ResponseModel<T> createResponseModel(T data, boolean result, String message) {
        return ResponseModel.<T>builder()
                .resultList(data)
                .result(result)
                .message(message)
                .build();
    }

    /**
     * 商品ステータス適用
     *
     * @param products 商品一覧
     * @return void
     */
    private void applyProductStatus(List<ProductInfo> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        for (ProductInfo product : products) {
            if (product == null) {
                continue;
            }

            int status = product.getCodeValue();

            // 通常販売商品は 1=販売中 の場合のみ配送予定日を算出
            if (status == 1) {
                product.setEstimatedDeliveryDate(
                    dateCalculator.convertToYYMMDD(
                        dateCalculator.tokyoTime(),
                        5
                    )
                );
            }
        }
    }

    /**
     * 画像登録
     *
     * @param file 登録する画像ファイル
     * @return 登録可否
     */
    public ResponseEntity<ResponseModel<String>> imageUpload(MultipartFile file) {
        // 一時ファイルとして保存
        String savedFileName = "/tmp/";
        try {
            if (!file.isEmpty()) {
                savedFileName = savedFileName.concat(saveFile(file, "tmp"));
            }
        } catch (Exception e) {
            return null;
        }
        return ResponseEntity
                .ok(createResponseModel(savedFileName, true, CM031000MessageConstant.SUCCESS_BANNER_UPDATE));
    }

    private String saveFile(MultipartFile file, String path) throws IOException, SecurityException {
        if (!isValidImageFile(file)) {
            throw new SecurityException("Invalid image file type.");
        }

        Path uploadPath = Paths.get(uploadDir, path);
        log.debug(CM031000MessageConstant.FILE_UPLOAD_PATH_DEBUG, uploadPath.toString());

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.debug(CM031000MessageConstant.FILE_UPLOAD_DIR_CREATED, uploadPath.toString());
            }
        } catch (IOException e) {
            log.error(CM031000MessageConstant.FILE_UPLOAD_DIR_CREATE_FAILED, uploadPath.toString(), e);
            throw new IOException(CMMessageConstant.ERROR_FILE_UPLOAD_DIR_CREATE, e);
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        }

        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String savedFileName = String.format("%s_%s%s", timeStamp, uuid, fileExtension);

        Path filePath = uploadPath.resolve(savedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return savedFileName;
    }
    

    private boolean isValidImageFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return false;
        }
        String lowerCaseFileName = originalFileName.toLowerCase();
        return lowerCaseFileName.endsWith(".png") || lowerCaseFileName.endsWith(".jpg") ||
            lowerCaseFileName.endsWith(".jpeg") || lowerCaseFileName.endsWith(".gif");
    }
}
