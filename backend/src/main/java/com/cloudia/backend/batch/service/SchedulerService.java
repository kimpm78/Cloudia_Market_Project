package com.cloudia.backend.batch.service;

public interface SchedulerService {
    /**
     * 予約締切商品のステータス更新
     */
    void updateResv();

    /**
     * 銀行振込の支払期限確認メール
     */
    void sendPaymentDeadline();

    /**
     * 翌年の祝日をDBに保存
     */
    void syncNextYearHolidays();

    /**
     * 発送完了案内メッセージ
     */
    void confirmDelivery();
}