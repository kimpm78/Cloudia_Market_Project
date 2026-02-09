package com.cloudia.backend.batch.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cloudia.backend.batch.model.HolidayDto;
import com.cloudia.backend.batch.model.OrdersDto;
import com.cloudia.backend.batch.model.SendEmail;

@Mapper
public interface SchedulerMapper {
    /**
     * 予約締切商品のステータス更新予定件数
     *
     * @return 更新予定件数
     */
    int updateProductCount(@Param("resvDay") String resvDay);

    /**
     * 予約締切商品のステータス更新
     *
     * @return 更新件数
     */
    int updateProduct(@Param("resvDay") String resvDay, @Param("updDay") LocalDateTime updDay);

    /**
     * 銀行振込の支払期限案内メールの送信予定件数
     */
    int sendPaymentDeadlineNoticeCount(@Param("endDay") String endDay);

    /**
     * 銀行振込の支払期限案内メールの送信
     */
    List<OrdersDto> sendPaymentDeadlineNotice(@Param("endDay") String endDay);

    /**
     * 送信対象メール（管理者、マネージャー）
     */
    List<SendEmail> sendEmails();

    /**
     * 祝日を保存
     */
    void insertHolidayBatch(List<HolidayDto> holidays);

    /**
     * 発送完了案内メッセージの送信予定件数
     */
    int sendConfirmDeliveryCont(@Param("endDay") String endDay);

    /**
     * 発送完了案内メッセージの送信
     */
    List<OrdersDto> sendConfirmDelivery(@Param("endDay") String endDay);
}