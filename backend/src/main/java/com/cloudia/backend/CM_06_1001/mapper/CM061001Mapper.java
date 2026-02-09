package com.cloudia.backend.CM_06_1001.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

import com.cloudia.backend.CM_06_1001.model.OrderInfo;
import com.cloudia.backend.CM_06_1001.model.OrderItemInfo;
import com.cloudia.backend.CM_06_1001.model.PaymentInfo;
import com.cloudia.backend.CM_06_1001.model.OrderSummary;
import com.cloudia.backend.CM_06_1000.model.CartItemResponse;

@Mapper
public interface CM061001Mapper {

   /**
    * 注文番号シーケンス生成
    *
    * @return 生成された注文番号
    */
   String selectNextOrderNumber();

   /**
    * 会員別の注文番号生成（00001から開始）
    *
    * @param memberNumber 会員番号
    * @return 生成された注文番号
    */
   String selectNextOrderNumberByMember(@Param("memberNumber") String memberNumber);

   /**
    * 注文マスター保存
    *
    * @param order 注文情報オブジェクト
    * @return 保存件数
    */
   int createOrder(OrderInfo order);

   /**
    * 注文詳細保存
    *
    * @param item 注文詳細情報オブジェクト
    * @return 保存件数
    */
   int insertOrderItem(OrderItemInfo item);

   /**
    * カート → 注文生成時のスナップショット取得
    *
    * @param userId      会員ID
    * @param cartItemIds カートアイテムIDリスト
    * @return カートアイテム情報リスト
    */
   List<CartItemResponse> selectCartItemsForOrder(
      @Param("userId") Long userId,
      @Param("cartItemIds") List<Long> cartItemIds
   );

   /**
    * 注文後にカートを無効化
    *
    * @param cartItemIds カートアイテムIDリスト
    * @param updatedBy  更新者
    * @return 更新件数
    */
   int deactivateCartItems(
      @Param("cartItemIds") List<Long> cartItemIds,
      @Param("updatedBy") String updatedBy
   );

   /**
    * 在庫減算（available_qty >= quantity 条件）
    *
    * @param productId 商品コード
    * @param quantity  減算数量
    * @return 更新件数
    */
   int decreaseStock(
      @Param("productId") String productId,
      @Param("quantity") Integer quantity
   );

   /**
    * 在庫ID取得（stocks.stock_id）
    *
    * @param productId 商品コード
    * @return 在庫ID
    */
   Long findStockIdByProductCode(@Param("productId") String productId);

   /**
    * 在庫変動履歴（stock_details）保存
    *
    * @param stockId   在庫ID
    * @param qty       変動数量
    * @param reason    変動理由
    * @param createdBy 作成者
    * @param updatedBy 更新者
    * @return 保存件数
    */
   int insertStockDetail(
      @Param("stockId") Long stockId,
      @Param("qty") Long qty,
      @Param("reason") String reason,
      @Param("createdBy") String createdBy,
      @Param("updatedBy") String updatedBy
   );

   /**
    * 注文の所有者（member_number）取得
    *
    * @param orderId 注文ID
    * @return 会員番号
    */
   String findOrderOwner(@Param("orderId") Long orderId);

   /**
    * 会員メールアドレス取得
    *
    * @param memberNumber 会員番号
    * @return 会員メールアドレス
    */
   String findEmailByMemberNumber(@Param("memberNumber") String memberNumber);

   /**
    * 注文の所有者（user_id）取得
    *
    * @param orderId 注文ID
    * @return 会員ユーザーID
    */
   String findOrderUserId(@Param("orderId") Long orderId);

   /**
    * 注文サマリー情報取得
    *
    * @param orderId 注文ID
    * @return 注文サマリー情報オブジェクト
    */
   OrderSummary findOrderSummary(@Param("orderId") Long orderId);

   /**
    * 注文詳細取得
    *
    * @param orderId 注文ID
    * @return 注文詳細情報リスト
    */
   List<OrderItemInfo> selectOrderItemsByOrderId(@Param("orderId") Long orderId);

   /**
    * 決済 insert
    *
    * @param payment 決済情報オブジェクト
    * @return 保存件数
    */
   int insertPayment(PaymentInfo payment);

   /**
    * 決済 update
    *
    * @param payment 決済情報オブジェクト
    * @return 更新件数
    */
   int updatePayment(PaymentInfo payment);

   /**
    * 決済TID更新
    *
    * @param paymentId      決済ID
    * @param transactionId  取引TID
    * @return 更新件数
    */
   int updatePaymentTransactionId(
      @Param("paymentId") String paymentId,
      @Param("transactionId") String transactionId
   );

   /**
    * 注文ステータス = 決済完了（購入確定）
    *
    * @param orderId         注文ID
    * @param refundDeadline  返金期限
    */
   void updateOrderStatusToCompleted(
      @Param("orderId") Long orderId,
      @Param("refundDeadline") String refundDeadline
   );

