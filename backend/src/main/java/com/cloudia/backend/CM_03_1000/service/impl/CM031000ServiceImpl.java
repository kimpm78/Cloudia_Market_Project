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
     * 신상품 목록 조회
     * 
     * @param categories 카테고리 리스트
     * @return 신상품 목록
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
     * 상품 상세 조회
     * 
     * @param productId 상품 ID
     * @return 상품 상세 정보
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
     * 장바구니 추가
     * 
     * @param cartRequest 장바구니 요청 정보
     * @return 추가 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Void>> addToCart(CartRequest cartRequest) {
        try {
            String auditUser = resolveAuditUser(cartRequest.getUserId());

            // 상품 정보 조회
            ProductInfo product = cm031000Mapper
                    .selectProductDetail(Long.valueOf(cartRequest.getProductId()));

            if (product == null) {
                return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, CM031000MessageConstant.FAIL_CART_ADD));
            }

            int status = product.getCodeValue();

            // 상품 상태 검증 (003 기준)
            // 2: 품절, 4: 예약 마감 → 장바구니 불가
            if (status == 2 || status == 4) {
                return ResponseEntity.badRequest()
                    .body(setResponseDto(null, false, CM031000MessageConstant.FAIL_CART_ADD));
            }

            // 장바구니 처리
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
     * 감사 사용자명 해석
     * 
     * @param userId
     * @return
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
     * 전체 카테고리 그룹 코드 조회
     * 
     * @return 카테고리 그룹 코드 리스트
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
     * 카테고리 상세 조회
     * 
     * @param categoryGroupCode 카테고리 그룹 코드 리스트
     * @return 카테고리 상세 리스트
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
     * 카테고리 그룹 체크박스용 조회
     * 
     * @return 카테고리 그룹 체크박스용 리스트
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
     * 상품 상태 적용
     * 
     * @param products 상품 리스트
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

            // 상시판매 상품 1=판매중만 배송 예정일 계산
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
     * 이미지 등록
     * 
     * @param file 등록 할 이미지 정보
     * @return 등록 여부
     */
    public ResponseEntity<ResponseModel<String>> imageUpload(MultipartFile file) {
        // 임시 파일 저장
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
