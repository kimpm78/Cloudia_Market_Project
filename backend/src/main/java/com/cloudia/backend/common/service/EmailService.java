package com.cloudia.backend.common.service;

import java.util.List;
import java.util.Map;

import com.cloudia.backend.common.model.EmailDto;

public interface EmailService {
    /**
     * 계좌이체 안내 이메일 발송
     */
    String sendBankTransferGuide(EmailDto emailInfo);

    /**
     * 구매 확정 이메일 발송
     */
    String sendOrderConfirmation(EmailDto emailInfo);

    /**
     * 배송 준비중 이메일 발송
     */
    String sendShippingPreparing(EmailDto emailInfo);

    /**
     * 배송중 이메일 발송
     */
    String sendShippingInProgress(EmailDto emailInfo);

    /**
     * 배송 완료 이메일 발송
     */
    String sendShippingCompleted(EmailDto emailInfo);

    /**
     * 주문 취소 이메일 발송
     */
    String sendCancel(EmailDto emailInfo);

    /**
     * 계좌이체 마감 안내 발송
     */
    String sendPaymentDeadlineNotice(EmailDto emailInfo, StringBuilder orders);

    /**
     * 단일 이메일 발송
     */
    String sendTemplateEmail(String templateName, String recipientEmail, Map<String, String> templateData);

    /**
     * 다중 이메일 발송
     */
    String sendTemplateEmail(String templateName, List<String> recipientEmails, Map<String, String> templateData);

    /**
     * Hard Bounce 처리 - 이메일 주소를 DB에서 즉시 삭제
     */
    void handleHardBounce(String email);

    /**
     * Soft Bounce 처리 - 재시도 횟수 증가 및 3회 초과 시 삭제
     */
    void handleSoftBounce(String email);

    /**
     * Complaint(스팸 신고) 처리 - 즉시 구독 취소 및 Suppression List 추가
     */
    void handleComplaint(String email);

    /**
     * 이메일 인증 코드 생성
     */
    String generateVerificationCode();

    /**
     * 인증 코드 이메일 발송
     */
    String sendVerificationEmail(EmailDto emailInfo);

    /**
     * 아이디 찾기 인증 코드 이메일 발송
     */
    String sendFindIdVerificationEmail(EmailDto emailInfo);

    /**
     * 비밀번호 재설정 인증 코드 이메일 발송
     */
    String sendPasswordResetVerificationEmail(EmailDto emailInfo);

    /**
     * 비밀번호 변경 완료 알림 이메일 발송
     */
    String sendPasswordChangedNotification(EmailDto emailInfo);

    /**
     * 회원 탈퇴 완료 알림 이메일 발송
     */
    String sendWithdrawalNotification(EmailDto emailInfo);

    /**
     * 관리자에게 교환/반품 신청 알림 발송
     */
    String sendAdminReturnNotification(List<String> adminEmails, Map<String, String> templateData);
}
