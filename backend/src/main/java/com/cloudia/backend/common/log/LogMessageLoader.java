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
                log.error("ログメッセージファイルが見つかりません: {}", LOG_MESSAGE_FILE);
                return;
            }

            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            LOG_MESSAGES.load(reader);
            reader.close();

            initialized = true;

            log.info("ログメッセージファイルの読み込み完了: {} 件", LOG_MESSAGES.size() / 2);

        } catch (IOException e) {
            log.error("ログメッセージファイルの読み込み失敗", e);
            initialized = false;
        }
    }

    /**
     * メッセージテンプレートを取得
     */
    public static String getTemplate(String code) {
        if (!initialized) {
            return String.format("[未初期化] コード: %s", code);
        }

        String template = LOG_MESSAGES.getProperty(code + ".message");
        if (template == null) {
            log.warn("メッセージが見つかりません [コード: {}]", code);
            return String.format("メッセージが見つかりません [コード: %s]", code);
        }

        return template;
    }

    /**
     * メッセージを取得
     */
    public static String getMessage(String code, Object... args) {
        try {
            if (!initialized) {
                return String.format("[未初期化] コード: %s", code);
            }

            String messageTemplate = LOG_MESSAGES.getProperty(code + ".message");
            if (messageTemplate == null) {
                return String.format("メッセージが見つかりません [コード: %s]", code);
            }

            if (args == null || args.length == 0) {
                return messageTemplate;
            }

            return MessageFormat.format(messageTemplate, args);

        } catch (IllegalArgumentException e) {
            log.error("メッセージフォーマットエラー - コード: {}, 引数数: {}", code, args.length, e);
            String template = LOG_MESSAGES.getProperty(code + ".message");
            return String.format("フォーマットエラー [コード: %s, テンプレート: %s]", code, template);
        } catch (Exception e) {
            log.error("getMessage エラー - コード: {}", code, e);
            return String.format("メッセージ読み込みエラー [コード: %s]", code);
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
            log.warn("不正なログカテゴリ（コード: {}）のため、INFO に置き換えます", code);
            return LogCategory.INFO;
        }
    }
}