   /**
    * 最新決済取得（orderNumber基準）
    *
    * @param orderNumber 注文番号
    * @return 決済情報オブジェクト
    */
   PaymentInfo findLatestPayment(@Param("orderNumber") String orderNumber);

   /**
    * 最新決済取得（orderId基準）
    *
    * @param orderId 注文ID
    * @return 決済情報オブジェクト
    */
   PaymentInfo findLatestPaymentByOrderId(@Param("orderId") Long orderId);

   /**
    * 最新決済取得（transactionId/TID基準）
    *
    * @param transactionId PG取引番号（TID）
    * @return 決済情報オブジェクト
    */
   PaymentInfo findLatestPaymentByTransactionId(@Param("transactionId") String transactionId);

   /**
    * 配送先住所が本人所有か検証（有効データのみ）
    *
    * @param memberNumber 会員番号
    * @param addressId    住所ID
    * @return 存在有無
    */
   boolean existsActiveDeliveryAddress(
      @Param("memberNumber") String memberNumber,
      @Param("addressId") Long addressId
   );

   /**
    * PaymentServiceImpl から呼び出すメソッド（4件）
    */

   /**
    * PG承認 成功/失敗 → 決済ステータス更新
    *
    * @param paymentId     決済ID
    * @param orderId       注文ID
    * @param transactionId 取引TID
    * @param resultCode    結果コード
    * @param resultMsg     結果メッセージ
    * @param statusCode    ステータスコード
    * @param approvedAt    承認日時
    * @param updatedBy     更新者
    * @return 更新件数
    */
   int updatePaymentStatusOnApprove(
      @Param("paymentId") String paymentId,
      @Param("orderId") Long orderId,
      @Param("transactionId") String transactionId,
      @Param("resultCode") String resultCode,
      @Param("resultMsg") String resultMsg,
      @Param("statusCode") String statusCode,
      @Param("approvedAt") String approvedAt,
      @Param("updatedBy") String updatedBy
   );

   /**
    * PG承認 成功/失敗 → 注文ステータス変更
    *
    * @param orderId      注文ID
    * @param statusValue  ステータス値
    * @param memo         メモ
    * @param updatedBy    更新者
    * @return 更新件数
    */
   int updateOrderStatusOnApprove(
      @Param("orderId") Long orderId,
      @Param("statusValue") Integer statusValue,
      @Param("memo") String memo,
      @Param("updatedBy") String updatedBy
   );

   /**
    * PG承認成功時に純利益を更新
    *
    * @param orderId   注文ID
    * @param updatedBy 更新者
    * @return 更新件数
    */
   int updateOrderNetProfit(
      @Param("orderId") Long orderId,
      @Param("updatedBy") String updatedBy
   );

   /**
    * PGキャンセル → 決済ステータス変更
    *
    * @param paymentId  決済ID
    * @param resultCode 結果コード
    * @param resultMsg  結果メッセージ
    * @param updatedBy  更新者
    * @return 更新件数
    */
   int updatePaymentStatusOnCancel(
      @Param("paymentId") String paymentId,
      @Param("resultCode") String resultCode,
      @Param("resultMsg") String resultMsg,
      @Param("updatedBy") String updatedBy
   );

   /**
    * 失敗/クローズ処理: 決済ステータスを失敗に変更
    *
    * @param paymentId     決済ID
    * @param transactionId 取引TID（あれば保存）
    * @param resultCode    結果コード
    * @param resultMsg     結果メッセージ
    * @param updatedBy     更新者
    * @param updatedAt     更新日時
    * @return 更新件数
    */
   int updatePaymentStatusToFailed(
      @Param("paymentId") String paymentId,
      @Param("transactionId") String transactionId,
      @Param("resultCode") String resultCode,
      @Param("resultMsg") String resultMsg,
      @Param("updatedBy") String updatedBy,
      @Param("updatedAt") LocalDateTime updatedAt
   );

   /**
    * 注文決済成功時にカートを無効化（order基準）
    *
    * @param orderId    注文ID
    * @param updatedBy  更新者
    * @return 更新件数
    */
   int deactivateCartItemsByOrderId(
      @Param("orderId") Long orderId,
      @Param("updatedBy") String updatedBy
   );

   /**
    * PGキャンセル > 注文ステータス変更
    *
    * @param orderId     注文ID
    * @param statusValue ステータス値
    * @return 更新件数
    */
   int updateOrderStatusOnCancel(
      @Param("orderId") Long orderId,
      @Param("statusValue") Integer statusValue
   );

   /**
    * 注文商品リスト取得
    *
    * @param orderId 注文ID
    * @return 注文商品リスト
    */
   List<OrderItemInfo> findOrderItems(@Param("orderId") Long orderId);
}
