package com.cloudia.backend.CM_01_1015.service;

import com.cloudia.backend.CM_01_1015.model.ReturnResponse;
import com.cloudia.backend.CM_01_1015.model.ReturnRequest;
import com.cloudia.backend.common.model.ResponseModel;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface CM011015Service {

    /**
     * 交換・返品履歴取得
     */
    ResponseModel<List<ReturnResponse>> getReturnHistory(String loginId);

    /**
     * 交換・返品申請
     */
    ResponseEntity<ResponseModel<Object>> createReturnRequest(String loginId, ReturnRequest request);

    /**
     * 詳細取得
     */
    ResponseModel<ReturnResponse> getReturnDetail(String loginId, int returnId);

    /**
     * 注文別商品一覧取得
     */
    ResponseEntity<List<ReturnResponse.ProductInfo>> getOrderProducts(String loginId, String orderNo);

    /**
     * 申請可能な購入確定注文一覧取得
     */
    ResponseModel<List<Map<String, Object>>> getReturnableOrderList(String loginId);
}