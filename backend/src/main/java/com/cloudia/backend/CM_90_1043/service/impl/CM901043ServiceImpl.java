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
import com.cloudia.backend.CM_90_1043.model.ResponseModel;
import com.cloudia.backend.CM_90_1043.service.CM901043Service;
import com.cloudia.backend.CM_90_1051.constants.CM901051MessageConstant;
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
     * 카테고리 전체 리스트 조회
     * 
     * @return 카테고리 전체 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseModel<Map<String, Object>>> findByAllCategory() {
        log.info(CM901051MessageConstant.ORDER_FIND_ALL_START);

        try {
            // 카테고리 그룹 및 상세 정보 조회
            List<CategoryGroupDTO> groups = cm901043Mapper.selectCategoryGroups();
            List<CategoryDetailDTO> details = cm901043Mapper.selectCategoryDetails();

            // 최신 수정 시간 조회
            LocalDateTime maxUpdatedAt = cm901043Mapper.selectMaxUpdatedAt();

            // 트리 구조 생성
            Map<String, Object> tree = getCategoryTree(groups, details);
            tree.put("maxUpdatedAt", maxUpdatedAt);

            log.info(CM901051MessageConstant.ORDER_FIND_ALL_COMPLETE);

            ResponseModel<Map<String, Object>> response = createResponseModel(
                    tree,
                    true,
                    "카테고리 목록 조회 성공");

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
     * 카테고리 변경사항 저장 (추가/수정/삭제)
     */
    @Override
    @Transactional
    public ResponseEntity<ResponseModel<String>> saveChanges(CategorySaveRequest request, String userId) {
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "카테고리 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }
        try {
            log.info("카테고리 저장 요청 - created: {}, updated: {}, deleted: {}",
                    request.getCreated().size(),
                    request.getUpdated().size(),
                    request.getDeleted().size());

            // 배타 체크
            LocalDateTime currentMaxUpdatedAt = cm901043Mapper.selectMaxUpdatedAt();

            // 클라이언트가 보낸 maxUpdatedAt과 비교
            if (request.getMaxUpdatedAt() != null &&
                    !request.getMaxUpdatedAt().equals(currentMaxUpdatedAt)) {

                log.warn("배타 체크 실패 - 클라이언트: {}, DB: {}",
                        request.getMaxUpdatedAt(), currentMaxUpdatedAt);

                ResponseModel<String> response = createResponseModel(
                        null,
                        false,
                        "데이터가 수정된 이력이 있습니다. 초기화 버튼을 클릭해서 최신 데이터로 다시 해주세요.");

                return ResponseEntity.ok(response);
            }

            // 1. 삭제 처리
            if (request.getDeleted() != null && !request.getDeleted().isEmpty()) {
                int deletedCount = 0;
                for (CategorySaveRequest.DeleteItem item : request.getDeleted()) {

                    if ("category".equals(item.getType())) {
                        log.info("카테고리 삭제: {}", item.getId());
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

                            String message = "다음 상품이 이 카테고리에 등록되어 있습니다" +
                                    productNames +
                                    "의 상품 삭제 후 다시 시도해주세요.";
                            ResponseModel<String> response = createResponseModel(
                                    null,
                                    false,
                                    message);

                            return ResponseEntity.ok(response);
                        }

                        deletedCount += cm901043Mapper.deleteCategory(category);
                    }
                }
                log.info("카테고리 삭제 완료: {}개 행 영향", deletedCount);
            }

            // 2. 수정 처리
            if (request.getUpdated() != null && !request.getUpdated().isEmpty()) {
                for (CategorySaveRequest.CategoryItem item : request.getUpdated()) {
                    if ("category".equals(item.getType())) {
                        log.info("카테고리 수정: {} - {}, order: {}, parentId: {}",
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
                        log.info("카테고리 수정 완료: {} ({}개 행 영향)", item.getId(), updatedCount);

                        if (updatedCount == 0) {
                            log.warn("카테고리 수정 실패 - 존재하지 않는 ID: {}", item.getId());
                        }
                    }
                }
            }

            // 3. 생성 처리
            if (request.getCreated() != null && !request.getCreated().isEmpty()) {
                for (CategorySaveRequest.CategoryItem item : request.getCreated()) {
                    if ("category".equals(item.getType())) {
                        log.info("카테고리 생성: {}, order: {}, parentId: {}",
                                item.getTitle(), item.getOrder(), item.getParentId());

                        // 검증
                        if (item.getParentId() == null || item.getParentId().isEmpty()) {
                            log.error("카테고리 생성 실패 - parentId 누락: {}", item.getTitle());
                            throw new IllegalArgumentException("카테고리는 반드시 그룹에 속해야 합니다: " + item.getTitle());
                        }
                        // 백엔드에서 다음 카테고리 코드 생성
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
                        log.info("카테고리 생성 완료: {} ({}개 행 추가)", item.getTitle(), insertedCount);
                    }
                }
            }

            log.info("카테고리 저장 완료");

            ResponseModel<String> response = createResponseModel(
                    "저장되었습니다.",
                    true,
                    "카테고리 저장 성공");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("카테고리 저장 실패 (유효성 검증)", e);
            ResponseModel<String> response = createResponseModel(
                    null,
                    false,
                    e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("카테고리 저장 실패", e);
            ResponseModel<String> response = createResponseModel(
                    null,
                    false,
                    "저장 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 그룹 + 상세 데이터를 트리 구조로 변환
     */
    public Map<String, Object> getCategoryTree(List<CategoryGroupDTO> groups, List<CategoryDetailDTO> details) {
        List<Map<String, Object>> children = groups.stream().map(g -> {

            // 각 그룹에 해당하는 상세 목록 필터링
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

            // 그룹 구조 생성
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("id", g.getCategoryGroupCode());
            groupMap.put("title", g.getCategoryGroupName());
            groupMap.put("isOpen", true);
            groupMap.put("children", childList);
            groupMap.put("order", g.getDisplayOrder());
            groupMap.put("flag", 0);
            return groupMap;

        }).collect(Collectors.toList());

        // 루트 노드 구성
        Map<String, Object> root = new HashMap<>();
        root.put("title", "전체 카테고리");
        root.put("isOpen", true);
        root.put("children", children);

        return root;
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
