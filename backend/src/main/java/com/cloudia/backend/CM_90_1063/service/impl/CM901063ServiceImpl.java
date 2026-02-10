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
    private final CM901063Mapper cm901063Mapper;
    private final DateCalculator dateCalculator;

    /**
     * 商品コード一覧を取得
     * 
     * @return 商品コード一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductCode> findAllProductCode() {
        List<ProductCode> productCodeList = cm901063Mapper.findAllProductCode();
        log.info("取得した商品コード数: {}", productCodeList == null ? 0 : productCodeList.size());

        return productCodeList;
    }

    /**
     * 在庫入出庫の登録
     *
     * @param entity 登録する在庫情報エンティティ
     * 
     * @return 登録成功可否
     **/
    @Override
    @Transactional
    public Integer stockUpsert(ProductCode entity, String userId) {
        if (null == userId || userId.isBlank()) {
            LogHelper.log(LogMessage.AUTH_TOKEN_INVALID, new String[] { "在庫照会" });
            throw new AuthenticationException(ErrorCode.INVALID_TOKEN);
        }

        if (entity == null) {
            log.info("在庫データがありません。");
            return 0;
        }

        int result = 0;
        Optional<Stock> productOpt = cm901063Mapper.findByProductCode(entity.getProductCode());

        if (productOpt.isPresent() && productOpt.get().getProductCode() == null) {
            if (Integer.parseInt(entity.getQuantity()) < 0) {
                throw new IllegalArgumentException("在庫数量は負の値にできません。");
            }
            log.info("[新規登録] 商品コード: {}", entity.getProductCode());

            result = insertStock(entity, userId);

            Optional<Stock> StockId = cm901063Mapper.findByProductCode(entity.getProductCode());

            result = insertStockDetail(entity, StockId.get().getStockId(), userId);

            return result;
        } else {
            log.info("[更新] 商品コード: {}", entity.getProductCode());

            result = updateStock(productOpt, entity, userId);
            result = insertStockDetail(entity, productOpt.get().getStockId(), userId);

            return result;
        }

    }

    /**
     * 在庫登録
     * 
     * @param entity 在庫情報
     * @return 登録可否
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
     * 在庫更新
     * 
     * @param productOpt 更新対象の在庫情報
     * @param entity     在庫情報
     * @return 更新可否
     */
    private int updateStock(Optional<Stock> productOpt, ProductCode entity, String userId) {
        Stock stockModel = new Stock();
        BigDecimal totalQty = BigDecimal.valueOf(productOpt.get().getTotalQty())
                .add(BigDecimal.valueOf(Integer.parseInt(entity.getQuantity())));
        BigDecimal availableQty = BigDecimal.valueOf(productOpt.get().getAvailableQty())
                .add(BigDecimal.valueOf(Integer.parseInt(entity.getQuantity())));

        log.info("総在庫数量（totalQty）: {}", totalQty);
        log.info("総利用可能在庫数量（availableQty）: {}", availableQty);

        if (totalQty.intValue() < 0 || availableQty.intValue() < 0) {
            throw new IllegalArgumentException("在庫数量は負の値にできません。");
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
     * 在庫詳細登録
     * 
     * @param entity  在庫情報
     * @param stockId 在庫ID
     * @return 登録可否
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
     * 入出庫一覧を全件取得
     * 
     * @return 入出庫一覧
     */
    @Override
    public List<StockInfo> findAllStocks() {
        List<StockInfo> productCodeList = cm901063Mapper.findAllStocks();
        log.info("取得した商品コード数: {}", productCodeList.size());
        return productCodeList;
    }

    /**
     * 指定した商品コード／商品名で商品価格情報を取得
     * 
     * @param searchType 検索種別（1: 商品コード 2: 商品名）
     * @param searchTerm 検索キーワード
     * @return 商品情報
     */
    @Override
    public List<StockInfo> findByStocks(String searchType, String searchTerm) {

        List<StockInfo> productCodeList = cm901063Mapper.findByStocks(searchType, searchTerm);
        log.info("取得した商品コード数: {}", productCodeList.size());
        return productCodeList;
    }

    /**
     * 指定した商品コードの価格情報を取得
     * 
     * @param searchCode 検索する商品コード
     * @return 商品コード情報
     */
    @Override
    public Optional<Stock> getStockByCode(String searchCode) {
        Optional<Stock> productOpt = cm901063Mapper.findByProductCode(searchCode);
        return productOpt;
    }
}
