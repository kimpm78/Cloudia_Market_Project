package com.cloudia.backend.CM_90_1065.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloudia.backend.CM_90_1065.mapper.CM901065Mapper;
import com.cloudia.backend.CM_90_1065.model.ProductCodeDto;
import com.cloudia.backend.CM_90_1065.service.CM901065Service;
import com.cloudia.backend.common.exception.ErrorCode;
import com.cloudia.backend.common.exception.InvalidRequestException;
import com.cloudia.backend.common.log.LogHelper;
import com.cloudia.backend.common.log.LogMessage;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CM901065ServiceImpl implements CM901065Service {
    private final CM901065Mapper cm901065Mapper;
    private final DateCalculator dateCalculator;

    /**
     * 商品コード取得
     * 
     * @return 商品コード一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductCodeDto> getProductCode() {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "商品コード登録" });

        List<ProductCodeDto> responseList = cm901065Mapper.getProductCode();

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "商品コード登録", String.valueOf(responseList.size()) });

        return responseList;
    }

    /**
     * 商品コード検索
     * 
     * @param searchTerm キーワード
     * @param searchType 種別（1:商品コード、2:商品名）
     * @return 商品コード一覧
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductCodeDto> findByProductCode(String searchTerm, int searchType) {
        LogHelper.log(LogMessage.COMMON_SELECT_START, new String[] { "商品コード登録" });

        List<ProductCodeDto> responseList = cm901065Mapper.findByProductCode(searchTerm, searchType);

        if (responseList.size() == 0) {
            responseList = Collections.emptyList();
        }

        LogHelper.log(LogMessage.COMMON_SELECT_SUCCESS,
                new String[] { "商品コード登録", String.valueOf(responseList.size()) });

        return responseList;
    }

    /**
     * 商品コード登録
     * 
     * @param entity 商品コード情報
     */
    @Override
    @Transactional
    public Integer insCode(ProductCodeDto entity, String memberNumber) {
        LogHelper.log(LogMessage.COMMON_INSERT_START, new String[] { "商品コード登録" });

        if (null == entity.getProductCode() || null == entity.getProductName()) {
            LogHelper.log(LogMessage.COMMON_INSERT_FAIL, new String[] { "商品コード登録" });
            throw new InvalidRequestException(ErrorCode.VALIDATION_FIELD_REQUIRED);
        }

        ProductCodeDto responseList = cm901065Mapper.findByOneCode(entity.getProductCode(), 1, null);
        if (responseList != null) {
            LogHelper.log(LogMessage.COMMON_INSERT_FAIL, new String[] { "商品コード登録" });
            throw new InvalidRequestException(ErrorCode.MSG_PRODUCT_REGISTERED);
        }

        responseList = cm901065Mapper.findByOneCode(entity.getProductName().trim(), 2, entity.getProductCategory());
        if (responseList != null) {
            LogHelper.log(LogMessage.COMMON_INSERT_FAIL, new String[] { "商品コード登録" });
            throw new InvalidRequestException(ErrorCode.MSG_PRODUCT_REGISTERED);
        }

        ProductCodeDto productCodeDto = new ProductCodeDto();
        productCodeDto.setProductCode(entity.getProductCode());
        productCodeDto.setProductName(entity.getProductName().trim());
        productCodeDto.setProductCategory(entity.getProductCategory());
        productCodeDto.setCreatedAt(dateCalculator.tokyoTime());
        productCodeDto.setCreatedBy(memberNumber);
        productCodeDto.setUpdatedAt(dateCalculator.tokyoTime());
        productCodeDto.setUpdatedBy(memberNumber);

        return cm901065Mapper.insCode(productCodeDto);
    }

    /**
     * 商品コード削除
     * 
     * @param entity 商品コード情報
     */
    @Override
    @Transactional
    public Integer uptCode(List<ProductCodeDto> entity, String memberNumber) {
        LogHelper.log(LogMessage.COMMON_DELETE_START, new String[] { "商品コード登録" });

        if (entity.size() == 0) {
            LogHelper.log(LogMessage.COMMON_UPDATE_EMPTY, new String[] { "商品コード登録" });
            throw new InvalidRequestException(ErrorCode.VALIDATION_FIELD_REQUIRED);
        }

        int upt = 0;
        for (ProductCodeDto code : entity) {
            ProductCodeDto responseList = cm901065Mapper.findByOneCode(code.getProductCode().trim(), 1,
                    null);
            if (responseList == null) {
                LogHelper.log(LogMessage.COMMON_UPDATE_FAIL, new String[] { "商品コード登録" });
                throw new InvalidRequestException(ErrorCode.UPDATE_FAILED);
            }

            ProductCodeDto response = cm901065Mapper.findByOneProduct(code.getProductCode().trim());
            if (response != null) {
                LogHelper.log(LogMessage.COMMON_UPDATE_FAIL, new String[] { "商品コード登録" });
                throw new InvalidRequestException(ErrorCode.MSG_PRODUCT_REGISTERED);
            }

            response = cm901065Mapper.findByOneStock(code.getProductCode().trim());
            if (response != null) {
                LogHelper.log(LogMessage.COMMON_UPDATE_FAIL, new String[] { "商品コード登録" });
                throw new InvalidRequestException(ErrorCode.MSG_STOCK_EXISTS);
            }

            ProductCodeDto productCodeDto = new ProductCodeDto();
            productCodeDto.setProductCode(code.getProductCode());
            productCodeDto.setProductCategory(code.getProductCategory());
            productCodeDto.setUpdatedAt(dateCalculator.tokyoTime());
            productCodeDto.setUpdatedBy(memberNumber);

            upt = cm901065Mapper.uptCode(productCodeDto);
        }

        return upt;
    }
}
