package com.cloudia.backend.CM_01_1015.mapper;

import com.cloudia.backend.CM_01_1015.model.ReturnResponse;
import com.cloudia.backend.CM_01_1015.model.ReturnRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CM011015Mapper {

        /**
         * ログイン中ユーザーの交換／返品申請履歴一覧を取得
         */
        List<ReturnResponse> getReturnList(@Param("userId") int userId);

        /**
         * 特定の交換／返品申請の詳細情報を取得
         */
        ReturnResponse getReturnDetail(@Param("returnId") int returnId, @Param("userId") int userId);

        /**
         * 申請画面で注文番号選択時、該当注文に含まれる商品一覧を取得
         */
        List<ReturnResponse.ProductInfo> getProductsByOrderNo(@Param("orderNo") String orderNo,
                        @Param("userId") int userId);

        /**
         * 交換／返品申請書のマスター情報を保存
         */
        void insertReturnRequest(@Param("req") ReturnRequest req, @Param("imageUrls") String imageUrls,
                        @Param("userId") int userId, @Param("createdAt") LocalDateTime createdAt);

        /**
         * 交換／返品申請書に含まれる個別商品の詳細情報を保存
         */
        void insertReturnDetail(@Param("productCode") String productCode, @Param("quantity") int quantity,
                        @Param("createdBy") String createdBy);

        /**
         * 注文ステータスを交換／返品関連ステータスに更新
         */
        void updateToExchangeStatus(@Param("orderNo") String orderNo, @Param("memberNumber") String memberNumber);

        /**
         * 現在のセッションで直近に生成された返品IDを取得
         */
        int getCurrentReturnId();

        /**
         * 申請可能な購入確定注文一覧を取得
         */
        List<Map<String, Object>> getReturnableOrders(@Param("userId") int userId);
}