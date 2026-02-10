package com.cloudia.backend.CM_90_1051.service;

import java.util.List;

import com.cloudia.backend.CM_90_1051.model.AddressDto;
import com.cloudia.backend.CM_90_1051.model.OrderDetailDto;
import com.cloudia.backend.CM_90_1051.model.OrderDto;
import com.cloudia.backend.CM_90_1051.model.SearchRequestDto;

public interface CM901051Service {
    /**
     * 注文全件一覧取得
     * 
     * @return 注文全件一覧
     */
    List<OrderDto> findByAllOrders();

    /**
     * 条件指定注文一覧取得
     * 
     * @return 注文一覧
     */
    List<OrderDto> getFindOrders(SearchRequestDto searchRequest);

    /**
     * 注文詳細一覧取得
     * 
     * @return 注文詳細一覧
     */
    List<OrderDetailDto> getFindOrderDetail(SearchRequestDto searchRequest);

    /**
     * 精算ステータス更新
     * 
     * @return 更新結果
     */
    Integer uptStatus(SearchRequestDto searchRequest, String userId);

    /**
     * 配送先情報取得
     * 
     * @return 配送先情報
     */
    AddressDto getAddress(SearchRequestDto searchRequest);
}
