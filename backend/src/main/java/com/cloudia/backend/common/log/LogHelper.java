package com.cloudia.backend.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHelper {
    private static final Logger log = LoggerFactory.getLogger("APPLICATION");

    /**
     * ログ出力
     * 
     * @param code ログコード
     * @param args メッセージパラメータ配列
     */
    public static void log(String code, String[] args) {
        try {
            Object[] objArgs = null;
            if (args != null && args.length > 0) {
                objArgs = new Object[args.length];
                System.arraycopy(args, 0, objArgs, 0, args.length);
            }

            // メッセージ取得
            String message = LogMessageLoader.getMessage(code, objArgs);

            // カテゴリ取得
            LogCategory category = LogMessageLoader.getCategory(code);

            // ログ出力
            switch (category) {
                case INFO:
                    log.info("[{}] {}", code, message);
                    break;
                case WARN:
                    log.warn("[{}] {}", code, message);
                    break;
                case ERROR:
                    log.error("[{}] {}", code, message);
                    break;
                case DEBUG:
                    log.debug("[{}] {}", code, message);
                    break;
                case TRACE:
                    log.trace("[{}] {}", code, message);
                    break;
                default:
                    log.info("[{}] {}", code, message);
            }

        } catch (Exception e) {
            log.error("LogHelper 오류 - code: {}, args: {}", code, args, e);
        }
    }

    /**
     * 例外を含むenumログ出力
     * 
     * @param code      ログコード
     * @param args      メッセージパラメータ配列
     * @param throwable エラー情報
     */
    public static void log(String code, String[] args, Throwable throwable) {
        try {
            Object[] objArgs = null;
            if (args != null && args.length > 0) {
                objArgs = new Object[args.length];
                System.arraycopy(args, 0, objArgs, 0, args.length);
            }

            String message = LogMessageLoader.getMessage(code, objArgs);
            LogCategory category = LogMessageLoader.getCategory(code);

            switch (category) {
                case ERROR:
                    log.error("[{}] {}", code, message, throwable);
                    break;
                case WARN:
                    log.warn("[{}] {}", code, message, throwable);
                    break;
                default:
                    log.error("[{}] {}", code, message, throwable);
                    break;
            }

        } catch (Exception e) {
            log.error("LogHelper エラー - code: {}, args: {}", code, args, e);
            log.error("原本例外:", throwable);
        }
    }

    /**
     * enum(LogMessage)용 ログ出力
     * 
     * @param logMessage ログメッセージ
     * @param args       メッセージパラメータ配列
     */
    public static void log(LogMessage logMessage, String[] args) {
        log(logMessage.getCode(), args);
    }

    /**
     * パラメータなしのenumログ出力
     * 
     * @param logMessage ログメッセージ
     */
    public static void log(LogMessage logMessage) {
        log(logMessage.getCode(), new String[] {});
    }

    /**
     * 例外を含むenumログを出力
     *
     * @param logMessage ログメッセージ(enum)
     * @param args       メッセージパラメータ配列
     * @param throwable  エラー情報
     */
    public static void log(LogMessage logMessage, String[] args, Throwable throwable) {
        log(logMessage.getCode(), args, throwable);
    }

    /**
     * パラメータなしのログを出力
     *
     * @param code ログコード
     */
    public static void log(String code) {
        log(code, new String[] {});
    }
}