package com.cloudia.backend.CM_90_1040.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudia.backend.CM_90_1040.constants.CM901040Constant;
import com.cloudia.backend.CM_90_1040.constants.CM901040MessageConstant;
import com.cloudia.backend.constants.CMMessageConstant;
import com.cloudia.backend.CM_90_1040.mapper.CM901040Mapper;
import com.cloudia.backend.CM_90_1040.model.BannerInfo;
import com.cloudia.backend.CM_90_1040.model.ResponseModel;
import com.cloudia.backend.CM_90_1040.service.CM901040Service;
import com.cloudia.backend.common.exception.AuthenticationException;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.service.S3Service;
import com.cloudia.backend.common.util.DateCalculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901040ServiceImpl implements CM901040Service {
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${uuid.upload.dir}")
    private String uuidDir;

    @Value("${app.upload.type}")
    private String uploadType;

    @Autowired
    private final CM901040Mapper cm901040Mapper;
    @Autowired(required = false)
    private S3Service s3Service;

    private final DateCalculator dateCalculator;

    /**
     * 배너 삭제
     * 
     * @param entity 배너 삭제 항목 리스트
     * @return 삭제 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> bannerDel(List<BannerInfo> entity) {
        if (entity == null || entity.isEmpty()) {
            log.warn(CM901040MessageConstant.BANNER_DELETE_FAILED_EMPTY_LIST);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_NO_BANNER_SELECTED));
        }

        log.info(CM901040MessageConstant.BANNER_DELETE_START, entity.size());

        try {
            int result = 0;
            for (BannerInfo banner : entity) {
                if (banner == null || banner.getBannerId() <= 0) {
                    log.warn(CM901040MessageConstant.BANNER_DELETE_FAILED_INVALID_INFO, banner);
                    continue;
                }

                try {
                    result += cm901040Mapper.bannerDel(banner.getBannerId());
                    try {
                        deleteImageFile(banner.getImageLink());
                    } catch (Exception e) {
                        log.error(CM901040MessageConstant.IMAGE_DELETE_FAILED, banner.getImageLink(), e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(createResponseModel(0, false, CM901040MessageConstant.IMAGE_DELETE_FAILED));
                    }
                    log.debug(CM901040MessageConstant.BANNER_DELETE_COMPLETE, banner.getBannerId(),
                            banner.getImageLink());
                } catch (DataIntegrityViolationException dive) {
                    log.error(CM901040MessageConstant.BANNER_DELETE_FAILED_INTEGRITY_VIOLATION, banner.getBannerId(),
                            dive);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_REFERENCED_BANNER));
                }
            }

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
     * 배너 전체 리스트 조회
     * 
     * @return 배너 전체 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<BannerInfo>>> findByAllBanner() {
        log.info(CM901040MessageConstant.BANNER_FIND_ALL_START);

        try {
            List<BannerInfo> bannerInfoList = cm901040Mapper.findByAllBanner();
            if (bannerInfoList == null) {
                bannerInfoList = Collections.emptyList();
            }

            log.info(CM901040MessageConstant.BANNER_FIND_ALL_COMPLETE, bannerInfoList.size());
            return ResponseEntity
                    .ok(createResponseModel(bannerInfoList, true, CM901040MessageConstant.SUCCESS_BANNER_FIND));

        } catch (DataAccessException dae) {
            log.error(CM901040MessageConstant.BANNER_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 특정 배너 리스트 조회
     * 
     * @param searchTerm 배너명
     * @return 배너 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<BannerInfo>>> findByBanner(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.warn(CM901040MessageConstant.BANNER_SEARCH_FAILED_EMPTY_TERM);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(Collections.emptyList(), false,
                            CM901040MessageConstant.FAIL_SEARCH_TERM_REQUIRED));
        }

        String trimmedSearchTerm = searchTerm.trim();
        log.info(CM901040MessageConstant.BANNER_SEARCH_START, trimmedSearchTerm);
        try {
            List<BannerInfo> bannerInfoList = cm901040Mapper.findByBanner(trimmedSearchTerm);
            if (bannerInfoList == null) {
                bannerInfoList = Collections.emptyList();
            }

            log.info(CM901040MessageConstant.BANNER_SEARCH_COMPLETE, bannerInfoList.size());
            return ResponseEntity
                    .ok(createResponseModel(bannerInfoList, true, CM901040MessageConstant.SUCCESS_BANNER_FIND));

        } catch (DataAccessException dae) {
            log.error(CM901040MessageConstant.BANNER_SEARCH_DB_ERROR, trimmedSearchTerm, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_SEARCH_UNEXPECTED_ERROR, trimmedSearchTerm, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 업데이트용 배너 리스트 조회
     * 
     * @param bannerId 배너 아이디
     * @return 배너 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<BannerInfo>> findByBanner(int bannerId) {
        if (bannerId <= 0) {
            log.warn(CM901040MessageConstant.BANNER_FIND_BY_ID_FAILED_INVALID_ID, bannerId);
            return ResponseEntity.badRequest()
                    .body(createResponseModel(null, false, CM901040MessageConstant.FAIL_INVALID_BANNER_ID));
        }

        log.info(CM901040MessageConstant.BANNER_FIND_BY_ID_START, bannerId);

        try {
            BannerInfo bannerInfo = cm901040Mapper.findByBannerById(bannerId);
            if (bannerInfo == null) {
                return ResponseEntity.badRequest()
                        .body(createResponseModel(null, false, CM901040MessageConstant.FAIL_BANNER_NOT_EXISTS));
            }

            log.info(CM901040MessageConstant.BANNER_FIND_BY_ID_COMPLETE, bannerId);
            return ResponseEntity
                    .ok(createResponseModel(bannerInfo, true, CM901040MessageConstant.SUCCESS_BANNER_FIND));

        } catch (DataAccessException dae) {
            log.error(CM901040MessageConstant.BANNER_FIND_BY_ID_DB_ERROR, bannerId, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_FIND_BY_ID_UNEXPECTED_ERROR, bannerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 배너 등록
     * 
     * @param entity 등록할 배너 정보
     * @return 등록 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> bannerUpload(BannerInfo entity, String userId) {
        log.info(CM901040MessageConstant.BANNER_UPLOAD_START, entity != null ? entity.getBannerName() : "null");
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "배너 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        try {
            // 활성 배너 수 확인
            int activeBannerCount = findByUsedAllBanner();
            log.debug(CM901040MessageConstant.BANNER_UPLOAD_ACTIVE_COUNT_CHECK, activeBannerCount);

            if (activeBannerCount >= CM901040Constant.MAX_ACTIVE_BANNERS) {
                log.warn(CM901040MessageConstant.BANNER_UPLOAD_FAILED_MAX_EXCEEDED,
                        CM901040Constant.MAX_ACTIVE_BANNERS, activeBannerCount);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_MAX_ACTIVE_BANNERS));
            }

            // 디스플레이 순서 중복 확인
            if (isDisplayOrderDuplicated(entity.getDisplayOrder())) {
                log.warn(CM901040MessageConstant.BANNER_UPLOAD_FAILED_DUPLICATE_ORDER, entity.getDisplayOrder());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_DUPLICATE_DISPLAY_ORDER));
            }

            // 파일 저장
            String savedFileName = null;
            if (entity.getImageFile() != null && !entity.getImageFile().isEmpty()) {
                savedFileName = saveFile(entity.getImageFile());
                if (savedFileName == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(0, false, CMMessageConstant.ERROR_FILE_SAVE));
                }
                log.debug(CM901040MessageConstant.BANNER_FILE_SAVED, savedFileName);
            }

            int result = insertBanner(entity, savedFileName, userId);
            log.info(CM901040MessageConstant.BANNER_UPLOAD_COMPLETE, entity.getBannerName(), result);
            return ResponseEntity
                    .ok(createResponseModel(result, true, CM901040MessageConstant.SUCCESS_BANNER_UPLOAD));

        } catch (DuplicateKeyException dke) {
            log.error(CM901040MessageConstant.BANNER_UPLOAD_DUPLICATE_KEY_ERROR, dke.getMessage(), dke);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_DUPLICATE_BANNER_INFO));
        } catch (DataAccessException dae) {
            log.error(CM901040MessageConstant.BANNER_UPLOAD_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_UPLOAD_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 배너 업데이트
     * 
     * @param entity 업데이트할 배너 정보
     * @return 업데이트 여부
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Integer>> bannerUpdate(BannerInfo entity, String userId) {
        log.info(CM901040MessageConstant.BANNER_UPDATE_START,
                entity != null ? entity.getBannerId() : "null",
                entity != null ? entity.getBannerName() : "null");

        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "배너 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }

        try {
            // 기존 배너 정보 조회
            BannerInfo existingBanner = cm901040Mapper.findByBannerById(entity.getBannerId());
            if (existingBanner == null) {
                log.warn(CM901040MessageConstant.BANNER_UPDATE_FAILED_NOT_EXISTS, entity.getBannerId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_BANNER_NOT_EXISTS));
            }

            if (!existingBanner.getUpdatedAt().isEqual(entity.getUpdatedAt())) {
                log.warn(CM901040MessageConstant.FAIL_BANNER_UPDATE, entity.getBannerId());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_BANNER_UPDATE));
            }

            // 활성 배너 수 확인 (업데이트 시에는 현재 배너를 제외한 수를 확인)
            int activeBannerCount = findByUsedAllBanner();

            // 현재 비활성 배너를 활성으로 변경하는 경우만 체크
            if (entity.getIsDisplay() == 1 && activeBannerCount >= CM901040Constant.MAX_ACTIVE_BANNERS) {
                log.warn(CM901040MessageConstant.BANNER_UPDATE_FAILED_MAX_EXCEEDED,
                        CM901040Constant.MAX_ACTIVE_BANNERS, activeBannerCount);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_MAX_ACTIVE_BANNERS));
            }

            // 디스플레이 순서 변경 시 중복 확인
            if (existingBanner.getDisplayOrder() != entity.getDisplayOrder()) {
                if (isDisplayOrderDuplicated(entity.getDisplayOrder())) {
                    log.warn(CM901040MessageConstant.BANNER_UPDATE_FAILED_DUPLICATE_ORDER, entity.getDisplayOrder());
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(createResponseModel(0, false,
                                    CM901040MessageConstant.FAIL_DUPLICATE_DISPLAY_ORDER));
                }
            }

            // 파일 저장
            String savedFileName = null;
            if (entity.getImageFile() != null && !entity.getImageFile().isEmpty()) {
                savedFileName = saveFile(entity.getImageFile());
                if (savedFileName == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(createResponseModel(0, false, CMMessageConstant.ERROR_FILE_SAVE));
                }
                log.debug(CM901040MessageConstant.BANNER_FILE_SAVED, savedFileName);

                // 기존 파일 삭제
                try {
                    deleteImageFile(existingBanner.getImageLink());
                } catch (Exception e) {
                    log.error(CM901040MessageConstant.IMAGE_DELETE_FAILED, existingBanner.getImageLink(),
                            e.getMessage());
                }
            }

            int result = updateBanner(entity, savedFileName, userId);
            log.info(CM901040MessageConstant.BANNER_UPDATE_COMPLETE, entity.getBannerId(), result);
            return ResponseEntity
                    .ok(createResponseModel(result, true, CM901040MessageConstant.SUCCESS_BANNER_UPDATE));

        } catch (DuplicateKeyException dke) {
            log.error(CM901040MessageConstant.BANNER_UPDATE_DUPLICATE_KEY_ERROR, dke.getMessage(), dke);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createResponseModel(0, false, CM901040MessageConstant.FAIL_DUPLICATE_BANNER_UPDATE));
        } catch (DataAccessException dae) {
            log.error(CM901040MessageConstant.BANNER_UPDATE_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.BANNER_UPDATE_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(0, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * 사용 가능한 디스플레이 번호 리스트 조회
     * 
     * @return 디스플레이 번호 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<List<Integer>>> getFindDisplayOrder() {
        log.info(CM901040MessageConstant.DISPLAY_ORDER_FIND_START);

        try {
            List<Integer> displayOrderList = cm901040Mapper.getFindDisplayOrder();
            if (displayOrderList == null) {
                displayOrderList = Collections.emptyList();
            }

            log.info(CM901040MessageConstant.DISPLAY_ORDER_FIND_COMPLETE, displayOrderList.size());
            return ResponseEntity.ok(
                    createResponseModel(displayOrderList, true, CM901040MessageConstant.SUCCESS_DISPLAY_ORDER_FIND));

        } catch (DataAccessException dae) {
            log.error(CM901040MessageConstant.DISPLAY_ORDER_FIND_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false, CMMessageConstant.ERROR_DATABASE));
        } catch (Exception e) {
            log.error(CM901040MessageConstant.DISPLAY_ORDER_FIND_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(Collections.emptyList(), false,
                            CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
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
     * 사용중인 배너 개수 조회
     * 
     * @return 사용중인 배너 개수
     */
    private int findByUsedAllBanner() {
        try {
            return cm901040Mapper.findByUsedAllBanner();
        } catch (DataAccessException e) {
            log.error(CM901040MessageConstant.ACTIVE_BANNER_COUNT_ERROR, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 배너 표시 순서 중복 확인
     * 
     * @param displayOrder 표시 순서
     * @return 중복 여부
     */
    private boolean isDisplayOrderDuplicated(int displayOrder) {
        try {
            return cm901040Mapper.countByDisplayOrder(displayOrder) > 0;
        } catch (DataAccessException e) {
            log.error(CM901040MessageConstant.DISPLAY_ORDER_DUPLICATE_CHECK_ERROR, e.getMessage(), e);
            return true;
        }
    }

    /**
     * 배너 업데이트 실행
     * 
     * @param entity        배너 정보
     * @param savedFileName 저장된 파일명
     * @return 업데이트 결과
     */
    private int updateBanner(BannerInfo entity, String savedFileName, String userId) {
        BannerInfo bannerModel = new BannerInfo();
        bannerModel.setBannerId(entity.getBannerId());
        bannerModel.setBannerName(entity.getBannerName().trim());

        if (entity.getUrlLink() != null) {
            bannerModel.setUrlLink(entity.getUrlLink().trim());
        }

        if (savedFileName != null) {
            bannerModel.setImageLink(CM901040Constant.BANNER_PATH_PREFIX + savedFileName);
        }

        bannerModel.setIsDisplay(entity.getIsDisplay());
        bannerModel.setDisplayOrder(entity.getDisplayOrder());
        bannerModel.setUpdatedBy(userId);
        bannerModel.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901040Mapper.bannerUpdate(bannerModel);
    }

    /**
     * 배너 등록 실행
     * 
     * @param entity        배너 정보
     * @param savedFileName 저장된 파일명
     * @return 등록 결과
     */
    private int insertBanner(BannerInfo entity, String savedFileName, String userId) {
        BannerInfo bannerModel = new BannerInfo();

        bannerModel.setBannerName(entity.getBannerName().trim());

        if (entity.getUrlLink() != null) {
            bannerModel.setUrlLink(entity.getUrlLink().trim());
        }

        bannerModel.setImageLink(CM901040Constant.BANNER_PATH_PREFIX + savedFileName);
        bannerModel.setIsDisplay(entity.getIsDisplay());
        bannerModel.setDisplayOrder(entity.getDisplayOrder());
        bannerModel.setCreatedBy(userId);
        bannerModel.setCreatedAt(dateCalculator.tokyoTime());
        bannerModel.setUpdatedBy(userId);
        bannerModel.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901040Mapper.bannerUpload(bannerModel);
    }

    /**
     * 파일 저장
     * 
     * @param file 파일 정보
     * @return 저장된 파일 URL (실패 시 null)
     */
    private String saveFile(MultipartFile file) throws IOException, SecurityException {

        // 추가 보안 검증
        if (!isValidImageFile(file)) {
            log.error(CM901040MessageConstant.FAIL_INVALID_FILE_TYPE);
            return null;
        }

        // S3 업로드 사용
        if ("s3".equals(uploadType) && s3Service != null) {
            try {
                String fileUrl = s3Service.uploadFile(file, "images/banner");
                log.debug("S3 업로드 완료: {}", fileUrl);
                String fileName = Paths.get(fileUrl).getFileName().toString();
                return fileName;
            } catch (IOException e) {
                log.error("S3 업로드 실패", e);
                throw new IOException(CMMessageConstant.ERROR_FILE_SAVE_FAILED, e);
            }
        }

        // 로컬 저장
        Path uploadPath = Paths.get(uploadDir, CM901040Constant.UPLOAD_PATH);
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
     * 이미지 파일 삭제
     * 
     * @param imageLink 파일 링크
     * @throws IOException
     */
    private void deleteImageFile(String imageLink) throws IOException {
        if (imageLink == null || imageLink.trim().isEmpty()) {
            log.debug(CM901040MessageConstant.IMAGE_DELETE_NO_LINK);
            return;
        }

        // imageLink가 /banner/로 시작하는 경우 제거
        String cleanImageLink = imageLink.startsWith(CM901040Constant.BANNER_PATH_PREFIX)
                ? imageLink.substring(CM901040Constant.BANNER_PATH_PREFIX.length())
                : imageLink;

        Path filePath = Paths.get(uploadDir, "banner", cleanImageLink);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.debug(CM901040MessageConstant.IMAGE_DELETE_COMPLETE, filePath);
        } else {
            log.debug(CM901040MessageConstant.IMAGE_DELETE_NOT_EXISTS, filePath);
        }
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