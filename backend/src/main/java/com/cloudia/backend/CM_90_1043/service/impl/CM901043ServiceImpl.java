package com.cloudia.backend.CM_90_1043.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1043.mapper.CM901043Mapper;
import com.cloudia.backend.CM_90_1043.model.CategoryDetailDTO;
import com.cloudia.backend.CM_90_1043.model.CategoryGroupDTO;
import com.cloudia.backend.CM_90_1043.model.CategorySaveRequest;
import com.cloudia.backend.CM_90_1043.model.ProductDTO;
import com.cloudia.backend.CM_90_1043.service.CM901043Service;
import com.cloudia.backend.CM_90_1051.constants.CM901051MessageConstant;
import com.cloudia.backend.common.model.ResponseModel;
import com.cloudia.backend.common.exception.AuthenticationException;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.util.DateCalculator;
import com.cloudia.backend.constants.CMMessageConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901043ServiceImpl implements CM901043Service {
    private final CM901043Mapper cm901043Mapper;
    private final DateCalculator dateCalculator;

    /**
     * カテゴリー全件一覧取得
     *
     * @return カテゴリー全件一覧
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<Map<String, Object>>> findByAllCategory() {
        log.info(CM901051MessageConstant.ORDER_FIND_ALL_START);

        try {
            // カテゴリーグループおよび詳細情報を取得
            List<CategoryGroupDTO> groups = cm901043Mapper.selectCategoryGroups();
            List<CategoryDetailDTO> details = cm901043Mapper.selectCategoryDetails();

            // 最終更新日時を取得
            LocalDateTime maxUpdatedAt = cm901043Mapper.selectMaxUpdatedAt();

            // ツリー構造を生成
            Map<String, Object> tree = getCategoryTree(groups, details);
            tree.put("maxUpdatedAt", maxUpdatedAt);

            log.info(CM901051MessageConstant.ORDER_FIND_ALL_COMPLETE);

            ResponseModel<Map<String, Object>> response = createResponseModel(
                    tree,
                    true,
                    "カテゴリー一覧の取得に成功しました");

            return ResponseEntity.ok(response);

        } catch (DataAccessException dae) {
            log.error(CM901051MessageConstant.ORDER_FIND_ALL_DB_ERROR, dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, CMMessageConstant.ERROR_DATABASE));
        } catch (NullPointerException npe) {
            log.error(CM901051MessageConstant.ORDER_FIND_ALL_NULL_ERROR, npe.getMessage(), npe);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createResponseModel(null, false, CMMessageConstant.ERROR_NULL));
        } catch (Exception e) {
            log.error(CM901051MessageConstant.ORDER_FIND_ALL_UNEXPECTED_ERROR, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createResponseModel(null, false, CMMessageConstant.ERROR_INTERNAL_SERVER));
        }
    }

    /**
     * カテゴリー変更内容を保存（追加/更新/削除）
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<String>> saveChanges(CategorySaveRequest request, String userId) {
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "カテゴリー取得" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        try {
            log.info("カテゴリー保存リクエスト - created: {}, updated: {}, deleted: {}",
                    request.getCreated().size(),
                    request.getUpdated().size(),
                    request.getDeleted().size());

            // 排他チェック
            LocalDateTime currentMaxUpdatedAt = cm901043Mapper.selectMaxUpdatedAt();

            // クライアントが送信した maxUpdatedAt と比較
            if (request.getMaxUpdatedAt() != null &&
                    !request.getMaxUpdatedAt().equals(currentMaxUpdatedAt)) {

                log.warn("排他チェック失敗 - クライアント: {}, DB: {}",
                        request.getMaxUpdatedAt(), currentMaxUpdatedAt);

                ResponseModel<String> response = createResponseModel(
                        null,
                        false,
                        "データが更新されています。初期化ボタンをクリックして最新データでやり直してください。");

                return ResponseEntity.ok(response);
            }

            // 1. 削除処理
            if (request.getDeleted() != null && !request.getDeleted().isEmpty()) {
                int deletedCount = 0;
                for (CategorySaveRequest.DeleteItem item : request.getDeleted()) {

                    if ("category".equals(item.getType())) {
                        log.info("カテゴリー削除: {}", item.getId());
                        CategoryDetailDTO category = new CategoryDetailDTO();
                        category.setCategoryCode(item.getId());
                        category.setCategoryGroupCode(item.getParentId());
                        category.setUpdatedBy(userId);
                        category.setUpdatedAt(dateCalculator.tokyoTime());
                        List<ProductDTO> linkedProducts = cm901043Mapper.selectByGroupAndCode(category);

                        if (linkedProducts != null && !linkedProducts.isEmpty()) {
                            String productNames = linkedProducts.stream()
                                    .map(ProductDTO::getName)
                                    .collect(Collectors.joining(", "));

                            String message = "次の商品がこのカテゴリに登録されています: " +
                                    productNames +
                                    "。商品を削除してから再度お試しください。";
                            ResponseModel<String> response = createResponseModel(
                                    null,
                                    false,
                                    message);

                            return ResponseEntity.ok(response);
                        }

                        deletedCount += cm901043Mapper.deleteCategory(category);
                    }
                }
                log.info("カテゴリー削除完了: {}件の行に影響", deletedCount);
            }

            // 2. 更新処理
            if (request.getUpdated() != null && !request.getUpdated().isEmpty()) {
                for (CategorySaveRequest.CategoryItem item : request.getUpdated()) {
                    if ("category".equals(item.getType())) {
                        log.info("カテゴリー更新: {} - {}, order: {}, parentId: {}",
                                item.getId(), item.getTitle(), item.getOrder(), item.getParentId());
                        CategoryDetailDTO category = new CategoryDetailDTO();
                        category.setCategoryCode(item.getId());
                        category.setCategoryName(item.getTitle());
                        category.setDisplayOrder(item.getOrder());
                        category.setCategoryGroupCode(item.getParentId());
                        category.setIsActive(0);
                        category.setUpdatedBy(userId);
                        category.setUpdatedAt(dateCalculator.tokyoTime());
                        int updatedCount = cm901043Mapper.updateCategory(category);
                        log.info("カテゴリー更新完了: {} ({}件の行に影響)", item.getId(), updatedCount);

                        if (updatedCount == 0) {
                            log.warn("カテゴリー更新失敗 - 存在しないID: {}", item.getId());
                        }
                    }
                }
            }

            // 3. 生成処理
            if (request.getCreated() != null && !request.getCreated().isEmpty()) {
                for (CategorySaveRequest.CategoryItem item : request.getCreated()) {
                    if ("category".equals(item.getType())) {
                        log.info("カテゴリー生成: {}, order: {}, parentId: {}",
                                item.getTitle(), item.getOrder(), item.getParentId());

                        // 検証
                        if (item.getParentId() == null || item.getParentId().isEmpty()) {
                            log.error("カテゴリー生成失敗 - parentId 未設定: {}", item.getTitle());
                            throw new IllegalArgumentException("カテゴリは必ずグループに属している必要があります: " + item.getTitle());
                        }
                        // バックエンド側で次のカテゴリコードを生成
                        String nextCategoryCode = cm901043Mapper.selectNextCategoryCode(item.getParentId());

                        CategoryDetailDTO category = new CategoryDetailDTO();
                        category.setCategoryName(item.getTitle());
                        category.setDisplayOrder(item.getOrder());
                        category.setCategoryGroupCode(item.getParentId());
                        category.setCategoryCode(nextCategoryCode);
                        category.setIsActive(1);
                        category.setCreatedBy(userId);
                        category.setCreatedAt(dateCalculator.tokyoTime());
                        category.setUpdatedBy(userId);
                        category.setUpdatedAt(dateCalculator.tokyoTime());
                        int insertedCount = cm901043Mapper.insertCategory(category);
                        log.info("カテゴリー生成完了: {} ({}件の行を追加)", item.getTitle(), insertedCount);
                    }
                }
            }

            log.info("カテゴリー保存完了");

            ResponseModel<String> response = createResponseModel(
                    "保存しました。",
                    true,
                    "カテゴリーの保存に成功しました");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("カテゴリー保存失敗（バリデーション）", e);
            ResponseModel<String> response = createResponseModel(
                    null,
                    false,
                    e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("カテゴリー保存失敗", e);
            ResponseModel<String> response = createResponseModel(
                    null,
                    false,
                    "保存中にエラーが発生しました: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * グループ + 詳細データをツリー構造へ変換
     */
    public Map<String, Object> getCategoryTree(List<CategoryGroupDTO> groups, List<CategoryDetailDTO> details) {
        List<Map<String, Object>> children = groups.stream().map(g -> {

            // 各グループに紐づく詳細リストを抽出
            List<Map<String, Object>> childList = details.stream()
                    .filter(d -> d.getCategoryGroupCode().equals(g.getCategoryGroupCode()))
                    .sorted(Comparator.comparing(CategoryDetailDTO::getDisplayOrder))
                    .map(d -> {
                        Map<String, Object> detailMap = new HashMap<>();
                        detailMap.put("id", d.getCategoryCode());
                        detailMap.put("title", d.getCategoryName());
                        detailMap.put("order", d.getDisplayOrder());
                        detailMap.put("flag", 0);
                        detailMap.put("children", new ArrayList<>());
                        return detailMap;
                    })
                    .collect(Collectors.toList());

            // グループ構造を生成
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("id", g.getCategoryGroupCode());
            groupMap.put("title", g.getCategoryGroupName());
            groupMap.put("isOpen", true);
            groupMap.put("children", childList);
            groupMap.put("order", g.getDisplayOrder());
            groupMap.put("flag", 0);
            return groupMap;

        }).collect(Collectors.toList());

        // ルートノード構成
        Map<String, Object> root = new HashMap<>();
        root.put("title", "全カテゴリ");
        root.put("isOpen", true);
        root.put("children", children);

        return root;
    }

    /**
     * ResponseModel 生成
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
