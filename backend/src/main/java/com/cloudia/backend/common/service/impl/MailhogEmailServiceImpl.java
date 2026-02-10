package com.cloudia.backend.common.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudia.backend.common.model.EmailDto;
import com.cloudia.backend.common.service.EmailService;
import com.cloudia.backend.constants.CMMessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
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
        return sendTemplateEmail("BankTransferGuide_jp", emailInfo.getSendEmail(), templateData);
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
        return sendTemplateEmail("OrderConfirmation_jp", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendShippingPreparing(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("orderNumber", emailInfo.getOrderNumber());
        templateData.put("shippingDate", emailInfo.getShippingDate());
        templateData.put("orderItems", emailInfo.getOrderItems());
        return sendTemplateEmail("ShippingPreparing_jp", emailInfo.getSendEmail(), templateData);
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
        return sendTemplateEmail("ShippingCompleted_jp", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendShippingCompleted(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("orderItems", emailInfo.getOrderItems());
        templateData.put("name", emailInfo.getName());
        return sendTemplateEmail("DeliveryCompleted_jp", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendCancel(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("orderNumber", emailInfo.getOrderNumber());
        templateData.put("orderItems", emailInfo.getOrderItems());
        templateData.put("name", emailInfo.getName());
        return sendTemplateEmail("OrderCancelled_jp", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendPaymentDeadlineNotice(EmailDto emailInfo, StringBuilder orders) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("pendingOrders", orders.toString());
        templateData.put("pendingCount", emailInfo.getPendingCount());
        return sendTemplateEmail("PaymentDeadlineNotice_jp", emailInfo.getSendEmails(), templateData);
    }

    @Override
    public String sendVerificationEmail(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("code", emailInfo.getVerificationCode());
        templateData.put("expirationMinutes", "3");
        return sendTemplateEmail("VerificationEmail_jp", emailInfo.getSendEmail(), templateData);
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
        return sendTemplateEmail("FindIdEmail_jp", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendPasswordResetVerificationEmail(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("code", emailInfo.getVerificationCode());
        templateData.put("expirationMinutes", "3");
        return sendTemplateEmail("FindPasswordEmail_jp", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendPasswordChangedNotification(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("userName", emailInfo.getName());
        templateData.put("homePageUrl", appHomepageUrl);
        return sendTemplateEmail("PasswordChangedNotification_jp", emailInfo.getSendEmail(), templateData);
    }

    @Override
    public String sendWithdrawalNotification(EmailDto emailInfo) {
        Map<String, String> templateData = new HashMap<>();
        templateData.put("userName", emailInfo.getName());
        templateData.put("loginId", emailInfo.getLoginId());
        templateData.put("homePageUrl", appHomepageUrl);
        return sendTemplateEmail("WithdrawalEmail_jp", emailInfo.getSendEmail(), templateData);
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
        return sendTemplateEmail("AdminReturnNotice_jp", adminEmails, templateData);
    }

    @Override
    public String sendTemplateEmail(String templateName, String recipientEmail, Map<String, String> templateData) {
        return sendTemplateEmail(templateName, List.of(recipientEmail), templateData);
    }

    @Override
    public String sendTemplateEmail(String templateName, List<String> recipientEmails, Map<String, String> templateData) {
        try {
            log.info(CMMessageConstant.EMAIL_SEND_START, templateName, maskEmailList(recipientEmails));
            TemplateBody templateBody = buildTemplateBody(templateName, templateData);

            for (String to : recipientEmails) {
                sendMail(to, templateBody.subject, templateBody.textBody, templateBody.htmlBody);
            }

            log.info(CMMessageConstant.EMAIL_SEND_SUCCESS, templateName, maskEmailList(recipientEmails));
            return "OK";
        } catch (Exception e) {
            log.error(CMMessageConstant.EMAIL_SEND_FAILED, templateName, maskEmailList(recipientEmails), e);
            return "FAIL";
        }
    }

    private void sendMail(String to, String subject, String textBody, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        boolean multipart = htmlBody != null && !htmlBody.isBlank();
        MimeMessageHelper helper = new MimeMessageHelper(message, multipart, StandardCharsets.UTF_8.name());
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        if (multipart) {
            helper.setText(textBody, htmlBody);
        } else {
            helper.setText(textBody, false);
        }
        mailSender.send(message);
    }

    private TemplateBody buildTemplateBody(String templateName, Map<String, String> templateData) throws Exception {
        String path = "mail-templates/" + templateName + ".json";
        ClassPathResource resource = new ClassPathResource(path);

        if (!resource.exists()) {
            log.warn("메일 템플릿을 찾을 수 없습니다. path={}, templateName={}", path, templateName);
            String dataJson = objectMapper.writeValueAsString(templateData);
            return new TemplateBody(templateName, "Template: " + templateName + "\n\n" + dataJson, null);
        }

        try (InputStream is = resource.getInputStream()) {
            SesTemplateWrapper wrapper = objectMapper.readValue(is, SesTemplateWrapper.class);
            if (wrapper == null || wrapper.Template == null) {
                log.warn("메일 템플릿 JSON 구조가 올바르지 않습니다. path={}, templateName={}", path, templateName);
                String dataJson = objectMapper.writeValueAsString(templateData);
                return new TemplateBody(templateName, "Template: " + templateName + "\n\n" + dataJson, null);
            }

            SesTemplate template = wrapper.Template;
            String subject = replacePlaceholders(
                template.SubjectPart == null || template.SubjectPart.isBlank() ? templateName : template.SubjectPart,
                templateData
            );
            String textBody = replacePlaceholders(
                template.TextPart == null ? "" : template.TextPart,
                templateData
            );
            String htmlBody = replacePlaceholders(template.HtmlPart, templateData);

            return new TemplateBody(subject, textBody, htmlBody);
        }
    }

    private String replacePlaceholders(String raw, Map<String, String> templateData) {
        if (raw == null) {
            return null;
        }
        String rendered = raw;
        for (Map.Entry<String, String> entry : templateData.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            rendered = rendered.replace(key, entry.getValue() == null ? "" : entry.getValue());
        }
        return rendered;
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

    private static class SesTemplateWrapper {
        public SesTemplate Template;
    }

    private static class SesTemplate {
        public String TemplateName;
        public String SubjectPart;
        public String TextPart;
        public String HtmlPart;
    }

    private static class TemplateBody {
        private final String subject;
        private final String textBody;
        private final String htmlBody;

        private TemplateBody(String subject, String textBody, String htmlBody) {
            this.subject = subject;
            this.textBody = textBody;
            this.htmlBody = htmlBody;
        }
    }
}
