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
import com.cloudia.backend.CM_90_1060.model.ResponseModel;
import com.cloudia.backend.CM_90_1060.model.ResponseProducts;
import com.cloudia.backend.CM_90_1060.model.Stock;
import com.cloudia.backend.CM_90_1060.service.CM901060Service;
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
            log.info("조회된 카테고리 그룹 수: {}", responseProducts == null ? 0 : responseProducts.size());

            return ResponseEntity.ok(createResponseModel(responseProducts, true, "카테고리 그룹 조회 성공"));
        } catch (DataAccessException dae) {
            // DB 관련 예외
            log.error("DB 접근 중 오류 발생: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "데이터베이스 오류가 발생했습니다."));

        } catch (NullPointerException npe) {
            // Null 처리 예외
            log.error("NullPointerException 발생: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "필수 데이터가 누락되었습니다."));

        } catch (Exception e) {
            // 그 외 일반 예외
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 상품 리스트 조회
     * 
     * @param searchTerm 키워드
     * @param searchType 타입 (1:상품 코드, 2:상품 명)
     * @return 상품 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<ResponseProducts>>> getFindProduct(String searchTerm, int searchType) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.warn("상품 검색 실패: 검색어가 비어있습니다.");
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyList(), false, "검색어를 입력해주세요."));
        }

        String trimmedSearchTerm = searchTerm.trim();
        log.info("상품 검색 시작, 검색어: {}", trimmedSearchTerm);

        try {
            List<ResponseProducts> responseProducts = cm901060Mapper.findByProduct(trimmedSearchTerm, searchType);
            if (responseProducts == null) {
                responseProducts = Collections.emptyList();
            }

            return ResponseEntity.ok(createResponseModel(responseProducts, true, "카테고리 그룹 조회 성공"));
        } catch (DataAccessException dae) {
            // DB 관련 예외
            log.error("DB 접근 중 오류 발생: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "데이터베이스 오류가 발생했습니다."));

        } catch (NullPointerException npe) {
            // Null 처리 예외
            log.error("NullPointerException 발생: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "필수 데이터가 누락되었습니다."));

        } catch (Exception e) {
            // 그 외 일반 예외
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 상품 삭제
     * 
     * @param productIds 삭제 아이디 리스트
     * @return 삭제 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> delProduct(List<Integer> productIds, String userId) {
        if (productIds == null || productIds.isEmpty()) {
            log.warn("상품 검색 실패: 검색어가 비어있습니다.");
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, "검색어를 입력해주세요."));
        }
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "상품 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        log.info("상품 검색 시작, 검색어: {}");
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
     * 카테고리 그룹 코드 전체 리스트 조회
     * 
     * @return 카테고리 그룹 코드 전체 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<Categories>>> findAllCategoryGroupCode() {
        try {
            List<Categories> categoryGroupCodeList = cm901060Mapper.findAllCategoryGroupCode();
            log.info("조회된 카테고리 그룹 수: {}", categoryGroupCodeList == null ? 0 : categoryGroupCodeList.size());

            return ResponseEntity.ok(createResponseModel(categoryGroupCodeList, true, "카테고리 그룹 조회 성공"));
        } catch (DataAccessException dae) {
            // DB 관련 예외
            log.error("DB 접근 중 오류 발생: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "데이터베이스 오류가 발생했습니다."));

        } catch (NullPointerException npe) {
            // Null 처리 예외
            log.error("NullPointerException 발생: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "필수 데이터가 누락되었습니다."));

        } catch (Exception e) {
            // 그 외 일반 예외
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 선택 된 카테고리 그룹의 하위 카테고리 정보 조회
     * 
     * @param categoryGroupCode 카테고리 그룹 코드드
     * @return 하위 카테고리 정보
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<CategoryDetails>>> findCategory(List<String> categoryGroupCode) {
        try {
            List<CategoryDetails> CategoryDetailList = cm901060Mapper.findCategory(categoryGroupCode);
            log.info("조회된 하위 카테고리 정보 수: {}", CategoryDetailList == null ? 0 : CategoryDetailList.size());

            return ResponseEntity.ok(createResponseModel(CategoryDetailList, true, "하위 카테고리 정보 조회 성공"));
        } catch (DataAccessException dae) {
            // DB 관련 예외
            log.error("DB 접근 중 오류 발생: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "데이터베이스 오류가 발생했습니다."));

        } catch (NullPointerException npe) {
            // Null 처리 예외
            log.error("NullPointerException 발생: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "필수 데이터가 누락되었습니다."));

        } catch (Exception e) {
            // 그 외 일반 예외
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 등록 가능한 재고 리스트 조회
     * 
     * @return 재고 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<Stock>>> findAllStockCode() {
        try {
            List<Stock> stockCodeList = cm901060Mapper.findAllStockCode();
            log.info("조회된 재고 코드 수: {}", stockCodeList == null ? 0 : stockCodeList.size());

            return ResponseEntity.ok(createResponseModel(stockCodeList, true, "재고 코드 조회 성공"));
        } catch (DataAccessException dae) {
            // DB 관련 예외
            log.error("DB 접근 중 오류 발생: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "데이터베이스 오류가 발생했습니다."));

        } catch (NullPointerException npe) {
            // Null 처리 예외
            log.error("NullPointerException 발생: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(Collections.emptyList(), false, "필수 데이터가 누락되었습니다."));

        } catch (Exception e) {
            // 그 외 일반 예외
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 상품 조회
     * 
     * @param productId 상품 코드
     * @return 특정 상품 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<ProductUpt>> findByProductCode(int productId) {
        try {
            log.info("상품 검색 시작, 상품코드: {}", productId);
            ProductUpt responseProduct = cm901060Mapper.findByUpdProductById(productId);
            List<Attachments> apAttachments = cm901060Mapper.editorGet(Long.valueOf(productId));
            List<String> detailData = new ArrayList<>();
            for (Attachments data : apAttachments) {
                detailData.add(data.getFilePath());
            }
            responseProduct.setDetailImages(detailData);
            return ResponseEntity.ok(createResponseModel(responseProduct, true, "특정 상품 조회 성공"));
        } catch (DataAccessException dae) {
            // DB 관련 예외
            log.error("DB 접근 중 오류 발생: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, "데이터베이스 오류가 발생했습니다."));

        } catch (NullPointerException npe) {
            // Null 처리 예외
            log.error("NullPointerException 발생: {}", npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(null, false, "필수 데이터가 누락되었습니다."));

        } catch (Exception e) {
            // 그 외 일반 예외
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, "서버 내부 오류가 발생했습니다."));
        }
    }

    /**
     * 상품 등록
     * 
     * @param entity 등록 할 상품 정보
     * @return 상품 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> productUpload(@ModelAttribute RequestModel entity, String userId) {
        log.info(CM901040MessageConstant.BANNER_UPLOAD_START, entity != null ? entity.getProductCode() : "null");
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "상품 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        try {
            if (entity == null) {
                return ResponseEntity.ok(createResponseModel(null, false, "등록할 상품 정보가 존재하지 않습니다."));
            }

            int result = cm901060Mapper.findByProductByCode(entity.getProductCode());

            if (result > 0) {
                return ResponseEntity.ok(createResponseModel(null, false, "이미 등록된 상품 코드입니다."));
            }
            if (!entity.getExpectedDeliveryDate().isEmpty()
                    && !dateCalculator.isFutureMonth(entity.getExpectedDeliveryDate())) {
                return ResponseEntity.ok(createResponseModel(null, false, "출고월은 과거 월로 할수없습니다."));
            }
            if (!entity.getReservationDeadline().isEmpty()
                    && !dateCalculator.isFutureDate(entity.getReservationDeadline())) {
                return ResponseEntity.ok(createResponseModel(null, false, "예약날짜는 과거 날짜로 할수없습니다."));
            }

            String productFolder = null;
            productFolder = determineProductFolder(entity);

            // 섬네일 저장
            String savedThumbnailName = null;
            if (null != entity.getProductFile() && !entity.getProductFile().isEmpty()) {
                savedThumbnailName = saveFile(entity.getProductFile(), "product/".concat(productFolder));
                if (savedThumbnailName == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(0, false, "정보가 없습니다"));
                }
                log.info("섬네일 저장 완료", savedThumbnailName);
            }

            List<String> savedDetailNames = new ArrayList<>();

            if (null != entity.getDetailImages()) {
                for (MultipartFile data : entity.getDetailImages()) {
                    String savedDetailName = saveFile(data, "product/".concat(productFolder));
                    if (savedDetailName == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseModel(0, false, "정보가 없습니다"));
                    }
                    savedDetailNames.add("product/".concat(productFolder).concat("/").concat(savedDetailName));
                    log.debug("상세이미지 저장 완료", savedDetailName);
                }
            }

            // 에디터 HTML 내용에서 이미지 처리
            String processedProductNote = null;
            List<String> movedImagePaths = new ArrayList<>();

            if (null != entity.getProductnote() && !entity.getProductnote().isEmpty()) {

                System.out.println("원본 ProductNote: " + entity.getProductnote());

                // HTML에서 임시 이미지 경로들 추출 및 처리
                processedProductNote = processEditorImages(entity.getProductnote(), productFolder, movedImagePaths);

                // 처리된 HTML 내용을 entity에 다시 설정
                entity.setProductnote(processedProductNote);

                System.out.println("처리된 ProductNote: " + processedProductNote);
                log.debug("에디터 내용 처리 완료. 이동된 이미지 수: {}", movedImagePaths.size());

            }

            long productId = cm901060Mapper.getNextProductId();
            insertImages(productId, savedDetailNames, false, userId);
            insertImages(productId, movedImagePaths, true, userId);

            insertProduct(productId, entity, userId);

            insertProductDetail(productId, productFolder, savedThumbnailName, entity.getProductnote(), userId);

            return ResponseEntity.ok(createResponseModel(1, true, "상품 등록 성공"));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_UPLOAD_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }

    }

    /**
     * 상품 수정
     * 
     * @param entity 수정 할 상품 정보
     * @return 상품 수정 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> productUpdate(@ModelAttribute RequestModel entity, String userId) {
        log.info(CM901040MessageConstant.BANNER_UPLOAD_START, entity != null ? entity.getProductCode() : "null");
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "상품 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        try {
            if (entity == null) {
                return ResponseEntity.ok(createResponseModel(null, false, "등록할 상품 정보가 존재하지 않습니다."));
            }

            if (!entity.getExpectedDeliveryDate().isEmpty()
                    && !dateCalculator.isFutureMonth(entity.getExpectedDeliveryDate())) {
                return ResponseEntity.ok(createResponseModel(null, false, "출고월은 과거 월로 할수없습니다."));
            }
            if (!entity.getReservationDeadline().isEmpty()
                    && !dateCalculator.isFutureDate(entity.getReservationDeadline())) {
                return ResponseEntity.ok(createResponseModel(null, false, "예약날짜는 과거 날짜로 할수없습니다."));
            }

            String productFolder = null;
            productFolder = determineProductFolder(entity);

            // 섬네일 저장
            String savedThumbnailName = null;
            if (entity.getProductFile() != null && !entity.getProductFile().isEmpty()) {
                savedThumbnailName = saveFile(entity.getProductFile(), "product/".concat(productFolder));
                if (savedThumbnailName == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(0, false, "정보가 없습니다"));
                }
                log.debug("섬네일 저장 완료", savedThumbnailName);
            }

            List<String> savedDetailNames = new ArrayList<>();

            if (entity.getDetailImages() != null) {
                for (MultipartFile data : entity.getDetailImages()) {
                    String savedDetailName = saveFile(data, "product/".concat(productFolder));
                    if (savedDetailName == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseModel(0, false, "정보가 없습니다"));
                    }
                    savedDetailNames.add("product/".concat(productFolder).concat("/").concat(savedDetailName));
                    log.debug("상세이미지 저장 완료", savedDetailName);
                }
            }
            // 에디터 HTML 내용에서 이미지 처리
            String processedProductNote = null;
            List<String> movedImagePaths = new ArrayList<>();

            if (entity.getProductnote() != null && !entity.getProductnote().isEmpty()) {

                System.out.println("원본 ProductNote: " + entity.getProductnote());

                // HTML에서 임시 이미지 경로들 추출 및 처리
                processedProductNote = processEditorImages(entity.getProductnote(), productFolder, movedImagePaths);

                // 처리된 HTML 내용을 entity에 다시 설정
                entity.setProductnote(processedProductNote);

                System.out.println("처리된 ProductNote: " + processedProductNote);
                log.debug("에디터 내용 처리 완료. 이동된 이미지 수: {}", movedImagePaths.size());

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

            return ResponseEntity.ok(createResponseModel(1, true, "상품 수정 성공"));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_UPLOAD_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
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
            log.error("예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, "서버 내부 오류가 발생했습니다."));
        }
        return ResponseEntity
                .ok(createResponseModel(savedFileName, true, CM901040MessageConstant.SUCCESS_BANNER_UPDATE));
    }

    /**
     * 파일 저장
     * 
     * @param file 파일 정보
     * @return 저장된 파일명
     * @throws IOException       파일 저장 중 오류 발생 시
     * @throws SecurityException 보안 검증 실패 시
     */
    private String saveFile(MultipartFile file, String path) throws IOException, SecurityException {
        // 추가 보안 검증
        if (!isValidImageFile(file)) {
            throw new SecurityException(CM901040MessageConstant.FAIL_INVALID_FILE_TYPE);
        }

        if ("s3".equals(uploadType) && s3Service != null) {
            try {
                String tmpUrl = "images/".concat(path);
                String fileUrl = s3Service.uploadFile(file, tmpUrl);
                log.debug("S3 업로드 완료: {}", fileUrl);
                String fileName = Paths.get(fileUrl).getFileName().toString();
                return fileName;
            } catch (IOException e) {
                log.error("S3 업로드 실패", e);
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

        // 보안을 위해 SecureRandom 사용
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
            // 저장 실패 시 생성된 파일 삭제 시도
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException deleteException) {
                log.warn(CM901040MessageConstant.FILE_DELETE_FAILED_CLEANUP, filePath, deleteException);
            }
            throw new IOException(CMMessageConstant.ERROR_FILE_SAVE_FAILED, e);
        }
    }

    /**
     * 보안을 위한 랜덤 문자열 생성
     * 
     * @param length 생성할 문자열 길이
     * @return 랜덤 문자열
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
     * 이미지 파일 형식 검증
     * 
     * @param file 파일
     * @return 유효한 이미지 파일 여부
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
     * 상품 폴더명 결정
     * 
     * @param entity 상품 정보
     * @return 폴더명
     */
    private String determineProductFolder(RequestModel entity) {
        // 상품 코드 사용
        if (entity.getProductCode() != null && !entity.getProductCode().isEmpty()) {
            return entity.getProductCode();
        }

        // 기본값: 현재 날짜 (년월)
        return dateCalculator.tokyoTime().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    /**
     * 에디터 HTML 내용에서 임시 이미지들을 처리
     * 
     * @param htmlContent     원본 HTML 내용
     * @param productFolder   상품 폴더명
     * @param movedImagePaths 이동된 이미지 경로 리스트 (참조로 전달)
     * @return 처리된 HTML 내용
     */
    private String processEditorImages(String htmlContent, String productFolder, List<String> movedImagePaths) {
        // img 태그에서 tmp 경로를 찾는 정규표현식
        Pattern pattern = Pattern.compile("<img[^>]+src=[\"']([^\"']*tmp/[^\"']*)[\"'][^>]*>",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlContent);

        StringBuffer processedContent = new StringBuffer();

        while (matcher.find()) {
            String fullImgTag = matcher.group(0); // 전체 img 태그
            String tmpImageUrl = matcher.group(1); // src 속성값

            log.debug("발견된 임시 이미지: {}", tmpImageUrl);

            // URL에서 파일명 추출
            String fileName = extractFileNameFromUrl(tmpImageUrl);
            if ("s3".equals(uploadType) && s3Service != null && tmpImageUrl.contains(baseUrl)) {
                // S3 파일 이동
                String newImageUrl = s3Service.moveFile(tmpImageUrl, "images/product/" + productFolder);

                if (newImageUrl != null) {
                    // img 태그의 src 속성 교체
                    String newImgTag = fullImgTag.replace(tmpImageUrl, newImageUrl);
                    matcher.appendReplacement(processedContent, Matcher.quoteReplacement(newImgTag));

                    movedImagePaths.add(newImageUrl);
                    log.debug("S3 이미지 이동 완료: {} → {}", tmpImageUrl, newImageUrl);
                } else {
                    log.warn("S3 이미지 이동 실패: {}", tmpImageUrl);
                    matcher.appendReplacement(processedContent, Matcher.quoteReplacement(fullImgTag));
                }
            } else {
                if (fileName != null) {
                    // tmp 파일을 product 폴더로 이동
                    String newImagePath = moveTmpImageToProduct("/tmp/" + fileName, productFolder);

                    if (newImagePath != null) {
                        // 새로운 URL 생성 (기존 도메인 부분 유지)
                        String newImageUrl = tmpImageUrl.replace("/tmp/", "/product/" + productFolder + "/");

                        // img 태그의 src 속성 교체
                        String newImgTag = fullImgTag.replace(tmpImageUrl, newImageUrl);

                        // 교체된 내용으로 변경
                        matcher.appendReplacement(processedContent, Matcher.quoteReplacement(newImgTag));

                        movedImagePaths.add(newImagePath);
                        log.debug("이미지 처리 완료: {} → {}", tmpImageUrl, newImageUrl);
                    } else {
                        log.warn("이미지 이동 실패: {}", fileName);
                        // 이동 실패시 원본 유지
                        matcher.appendReplacement(processedContent, Matcher.quoteReplacement(fullImgTag));
                    }
                } else {
                    log.warn("파일명 추출 실패: {}", tmpImageUrl);
                    matcher.appendReplacement(processedContent, Matcher.quoteReplacement(fullImgTag));
                }
            }
        }

        matcher.appendTail(processedContent);
        return processedContent.toString();
    }

    /**
     * tmp 이미지를 product/{동적폴더} 경로로 이동
     * 
     * @param tmpImagePath  임시 이미지 경로
     * @param productFolder 상품 폴더명
     * @return 이동된 이미지의 새 경로, 실패시 null
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
                log.debug("디렉토리 생성: {}", targetDir);
            }

            if (Files.exists(sourcePath)) {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(sourcePath);

                log.debug("파일 이동 완료: {} -> {}", sourcePath, targetPath);
                return "product/" + productFolder + "/" + fileName;
            } else {
                log.warn("소스 파일이 존재하지 않습니다: {}", sourcePath);
                return null;
            }
        } catch (IOException e) {
            log.error("파일 이동 중 오류 발생: {}", tmpImagePath, e);
            return null;
        }
    }

    /**
     * URL에서 파일명 추출
     * 
     * @param imageUrl 이미지 URL
     * @return 파일명, 실패시 null
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
            log.warn("파일명 추출 중 오류: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * 상품 상세 등록
     * 
     * @param productId          상품 코드
     * @param productFolder      섬네일 경로
     * @param savedThumbnailName 섬네일 명
     * @param description        상품 상세 내용
     * @return 등록 결과
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
     * 상품 상세 수정
     * 
     * @param productId          상품 코드
     * @param productFolder      섬네일 경로
     * @param savedThumbnailName 섬네일 명
     * @param description        상품 상세 내용
     * @return 수정 결과
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
     * 상품 등록
     * 
     * @param productId 상품 코드
     * @param entity    저장될 상품 정보
     * @return 등록 결과
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
     * 상품 수정
     * 
     * @param productId 상품 코드
     * @param entity    수정될 상품 정보
     * @return 수정 결과
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
     * 에디터 이미지 삭제
     * 
     * @param productId           상품 코드
     * @param deletedDetailImages 저장될 파일 경로
     * @return 등록 결과
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
     * 에디터 이미지 경로 등록
     * 
     * @param productId       상품 코드
     * @param movedImagePaths 저장될 파일 경로
     * @return 등록 결과
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
     * 파일 경로에서 확장자 추출
     * 
     * @param filePath 파일 경로
     * @return 확장자 (점 제외), 없으면 빈 문자열
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
     * ResponseModel 생성
     * 
     * @param resultList 결과 데이터
     * @param result     처리 결과
     * @param message    메시지
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
