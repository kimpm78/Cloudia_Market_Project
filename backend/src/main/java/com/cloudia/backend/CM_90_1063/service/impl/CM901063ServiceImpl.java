package com.cloudia.backend.CM_90_1063.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1063.mapper.CM901063Mapper;
import com.cloudia.backend.CM_90_1063.model.ProductCode;
import com.cloudia.backend.CM_90_1063.model.Stock;
import com.cloudia.backend.CM_90_1063.model.StockDetail;
import com.cloudia.backend.CM_90_1063.model.StockInfo;
import com.cloudia.backend.CM_90_1063.service.CM901063Service;
import com.cloudia.backend.common.exception.AuthenticationException;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901063ServiceImpl implements CM901063Service {
    // Mapper 정의
    private final CM901063Mapper cm901063Mapper;
    private final DateCalculator dateCalculator;

    /**
     * 상품 코드 전체 리스트 조회
     * 
     * @return 상품 코드 전체 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductCode> findAllProductCode() {
        List<ProductCode> productCodeList = cm901063Mapper.findAllProductCode();
        log.info("조회된 상품 코드 수: {}", productCodeList == null ? 0 : productCodeList.size());

        return productCodeList;
    }

    /**
     * 재고 입/출고 등록
     *
     * @param entity 등록 할 재고 정보 엔티티
     * 
     * @return 등록 성공 여부
     **/
    @Override
    @Transactional
    public Integer stockUpsert(ProductCode entity, String userId) {
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "재고 조회" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }

        if (entity == null) {
            log.info("재고 데이터가 없습니다.");
            return 0;
        }

        int result = 0;
        Optional<Stock> productOpt = cm901063Mapper.findByProductCode(entity.getProductCode());

        if (productOpt.isPresent() && productOpt.get().getProductCode() == null) {
            if (Integer.parseInt(entity.getQuantity()) < 0) {
                throw new IllegalArgumentException("재고 수량은 음수가 될 수 없습니다.");
            }
            log.info("[신규 등록] 상품코드: {}", entity.getProductCode());

            result = insertStock(entity, userId);

            Optional<Stock> StockId = cm901063Mapper.findByProductCode(entity.getProductCode());

            result = insertStockDetail(entity, StockId.get().getStockId(), userId);

            return result;
        } else {
            log.info("[업데이트] 상품코드: {}", entity.getProductCode());

            result = updateStock(productOpt, entity, userId);
            result = insertStockDetail(entity, productOpt.get().getStockId(), userId);

            return result;
        }

    }

    /**
     * 재고 등록
     * 
     * @param entity 재고 정보
     * @return 등록 여부
     */
    private int insertStock(ProductCode entity, String userId) {
        Stock stockModel = new Stock();

        stockModel.setProductCode(entity.getProductCode().trim());
        stockModel.setPrice(Integer.parseInt(entity.getProductPrice()));
        stockModel.setDefectiveQty(0);
        stockModel.setTotalQty(Integer.parseInt(entity.getQuantity()));
        stockModel.setAvailableQty(Integer.parseInt(entity.getQuantity()));
        stockModel.setCreatedBy(userId);
        stockModel.setCreatedAt(dateCalculator.tokyoTime());
        stockModel.setUpdatedBy(userId);
        stockModel.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901063Mapper.insertStock(stockModel);
    }

    /**
     * 재고 업데이트
     * 
     * @param productOpt 업데이트 재고 정보
     * @param entity     재고 정보
     * @return 업데이트 여부
     */
    private int updateStock(Optional<Stock> productOpt, ProductCode entity, String userId) {
        Stock stockModel = new Stock();
        BigDecimal totalQty = BigDecimal.valueOf(productOpt.get().getTotalQty())
                .add(BigDecimal.valueOf(Integer.parseInt(entity.getQuantity())));
        BigDecimal availableQty = BigDecimal.valueOf(productOpt.get().getAvailableQty())
                .add(BigDecimal.valueOf(Integer.parseInt(entity.getQuantity())));

        log.info("총 재고 수량 : totalQty : {}", totalQty);
        log.info("총 가용 재고 수량 : availableQty : {}", availableQty);

        if (totalQty.intValue() < 0 || availableQty.intValue() < 0) {
            throw new IllegalArgumentException("재고 수량은 음수가 될 수 없습니다.");
        }

        stockModel.setProductCode(entity.getProductCode().trim());
        stockModel.setPrice(Integer.parseInt(entity.getProductPrice()));
        stockModel.setTotalQty(totalQty.intValue());
        stockModel.setAvailableQty(availableQty.intValue());
        stockModel.setUpdatedBy(userId);
        stockModel.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901063Mapper.updateStock(stockModel);
    }

    /**
     * 재고 상세 등록
     * 
     * @param entity  재고 정보
     * @param stockId 재고 아이디
     * @return 등록 여부
     */
    private int insertStockDetail(ProductCode entity, int stockId, String userId) {
        StockDetail stockDetail = new StockDetail();
        stockDetail.setStockId(stockId);
        stockDetail.setQty(Integer.parseInt(entity.getQuantity()));
        stockDetail.setReason(entity.getNote());
        stockDetail.setCreatedBy(userId);
        stockDetail.setCreatedAt(dateCalculator.tokyoTime());
        stockDetail.setUpdatedBy(userId);
        stockDetail.setUpdatedAt(dateCalculator.tokyoTime());

        return cm901063Mapper.insertStockDetail(stockDetail);
    }

    /**
     * 입/출고 일람 전체 조회
     * 
     * @return 입/출고 일람 전체 리스트
     */
    @Override
    public List<StockInfo> findAllStocks() {
        List<StockInfo> productCodeList = cm901063Mapper.findAllStocks();
        log.info("조회된 상품 코드 수: {}", productCodeList.size());
        return productCodeList;
    }

    /**
     * 선택 된 상품 코드 / 상품명의 상품 가격 정보 조회
     * 
     * @param searchType 검색 타입 (1: 상품 코드 2: 상품 명)
     * @param searchTerm 검색어
     * @return 상품 정보
     */
    @Override
    public List<StockInfo> findByStocks(String searchType, String searchTerm) {

        List<StockInfo> productCodeList = cm901063Mapper.findByStocks(searchType, searchTerm);
        log.info("조회된 상품 코드 수: {}", productCodeList.size());
        return productCodeList;
    }

    /**
     * 특정 상품 코드 가격 조회
     * 
     * @param searchCode 검색 상품 코드
     * @return 상품 코드 리스트
     */
    @Override
    public Optional<Stock> getStockByCode(String searchCode) {
        Optional<Stock> productOpt = cm901063Mapper.findByProductCode(searchCode);
        return productOpt;
    }
}
