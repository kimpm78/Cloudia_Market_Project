package com.cloudia.backend.common.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Properties;

@Slf4j
@Component
public class LogMessageLoader {
    private static final Properties LOG_MESSAGES = new Properties();
    private static final String LOG_MESSAGE_FILE = "log-message.properties";
    private static volatile boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream(LOG_MESSAGE_FILE);

            if (input == null) {
                log.error("로그 메시지 파일을 찾을 수 없습니다: {}", LOG_MESSAGE_FILE);
                return;
            }

            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            LOG_MESSAGES.load(reader);
            reader.close();

            initialized = true;

            log.info("로그 메시지 파일 로드 완료: {} 개", LOG_MESSAGES.size() / 2);

        } catch (IOException e) {
            log.error("로그 메시지 파일 로드 실패", e);
            initialized = false;
        }
    }

    /**
     * 메시지 템플릿 가져오기
     */
    public static String getTemplate(String code) {
        if (!initialized) {
            return String.format("[미초기화] 코드: %s", code);
        }

        String template = LOG_MESSAGES.getProperty(code + ".message");
        if (template == null) {
            log.warn("메시지를 찾을 수 없습니다 [코드: {}]", code);
            return String.format("메시지를 찾을 수 없습니다 [코드: %s]", code);
        }

        return template;
    }

    /**
     * 메시지 가져오기
     */
    public static String getMessage(String code, Object... args) {
        try {
            if (!initialized) {
                return String.format("[미초기화] 코드: %s", code);
            }

            String messageTemplate = LOG_MESSAGES.getProperty(code + ".message");
            if (messageTemplate == null) {
                return String.format("메시지를 찾을 수 없습니다 [코드: %s]", code);
            }

            if (args == null || args.length == 0) {
                return messageTemplate;
            }

            return MessageFormat.format(messageTemplate, args);

        } catch (IllegalArgumentException e) {
            log.error("메시지 포맷 오류 - 코드: {}, 인자 개수: {}", code, args.length, e);
            String template = LOG_MESSAGES.getProperty(code + ".message");
            return String.format("포맷 오류 [코드: %s, 템플릿: %s]", code, template);
        } catch (Exception e) {
            log.error("getMessage 오류 - 코드: {}", code, e);
            return String.format("메시지 로드 오류 [코드: %s]", code);
        }
    }

    public static LogCategory getCategory(String code) {
        try {
            if (!initialized) {
                return LogCategory.INFO;
            }

            String category = LOG_MESSAGES.getProperty(code + ".category", "INFO");
            return LogCategory.valueOf(category.toUpperCase());

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 로그 카테고리 (코드: {}), INFO로 대체", code);
            return LogCategory.INFO;
        }
    }
}