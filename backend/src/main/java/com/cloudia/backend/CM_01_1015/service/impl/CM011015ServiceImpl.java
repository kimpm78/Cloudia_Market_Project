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
     * 交換・返品申請履歴一覧取得
     */
    @Override
    public ResponseModel<List<ReturnResponse>> getReturnHistory(String loginId) {
        User user = cm011001UserMapper.findByLoginId(loginId);
        if (user == null) {
            return ResponseModel.<List<ReturnResponse>>builder()
                    .result(false)
                    .message("ユーザー情報なし")
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
                .message("取得成功")
                .resultList(list)
                .build();
    }

    /**
     * 交換・返品の統合申請リクエスト
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<Object>> createReturnRequest(String loginId, ReturnRequest request) {
        try {
            User user = cm011001UserMapper.findByLoginId(loginId);
            if (user == null) {
                return ResponseEntity.ok(ResponseModel.builder().result(false).message("ログインが必要です。").build());
            }

            LocalDateTime tokyoNow = dateCalculator.tokyoTime();
            int userId = user.getUserId().intValue();

            // 画像アップロード処理
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
                            log.error("ファイル保存失敗: {}", file.getOriginalFilename(), e);
                        }
                    }
                }
            }

            // 申請マスター情報保存
            String joinedImageUrls = String.join(",", imageUrls);
            cm011015Mapper.insertReturnRequest(request, joinedImageUrls, userId, tokyoNow);

            // 詳細商品情報保存
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
                    .message((request.getType() == 0 ? "返品" : "交換") + "申請が正常に処理されました。")
                    .build());

        } catch (Exception e) {
            log.error("申請処理エラー: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseModel.builder().result(false).message("サーバーエラーが発生しました").build());
        }
    }

    /**
     * 交換・返品申請詳細取得
     */
    @Override
    public ResponseModel<ReturnResponse> getReturnDetail(String loginId, int returnId) {
        User user = cm011001UserMapper.findByLoginId(loginId);
        if (user == null) {
            return ResponseModel.<ReturnResponse>builder().result(false).message("認証エラー").build();
        }

        int userId = user.getUserId().intValue();
        ReturnResponse response = cm011015Mapper.getReturnDetail(returnId, userId);

        if (response == null) {
            return ResponseModel.<ReturnResponse>builder().result(false).message("履歴なし").build();
        }

        CodeMaster code = codeMasterService.getCodeByValue("005", response.getReturnStatusValue());
        if (code != null) {
            response.setReturnStatusName(code.getCodeValueName());
        }

        return ResponseModel.<ReturnResponse>builder().result(true).message("取得成功").resultList(response).build();
    }

    /**
     * 注文商品一覧取得
     */
    @Override
    public ResponseEntity<List<ReturnResponse.ProductInfo>> getOrderProducts(String loginId, String orderNo) {
        User user = cm011001UserMapper.findByLoginId(loginId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int userId = user.getUserId().intValue();
        List<ReturnResponse.ProductInfo> products = cm011015Mapper.getProductsByOrderNo(orderNo, userId);

        log.info("注文番号 {} の商品取得完了 (ユーザーID: {})", orderNo, userId);
        return ResponseEntity.ok(products);
    }

    /**
     * ファイル保存
     * 
     * @param file ファイル情報
     * @return 保存されたファイル名
     * @throws IOException       ファイル保存中にエラーが発生した場合
     * @throws SecurityException セキュリティ検証に失敗した場合
     */
    private String saveFile(MultipartFile file, String path) throws IOException {
        // 追加セキュリティ検証
        if (!isValidImageFile(file)) {
            log.error("無効なファイル形式です。");
            throw new IOException("無効なファイル形式です。");
        }

        // 2. S3アップロードロジック（他ファイルと文脈統一）
        if ("s3".equals(uploadType) && s3Service != null) {
            try {
                // ポイント: ここで "images/" を付与して呼び出し側の負担を軽減
                String tmpUrl = "images/".concat(path);
                String fileUrl = s3Service.uploadFile(file, tmpUrl);

                log.debug("S3アップロード完了: {}", fileUrl);
                // S3から返却されたURLからファイル名のみ抽出して返却
                return Paths.get(fileUrl).getFileName().toString();
            } catch (IOException e) {
                log.error("S3アップロード失敗", e);
                throw new IOException("S3ファイル保存失敗", e);
            }
        }

        Path uploadPath = Paths.get(uploadDir, path);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.debug("ディレクトリ作成完了: {}", uploadPath.toString());
            }
        } catch (IOException e) {
            log.error("ディレクトリ作成失敗", e);
            throw new IOException("ファイルアップロード用ディレクトリ作成失敗", e);
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
            log.debug("ローカルファイル保存完了: {}", savedFileName);
            return savedFileName;
        } catch (IOException e) {
            log.error("ファイル保存失敗: {}", savedFileName, e);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException deleteException) {
                log.warn("ファイル削除失敗（クリーンアップ中）", deleteException);
            }
            throw new IOException("ファイル保存失敗", e);
        }
    }

    /**
     * セキュリティ用ランダム文字列生成
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
     * 管理者通知メール送信
     */
    private void sendAdminNotification(User user, ReturnRequest request, List<String> codes, int returnNumber) {

        List<String> adminEmails = cm011001UserMapper.findEmailsByRoleId(1);
        if (adminEmails == null || adminEmails.isEmpty()) {
            log.warn("管理者権限ユーザーがいないためメールを送信できませんでした。\n");
            return;
        }

        String rawContent = request.getContent();
        String cleanContent = rawContent;
        if (rawContent != null && rawContent.contains("주문번호:")) {
            cleanContent = rawContent.substring(rawContent.indexOf("주문번호:")).trim();
        }

        Map<String, String> templateData = new HashMap<>();
        templateData.put("title", request.getTitle());
        templateData.put("type", request.getType() == 0 ? "反품" : "교환");
        templateData.put("returnNumber", String.valueOf(returnNumber));
        templateData.put("orderNumber", request.getOrderNo());
        templateData.put("memberNumber", user.getMemberNumber());
        templateData.put("productCode", String.join(", ", codes));
        templateData.put("content", cleanContent);

        try {
            emailService.sendAdminReturnNotification(adminEmails, templateData);
        } catch (Exception e) {
            log.error("メール送信中エラー", e);
        }
    }

    /**
     * 画像ファイル形式検証
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
        for (String extension : CM011015Constant.ALLOWED_EXTENSIONS) {
            if (lowerCaseFileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 申請可能な注文一覧取得
     */
    @Override
    public ResponseModel<List<Map<String, Object>>> getReturnableOrderList(String loginId) {
        User user = cm011001UserMapper.findByLoginId(loginId);
        if (user == null) {
            return ResponseModel.<List<Map<String, Object>>>builder()
                    .result(false)
                    .message("ユーザー情報がありません。")
                    .build();
        }

        int userId = user.getUserId().intValue();
        List<Map<String, Object>> list = cm011015Mapper.getReturnableOrders(userId);

        return ResponseModel.<List<Map<String, Object>>>builder()
                .result(true)
                .message("申請可能一覧の取得成功")
                .resultList(list)
                .build();
    }

}