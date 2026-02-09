package com.cloudia.backend.CM_01_1015.service.impl;

import com.cloudia.backend.CM_01_1001.mapper.CM011001UserMapper;
import com.cloudia.backend.CM_01_1001.model.User;
import com.cloudia.backend.CM_01_1015.constants.CM011015Constant;
import com.cloudia.backend.CM_01_1015.mapper.CM011015Mapper;
import com.cloudia.backend.CM_01_1015.model.ReturnResponse;
import com.cloudia.backend.CM_01_1015.model.ReturnRequest;
import com.cloudia.backend.CM_01_1015.service.CM011015Service;
import com.cloudia.backend.common.model.CodeMaster;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.service.CodeMasterService;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.common.service.S3Service;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM011015ServiceImpl implements CM011015Service {

    private final CM011015Mapper cm011015Mapper;
    private final CM011001UserMapper cm011001UserMapper;
    private final EmailService emailService;
    private final DateCalculator dateCalculator;
    private final CodeMasterService codeMasterService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.type}")
    private String uploadType;

    @Value("${uuid.upload.dir}")
    private String uuidDir;

    @Autowired(required = false)
    private S3Service s3Service;

    /**
     * 교환/반품 신청 내역 목록 조회
     */
    @Override
    public ResponseModel<List<ReturnResponse>> getReturnHistory(String loginId) {
        User user = cm011001UserMapper.findByLoginId(loginId);
        if (user == null) {
            return ResponseModel.<List<ReturnResponse>>builder()
                    .result(false)
                    .message("사용자 정보 없음")
                    .build();
        }

        int userId = user.getUserId().intValue();
        List<ReturnResponse> list = cm011015Mapper.getReturnList(userId);

        for (ReturnResponse item : list) {
            CodeMaster code = codeMasterService.getCodeByValue("005", item.getReturnStatusValue());
            if (code != null) {
                item.setReturnStatusName(code.getCodeValueName());
            }
        }
        return ResponseModel.<List<ReturnResponse>>builder()
                .result(true)
                .message("조회 성공")
                .resultList(list)
                .build();
    }

    /**
     * 교환/반품 통합 신청 요청
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Object>> createReturnRequest(String loginId, ReturnRequest request) {
        try {
            User user = cm011001UserMapper.findByLoginId(loginId);
            if (user == null) {
                return ResponseEntity.ok(ResponseModel.builder().result(false).message("로그인이 필요합니다.").build());
            }

            LocalDateTime tokyoNow = dateCalculator.tokyoTime();
            int userId = user.getUserId().intValue();

            // 이미지 업로드 처리
            List<String> imageUrls = new ArrayList<>();

            String folderName = "returns/" + tokyoNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            if (request.getFiles() != null && request.getFiles().length > 0) {
                for (MultipartFile file : request.getFiles()) {
                    if (!file.isEmpty()) {
                        try {
                            String savedFileName = saveFile(file, folderName);
                            if (savedFileName != null) {
                                imageUrls.add(folderName + "/" + savedFileName);
                            }
                        } catch (Exception e) {
                            log.error("파일 저장 실패: {}", file.getOriginalFilename(), e);
                        }
                    }
                }
            }

            // 신청 마스터 정보 저장
            String joinedImageUrls = String.join(",", imageUrls);
            cm011015Mapper.insertReturnRequest(request, joinedImageUrls, userId, tokyoNow);

            // 상세 상품 정보 저장
            List<String> codeListForEmail = new ArrayList<>();
            if (request.getProductCode() != null && !request.getProductCode().isEmpty()) {
                String[] products = request.getProductCode().split(",");
                for (String prodStr : products) {
                    String[] parts = prodStr.split(":");
                    if (parts.length == 2) {
                        String code = parts[0];
                        int quantity = Integer.parseInt(parts[1]);

                        cm011015Mapper.insertReturnDetail(
                                code,
                                quantity,
                                user.getLoginId());

                        codeListForEmail.add(code);
                    }
                }
            }

            int returnNumber = cm011015Mapper.getCurrentReturnId();

            cm011015Mapper.updateToExchangeStatus(request.getOrderNo(), user.getMemberNumber());

            sendAdminNotification(user, request, codeListForEmail, returnNumber);

            return ResponseEntity.ok(ResponseModel.builder()
                    .result(true)
                    .message((request.getType() == 0 ? "반품" : "교환") + " 신청이 정상적으로 처리되었습니다.")
                    .build());

        } catch (Exception e) {
            log.error("신청 처리 에러: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseModel.builder().result(false).message("서버 오류 발생").build());
        }
    }

    /**
     * 교환/반품 상세 정보 조회
     */
    @Override
    public ResponseModel<ReturnResponse> getReturnDetail(String loginId, int returnId) {
        User user = cm011001UserMapper.findByLoginId(loginId);
        if (user == null) {
            return ResponseModel.<ReturnResponse>builder().result(false).message("인증 오류").build();
        }

        int userId = user.getUserId().intValue();
        ReturnResponse response = cm011015Mapper.getReturnDetail(returnId, userId);

        if (response == null) {
            return ResponseModel.<ReturnResponse>builder().result(false).message("내역 없음").build();
        }

        CodeMaster code = codeMasterService.getCodeByValue("005", response.getReturnStatusValue());
        if (code != null) {
            response.setReturnStatusName(code.getCodeValueName());
        }

        return ResponseModel.<ReturnResponse>builder().result(true).message("조회 성공").resultList(response).build();
    }

    /**
     * 주문 상품 목록 조회
     */
    @Override
    public ResponseEntity<List<ReturnResponse.ProductInfo>> getOrderProducts(String loginId, String orderNo) {
        User user = cm011001UserMapper.findByLoginId(loginId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int userId = user.getUserId().intValue();
        List<ReturnResponse.ProductInfo> products = cm011015Mapper.getProductsByOrderNo(orderNo, userId);

        log.info("주문번호 {} 상품 조회 완료 (사용자ID: {})", orderNo, userId);
        return ResponseEntity.ok(products);
    }

    /**
     * 파일 저장
     * 
     * @param file 파일 정보
     * @return 저장된 파일명
     * @throws IOException       파일 저장 중 오류 발생 시
     * @throws SecurityException 보안 검증 실패 시
     */
    private String saveFile(MultipartFile file, String path) throws IOException {
        // 추가 보안 검증
        if (!isValidImageFile(file)) {
            log.error("유효하지 않은 파일 형식입니다.");
            throw new IOException("유효하지 않은 파일 형식입니다.");
        }

        // 2. S3 업로드 로직 (다른 파일과 맥락 통일)
        if ("s3".equals(uploadType) && s3Service != null) {
            try {
                // 핵심: 여기서 "images/"를 붙여줌으로써 호출부의 부담을 덜어줌
                String tmpUrl = "images/".concat(path);
                String fileUrl = s3Service.uploadFile(file, tmpUrl);

                log.debug("S3 업로드 완료: {}", fileUrl);
                // S3에서 반환된 URL에서 파일명만 추출해서 반환
                return Paths.get(fileUrl).getFileName().toString();
            } catch (IOException e) {
                log.error("S3 업로드 실패", e);
                throw new IOException("S3 파일 저장 실패", e);
            }
        }

        Path uploadPath = Paths.get(uploadDir, path);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.debug("디렉토리 생성됨: {}", uploadPath.toString());
            }
        } catch (IOException e) {
            log.error("디렉토리 생성 실패", e);
            throw new IOException("파일 업로드 디렉토리 생성 실패", e);
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
            log.debug("로컬 파일 저장 완료: {}", savedFileName);
            return savedFileName;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", savedFileName, e);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException deleteException) {
                log.warn("파일 삭제 실패 (정리 중)", deleteException);
            }
            throw new IOException("파일 저장 실패", e);
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
     * 관리자 알림 메일 발송
     */
    private void sendAdminNotification(User user, ReturnRequest request, List<String> codes, int returnNumber) {

        List<String> adminEmails = cm011001UserMapper.findEmailsByRoleId(1);
        if (adminEmails == null || adminEmails.isEmpty()) {
            log.warn("관리자 권한 유저가 없어 메일을 발송하지 못했습니다.");
            return;
        }

        String rawContent = request.getContent();
        String cleanContent = rawContent;
        if (rawContent != null && rawContent.contains("주문번호:")) {
            cleanContent = rawContent.substring(rawContent.indexOf("주문번호:")).trim();
        }

        Map<String, String> templateData = new HashMap<>();
        templateData.put("title", request.getTitle());
        templateData.put("type", request.getType() == 0 ? "반품" : "교환");
        templateData.put("returnNumber", String.valueOf(returnNumber));
        templateData.put("orderNumber", request.getOrderNo());
        templateData.put("memberNumber", user.getMemberNumber());
        templateData.put("productCode", String.join(", ", codes));
        templateData.put("content", cleanContent);

        try {
            emailService.sendAdminReturnNotification(adminEmails, templateData);
        } catch (Exception e) {
            log.error("메일 발송 중 오류", e);
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
        for (String extension : CM011015Constant.ALLOWED_EXTENSIONS) {
            if (lowerCaseFileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 신청 가능한 주문 목록 조회
     */
    @Override
    public ResponseModel<List<Map<String, Object>>> getReturnableOrderList(String loginId) {
        User user = cm011001UserMapper.findByLoginId(loginId);
        if (user == null) {
            return ResponseModel.<List<Map<String, Object>>>builder()
                    .result(false)
                    .message("사용자 정보가 없습니다.")
                    .build();
        }

        int userId = user.getUserId().intValue();
        List<Map<String, Object>> list = cm011015Mapper.getReturnableOrders(userId);

        return ResponseModel.<List<Map<String, Object>>>builder()
                .result(true)
                .message("신청 가능 목록 조회 성공")
                .resultList(list)
                .build();
    }

}