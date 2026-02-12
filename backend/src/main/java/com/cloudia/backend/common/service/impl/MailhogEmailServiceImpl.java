package com.cloudia.backend.common.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        templateData.put("name", emailInfo.getName());
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
        log.warn("Hard Bounce 発生 - メールアドレス無効: {}", maskEmail(email));
    }

    @Override
    public void handleSoftBounce(String email) {
        log.warn("Soft Bounce 発生 - 一時的な送信失敗: {}", maskEmail(email));
    }

    @Override
    public void handleComplaint(String email) {
        log.warn("Complaint（スパム報告）発生 - メール: {}", maskEmail(email));
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
        log.info("メール本文形式: multipart={}, subject={}, to={}", multipart, subject, maskEmail(to));
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
        JsonNode templateNode = readTemplateNode(path);
        if (templateNode == null) {
            log.warn("メールテンプレートが見つかりません。 path={}, templateName={}", path, templateName);
            String dataJson = objectMapper.writeValueAsString(templateData);
            return new TemplateBody(templateName, "Template: " + templateName + "\n\n" + dataJson, null);
        }

        String subjectRaw = templateNode.path("SubjectPart").asText(templateName);
        if (subjectRaw == null || subjectRaw.isBlank()) {
            subjectRaw = templateName;
        }
        String textRaw = templateNode.path("TextPart").isMissingNode() || templateNode.path("TextPart").isNull()
                ? ""
                : templateNode.path("TextPart").asText("");
        String htmlRaw = templateNode.path("HtmlPart").isMissingNode() || templateNode.path("HtmlPart").isNull()
                ? null
                : templateNode.path("HtmlPart").asText(null);

        String subject = replacePlaceholders(subjectRaw, templateData);
        String textBody = replacePlaceholders(textRaw, templateData);
        String htmlBody = replacePlaceholders(htmlRaw, templateData);
        return new TemplateBody(subject, textBody, htmlBody);
    }

    private JsonNode readTemplateNode(String path) {
        JsonNode classPathNode = readTemplateFromClassPath(path);
        if (classPathNode != null) {
            return classPathNode;
        }

        JsonNode appPathNode = readTemplateFromFileSystem(Paths.get("/app", path));
        if (appPathNode != null) {
            return appPathNode;
        }

        JsonNode srcPathNode = readTemplateFromFileSystem(Paths.get("src/main/resources", path));
        if (srcPathNode != null) {
            return srcPathNode;
        }

        JsonNode backendSrcPathNode = readTemplateFromFileSystem(Paths.get("backend/src/main/resources", path));
        if (backendSrcPathNode != null) {
            return backendSrcPathNode;
        }

        return null;
    }

    private JsonNode readTemplateFromClassPath(String path) {
        String[] candidates = {
                path,
                "/" + path,
                "BOOT-INF/classes/" + path,
                "/BOOT-INF/classes/" + path
        };

        for (String candidate : candidates) {
            ClassPathResource resource = new ClassPathResource(candidate);
            try (InputStream is = resource.getInputStream()) {
                return parseTemplateNode(is, "classpath:" + candidate);
            } catch (IOException ignored) {
                // 다음 candidate 경로를 시도
            }

            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(candidate)) {
                if (is != null) {
                    return parseTemplateNode(is, "contextClassLoader:" + candidate);
                }
            } catch (IOException ignored) {
                // 다음 candidate 경로를 시도
            }

            try (InputStream is = MailhogEmailServiceImpl.class.getClassLoader().getResourceAsStream(candidate)) {
                if (is != null) {
                    return parseTemplateNode(is, "classLoader:" + candidate);
                }
            } catch (IOException ignored) {
                // 다음 candidate 경로를 시도
            }
        }

        log.warn("クラスパステンプレート読み込み失敗。 path={}", path);
        return null;
    }

    private JsonNode readTemplateFromFileSystem(Path filePath) {
        if (!Files.exists(filePath)) {
            return null;
        }
        try (InputStream is = Files.newInputStream(filePath)) {
            return parseTemplateNode(is, "file:" + filePath);
        } catch (IOException e) {
            log.warn("ファイルテンプレート読み込み失敗。 path={}, error={}", filePath, e.getMessage());
            return null;
        }
    }

    private JsonNode parseTemplateNode(InputStream is, String source) throws IOException {
        byte[] bytes = is.readAllBytes();
        String json = new String(bytes, StandardCharsets.UTF_8);
        if (json.startsWith("\uFEFF")) {
            json = json.substring(1);
        }
        JsonNode root = objectMapper.readTree(json);
        JsonNode templateNode = root.path("Template");
        if (templateNode.isMissingNode() || templateNode.isNull()) {
            log.warn("テンプレートJSON構造が不正です。 source={}", source);
            return null;
        }
        log.info("メールテンプレート読み込み成功。 source={}", source);
        return templateNode;
    }

    private String replacePlaceholders(String raw, Map<String, String> templateData) {
        if (raw == null) {
            return null;
        }
        if (templateData == null || templateData.isEmpty()) {
            return raw;
        }
        String rendered = raw;
        for (Map.Entry<String, String> entry : templateData.entrySet()) {
            String key = "{{" + entry.getKey() + "}}";
            rendered = rendered.replace(key, entry.getValue() == null ? "" : entry.getValue());
        }
        rendered = rendered.replaceAll("\\{\\{[^}]+\\}\\}", "");
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
