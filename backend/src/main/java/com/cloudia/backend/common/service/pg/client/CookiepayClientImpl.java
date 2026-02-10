package com.cloudia.backend.common.service.pg.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cloudia.backend.common.model.pg.PGReadyRequest;
import com.cloudia.backend.common.model.pg.PGResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookiepayClientImpl {

    private final RestTemplate cookiepayRestTemplate;
    private final ObjectMapper objectMapper;

    /** Cookiepay 加盟店情報 */
    @Value("${cookiepay.store-id}")
    private String storeId;

    /** Cookiepay シークレットキー */
    @Value("${cookiepay.secret-key}")
    private String secretKey;

    /** Cookiepay 決済準備 URL */
    @Value("${cookiepay.ready-url}")
    private String readyUrl;

    /** Cookiepay 決済認証 URL */
    @Value("${cookiepay.paycert-url}")
    private String payCertUrl;

    /** Cookiepay トークン発行 URL */
    @Value("${cookiepay.token-url}")
    private String tokenUrl;

    /** Cookiepay 復号 URL */
    @Value("${cookiepay.decrypt-url}")
    private String decryptUrl;

    /** Cookiepay 決済取消 URL */
    @Value("${cookiepay.cancel-url}")
    private String cancelUrl;

    /** Cookiepay 決済履歴照会 URL */
    @Value("${cookiepay.paylist-url}")
    private String paylistUrl;

    /**
     * 共通 POST リクエストメソッド
     * 
     * @param url
     * @param payload
     */
    private PGResult post(String url, Map<String, Object> payload) {
        Map<String, Object> requestBody = (payload == null) ? java.util.Collections.emptyMap()
                : new java.util.HashMap<>(payload);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("[CookiePay Request] URL={} payload={}", url, requestBody);

            ResponseEntity<String> response = cookiepayRestTemplate.exchange(url, HttpMethod.POST, entity,
                    String.class);

            String body = response.getBody();

            if (body == null) {
                return PGResult.builder()
                        .resultCode("9999")
                        .resultMsg("PG 통신 오류: empty response body")
                        .build();
            }

            String trimmed = body.trim();
            log.info("[CookiePay Response] URL={} body={}", url, trimmed);
            if (trimmed.startsWith("<")) {
                return PGResult.builder()
                        .resultCode("HTML")
                        .resultMsg("HTML_READY")
                        .html(trimmed)
                        .build();
            }

            return objectMapper.readValue(trimmed, PGResult.class);

        } catch (Exception e) {
            log.error("[CookiePay API ERROR] URL={}", url, e);

            return PGResult.builder()
                    .resultCode("9999")
                    .resultMsg("PG 통신 오류: " + e.getMessage())
                    .build();
        }
    }

    private PGResult postWithToken(String url, Map<String, Object> payload) {
        String token = requestToken();
        if (token == null || token.isBlank()) {
            return PGResult.builder()
                    .resultCode("TOKEN")
                    .resultMsg("PG token 발급 실패")
                    .build();
        }

        Map<String, Object> requestBody = (payload == null) ? java.util.Collections.emptyMap()
                : new java.util.HashMap<>(payload);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("TOKEN", token);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("[CookiePay Request] URL={} payload={}", url, requestBody);

            ResponseEntity<String> response = cookiepayRestTemplate.exchange(url, HttpMethod.POST, entity,
                    String.class);

            String body = response.getBody();

            if (body == null) {
                return PGResult.builder()
                        .resultCode("9999")
                        .resultMsg("PG 통신 오류: empty response body")
                        .build();
            }

            String trimmed = body.trim();
            log.info("[CookiePay Response] URL={} body={}", url, trimmed);
            if (trimmed.startsWith("<")) {
                return PGResult.builder()
                        .resultCode("HTML")
                        .resultMsg("HTML_READY")
                        .html(trimmed)
                        .build();
            }

            return objectMapper.readValue(trimmed, PGResult.class);

        } catch (Exception e) {
            log.error("[CookiePay API ERROR] URL={}", url, e);

            return PGResult.builder()
                    .resultCode("9999")
                    .resultMsg("PG 통신 오류: " + e.getMessage())
                    .build();
        }
    }

    private String requestToken() {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("pay2_id", storeId);
        payload.put("pay2_key", secretKey);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            log.info("[CookiePay Token Request] URL={} payload={}", tokenUrl, payload);

            ResponseEntity<String> response = cookiepayRestTemplate.exchange(tokenUrl, HttpMethod.POST, entity,
                    String.class);

            String body = response.getBody();
            if (body == null) {
                log.warn("[CookiePay Token Response] empty body");
                return null;
            }

            String trimmed = body.trim();
            log.info("[CookiePay Token Response] URL={} body={}", tokenUrl, trimmed);
            if (trimmed.startsWith("<")) {
                return null;
            }

            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(trimmed);
            if (node == null) {
                return null;
            }
            if (node.hasNonNull("TOKEN")) {
                return node.get("TOKEN").asText();
            }
            if (node.hasNonNull("token")) {
                return node.get("token").asText();
            }
            if (node.hasNonNull("access_token")) {
                return node.get("access_token").asText();
            }
            return null;
        } catch (Exception e) {
            log.error("[CookiePay Token ERROR] URL={}", tokenUrl, e);
            return null;
        }
    }

    private PGResult get(String url, Map<String, Object> params) {
        Map<String, Object> query = (params == null) ? java.util.Collections.emptyMap()
                : new java.util.HashMap<>(params);

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
            for (Map.Entry<String, Object> entry : query.entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
            String builtUrl = builder.toUriString();

            log.info("[CookiePay Request] URL={} payload={}", builtUrl, query);

            HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());
            ResponseEntity<String> response = cookiepayRestTemplate.exchange(builtUrl, HttpMethod.GET, entity,
                    String.class);

            String body = response.getBody();

            if (body == null) {
                return PGResult.builder()
                        .resultCode("9999")
                        .resultMsg("PG 통신 오류: empty response body")
                        .build();
            }

            String trimmed = body.trim();
            log.info("[CookiePay Response] URL={} body={}", builtUrl, trimmed);
            if (trimmed.startsWith("<")) {
                return PGResult.builder()
                        .resultCode("HTML")
                        .resultMsg("HTML_READY")
                        .html(trimmed)
                        .build();
            }

            return objectMapper.readValue(trimmed, PGResult.class);

        } catch (Exception e) {
            log.error("[CookiePay API ERROR] URL={}", url, e);

            return PGResult.builder()
                    .resultCode("9999")
                    .resultMsg("PG 통신 오류: " + e.getMessage())
                    .build();
        }
    }

    /**
     * READY (UI Script 포함)
     *
     * @param req
     * @param payload
     */
    public PGResult ready(PGReadyRequest req, Map<String, Object> payload) {
        Map<String, Object> body = withApiId(payload);

        PGResult result = post(readyUrl, body);
        result.setOrderNo(req.getOrderNo());

        String scripts = buildReadyScripts();
        result.setPgScripts(scripts);

        String html = result.getHtml();
        if (html == null || html.isBlank()) {
            return result;
        }

        html = replaceReturnUrlInputValue(html, req.getReturnUrl());
        result.setHtml(ensureFullHtml(html, scripts));
        return result;
    }

    private String resolveCookiepayDomain() {
        if (readyUrl == null || readyUrl.isBlank()) {
            throw new IllegalStateException("[CookiePay] readyUrl is empty");
        }

        java.net.URI uri = java.net.URI.create(readyUrl);
        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalStateException("[CookiePay] Invalid readyUrl: " + readyUrl);
        }

        return uri.getScheme() + "://" + uri.getHost();
    }

    private String ensureFullHtml(String html, String scripts) {
        String trimmed = html == null ? "" : html.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        String lower = trimmed.toLowerCase();
        if (lower.contains("<html")) {
            int headIdx = lower.indexOf("<head");
            if (headIdx >= 0) {
                int headClose = lower.indexOf(">", headIdx);
                if (headClose >= 0) {
                    return trimmed.substring(0, headClose + 1)
                            + "\n" + scripts + "\n"
                            + trimmed.substring(headClose + 1);
                }
            }
            return scripts + "\n" + trimmed;
        }

        return "<!doctype html><html><head><meta charset=\"utf-8\"/>\n"
                + scripts
                + "\n</head><body>\n"
                + trimmed
                + "\n</body></html>";
    }

    private String replaceReturnUrlInputValue(String html, String returnUrl) {
        if (html == null || html.isBlank() || returnUrl == null || returnUrl.isBlank()) {
            return html;
        }

        String safeReturnUrl = java.util.regex.Matcher.quoteReplacement(returnUrl);
        String pattern = "(?i)(<input[^>]*(?:id|name)=[\"']returnurl[\"'][^>]*value=[\"'])([^\"']*)([\"'])";
        return html.replaceAll(pattern, "$1" + safeReturnUrl + "$3");
    }

    /**
     * 認証情報を追加
     * 
     * @param payload
     * @return
     */
    private Map<String, Object> withAuth(Map<String, Object> payload) {
        Map<String, Object> body = (payload == null) ? new java.util.HashMap<>() : new java.util.HashMap<>(payload);
        body.put("API_ID", storeId);
        body.put("SECRET_KEY", secretKey);
        return body;
    }

    private Map<String, Object> withApiId(Map<String, Object> payload) {
        Map<String, Object> body = (payload == null) ? new java.util.HashMap<>() : new java.util.HashMap<>(payload);
        body.put("API_ID", storeId);
        return body;
    }

    private String buildReadyScripts() {
        String jquery = "<script src=\"https://code.jquery.com/jquery-1.12.4.min.js\"></script>";
        String cookiepayDomain = resolveCookiepayDomain();
        String cookiepayJs = "<script src=\"" + cookiepayDomain + "/js/cookiepayments-1.1.4.js\"></script>";
        return jquery + "\n" + cookiepayJs;
    }

    private PGResult call(String url, Map<String, Object> payload) {
        return post(url, withAuth(payload));
    }

    /**
     * 承認
     * 
     * @param payload
     * @return
     */
    public PGResult approve(Map<String, Object> payload) {
        return postWithToken(payCertUrl, payload);
    }

    /**
     * 取消
     * 
     * @param payload
     * @return
     */
    public PGResult cancel(Map<String, Object> payload) {
        return postWithToken(cancelUrl, payload);
    }

    /**
     * 復号
     * 
     * @param payload
     * @return
     */
    public PGResult decrypt(Map<String, Object> payload) {
        return call(decryptUrl, payload);
    }

    /**
     * 決済認証
     * 
     * @param payload
     * @return
     */
    public PGResult payCert(Map<String, Object> payload) {
        return call(payCertUrl, payload);
    }

    /**
     * 領収書発行
     * 
     * @param payload
     * @return
     */
    public PGResult receipt(Map<String, Object> payload) {
        return get(paylistUrl, withAuth(payload));
    }
}
