package com.cloudia.backend.common.service;

import java.util.List;
import java.util.Map;

import com.cloudia.backend.common.model.EmailDto;

public interface EmailService {
    /**
     * 銀行振込案内メール送信
     */
    String sendBankTransferGuide(EmailDto emailInfo);

    /**
     * 購入確定メール送信
     */
    String sendOrderConfirmation(EmailDto emailInfo);

    /**
     * 発送準備中メール送信
     */
    String sendShippingPreparing(EmailDto emailInfo);

    /**
     * 配送中メール送信
     */
    String sendShippingInProgress(EmailDto emailInfo);

    /**
     * 配送完了メール送信
     */
    String sendShippingCompleted(EmailDto emailInfo);

    /**
     * 注文キャンセルメール送信
     */
    String sendCancel(EmailDto emailInfo);

    /**
     * 銀行振込期限案内送信
     */
    String sendPaymentDeadlineNotice(EmailDto emailInfo, StringBuilder orders);

    /**
     * 単一メール送信
     */
    String sendTemplateEmail(String templateName, String recipientEmail, Map<String, String> templateData);

    /**
     * 複数メール送信
     */
    String sendTemplateEmail(String templateName, List<String> recipientEmails, Map<String, String> templateData);

    /**
     * Hard Bounce 対応 - メールアドレスをDBから即時削除
     */
    void handleHardBounce(String email);

    /**
     * Soft Bounce 対応 - 再試行回数を増加し、3回超過で削除
     */
    void handleSoftBounce(String email);

    /**
     * Complaint（スパム報告）対応 - 即時購読解除およびSuppression Listへ追加
     */
    void handleComplaint(String email);

    /**
     * メール認証コード生成
     */
    String generateVerificationCode();

    /**
     * 認証コードメール送信
     */
    String sendVerificationEmail(EmailDto emailInfo);

    /**
     * ID検索用認証コードメール送信
     */
    String sendFindIdVerificationEmail(EmailDto emailInfo);

    /**
     * パスワード再設定用認証コードメール送信
     */
    String sendPasswordResetVerificationEmail(EmailDto emailInfo);

    /**
     * パスワード変更完了通知メール送信
     */
    String sendPasswordChangedNotification(EmailDto emailInfo);

    /**
     * 退会完了通知メール送信
     */
    String sendWithdrawalNotification(EmailDto emailInfo);

    /**
     * 管理者へ交換／返品申請通知送信
     */
    String sendAdminReturnNotification(List<String> adminEmails, Map<String, String> templateData);
}
