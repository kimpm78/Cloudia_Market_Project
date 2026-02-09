package com.cloudia.backend.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.constants.CMMessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
@Slf4j
public class MailhogEmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cloudia.email.from}")
    private String fromEmail;

    @Value("${app.homepage.url}")
    private String appHomepageUrl;

    public MailhogEmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String sendBankTransferGuide(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("name", emailInfo.getName());
        templateData.put("orderDate", emailInfo.getOrderDate());
        templateData.put("dueDate", emailInfo.getDueDate());
        return sendTemplateEmail("BankTransferGuide_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendOrderConfirmation(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("orderDate", emailInfo.getOrderDate());
        templateData.put("orderNumber", emailInfo.getOrderNumber());
        templateData.put("paymentMethod", emailInfo.getPaymentMethod());
        templateData.put("paymentAmount", emailInfo.getPaymentAmount());
        templateData.put("orderItems", emailInfo.getOrderItems());
        templateData.put("name", emailInfo.getName());
        return sendTemplateEmail("OrderConfirmation_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendShippingPreparing(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("orderNumber", emailInfo.getOrderNumber());
        templateData.put("shippingDate", emailInfo.getShippingDate());
        templateData.put("orderItems", emailInfo.getOrderItems());
        return sendTemplateEmail("ShippingPreparing_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendShippingInProgress(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("trackingNumber", emailInfo.getTrackingNumber());
        templateData.put("orderDate", emailInfo.getOrderDate());
        templateData.put("orderNumber", emailInfo.getOrderNumber());
        templateData.put("paymentMethod", emailInfo.getPaymentMethod());
        templateData.put("orderItems", emailInfo.getOrderItems());
        templateData.put("name", emailInfo.getName());
        return sendTemplateEmail("ShippingCompleted_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendShippingCompleted(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("orderItems", emailInfo.getOrderItems());
        templateData.put("name", emailInfo.getName());
        return sendTemplateEmail("DeliveryCompleted_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendCancel(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("orderNumber", emailInfo.getOrderNumber());
        templateData.put("orderItems", emailInfo.getOrderItems());
        templateData.put("name", emailInfo.getName());
        return sendTemplateEmail("OrderCancelled_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendPaymentDeadlineNotice(EmailDto emailInfo, StringBuilder orders) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("pendingOrders", orders.toString());
        templateData.put("pendingCount", emailInfo.getPendingCount());
        return sendTemplateEmail("PaymentDeadlineNotice_kr", emailInfo.getSendEmails(), templateData);
    }

    @Override
    public String sendVerificationEmail(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("code", emailInfo.getVerificationCode());
        templateData.put("expirationMinutes", "3");
        return sendTemplateEmail("VerificationEmail_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Override
    public String sendFindIdVerificationEmail(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("code", emailInfo.getVerificationCode());
        templateData.put("expirationMinutes", "3");
        return sendTemplateEmail("FindIdEmail_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendPasswordResetVerificationEmail(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("code", emailInfo.getVerificationCode());
        templateData.put("expirationMinutes", "3");
        return sendTemplateEmail("FindPasswordEmail_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendPasswordChangedNotification(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("userName", emailInfo.getName());
        templateData.put("homePageUrl", appHomepageUrl);
        return sendTemplateEmail("PasswordChangedNotification_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendWithdrawalNotification(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("userName", emailInfo.getName());
        templateData.put("loginId", emailInfo.getLoginId());
        templateData.put("homePageUrl", appHomepageUrl);
        return sendTemplateEmail("WithdrawalEmail_kr", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public void handleHardBounce(String email) {
        log.warn("Hard Bounce 발생 - 이메일 주소 무효: {}", maskEmail(email));
    }

    @Override
    public void handleSoftBounce(String email) {
        log.warn("Soft Bounce 발생 - 일시적 전송 실패: {}", maskEmail(email));
    }

    @Override
    public void handleComplaint(String email) {
        log.warn("Complaint(스팸 신고) 발생 - 이메일: {}", maskEmail(email));
    }

    @Override
    public String sendAdminReturnNotification(List<String> adminEmails, Map<String, String> templateData) {
        return sendTemplateEmail("AdminReturnNotice_kr", adminEmails, templateData);
    }

    @Override
    public String sendTemplateEmail(String templateName, String recipientEmail, Map<String, String> templateData) {
        return sendTemplateEmail(templateName, List.of(recipientEmail), templateData);
    }

    @Override
    public String sendTemplateEmail(String templateName, List<String> recipientEmails, Map<String, String> templateData) {
        try {
            log.info(CMMessageConstant.EMAIL_SEND_START, templateName, maskEmailList(recipientEmails));
            String body = buildBody(templateName, templateData);

            for (String to : recipientEmails) {
                sendMail(to, templateName, body);
            }

            log.info(CMMessageConstant.EMAIL_SEND_SUCCESS, templateName, maskEmailList(recipientEmails));
            return "OK";
        } catch (Exception e) {
            log.error(CMMessageConstant.EMAIL_SEND_FAILED, templateName, maskEmailList(recipientEmails), e);
            return "FAIL";
        }
    }

    private void sendMail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, false);
        mailSender.send(message);
    }

    private String buildBody(String templateName, Map<String, String> templateData) throws Exception {
        String dataJson = objectMapper.writeValueAsString(templateData);
        return "Template: " + templateName + "\n\n" + dataJson;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        String maskedLocal = local.length() <= 2 ? local.charAt(0) + "*" : local.substring(0, 2) + "***";
        return maskedLocal + "@" + domain;
    }

    private String maskEmailList(List<String> emails) {
        return emails.stream().map(this::maskEmail).reduce((a, b) -> a + "," + b).orElse("");
    }
}
