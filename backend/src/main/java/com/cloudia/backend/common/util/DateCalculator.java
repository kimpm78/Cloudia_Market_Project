package com.cloudia.backend.common.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.cloudia.backend.common.mapper.UtilMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DateCalculator {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_FORMAT_YYMM = "yyyy-MM";
    private static final String DATE_FORMAT_YYMM_STRING = "yy/MM/dd(E)";
    private static final String COUNTRY_CODE = "JP";
    private final UtilMapper utilMapper;

    /**
     * 日本の現在日時を取得
     *
     * @return yyyy/MM/dd hh:mm:ss（東京時間）
     */
    public LocalDateTime tokyoTime() {
        LocalDateTime tokyoTime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));

        return tokyoTime;
    }

    /**
     * 日本の現在日付を取得
     *
     * @return yyyy-MM-dd（東京時間）
     */
    public String DateString() {
        String dateStr = tokyoTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT));

        return dateStr;
    }

    /**
     * 営業日でN日後（開始日を含む）
     * 
     * @param inputDate 開始日（yyyy-MM-dd）
     * @param days      営業日数
     * @return N営業日目の日付（yyyy-MM-dd）
     */
    public String calculateBusinessDay(String inputDate, int days) {
        if (days <= 0) {
            return inputDate;
        }

        LocalDate current = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
        int count = 0;
        int safety = 0;
        final int maxLoop = 3660; // 異常データ時の無限ループ防止（約10年分）

        while (count < days) {
            if (!checkWeekend(current) && !checkHoliday(current)) {
                count++;
                if (count == days) {
                    break;
                }
            }
            current = current.plusDays(1);
            safety++;
            if (safety > maxLoop) {
                throw new IllegalStateException("営業日計算のループ上限を超過しました。 inputDate=" + inputDate
                        + ", days=" + days + ", current=" + current);
            }
        }

        return current.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * N日前／N日後
     * 
     * @param inputDate 基準日（yyyy-MM-dd）
     * @param days      加算日数（負数: 過去、正数: 未来）
     * @return 計算後の日付（yyyy-MM-dd）
     */
    public String nextDay(String inputDate, int days) {
        Calendar calendar = convartToCalendar(inputDate);
        if (days != 0) {
            calendar.add(Calendar.DATE, days);
        }
        return convartToString(calendar);
    }

    /**
     * 祝日チェック
     * 
     * @param inputDate yyyy-MM-dd 形式
     * @return true: 祝日, false: 平日
     */
    private boolean checkHoliday(String inputDate) {
        try {
            LocalDate date = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
            return checkHoliday(date);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 祝日チェック（LocalDate）
     *
     * @param date 日付
     * @return true: 祝日, false: 平日
     */
    private boolean checkHoliday(LocalDate date) {
        try {
            return utilMapper.isHoliday(date, COUNTRY_CODE);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 土日チェック
     *
     * @param inputDate 日付
     * @return チェック結果
     */
    private boolean checkWeekend(String inputDate) {
        Calendar calendar = convartToCalendar(inputDate);

        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return true;
        }
        return false;
    }

    /**
     * 土日チェック（LocalDate）
     *
     * @param date 日付
     * @return true: 土日, false: 平日
     */
    private boolean checkWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 今週の開始日（日曜日）を取得
     *
     * @return 今週の日曜日
     */
    public LocalDate getCurrentWeekStartDate() {
        LocalDate starDate = convertToLocalDate(tokyoTime());
        return getWeekStartDate(starDate);
    }

    /**
     * 今週の終了日（土曜日）を取得
     *
     * @return 今週の土曜日
     */
    public LocalDate getCurrentWeekEndDate() {
        LocalDate endDate = convertToLocalDate(tokyoTime());
        return getWeekEndDate(endDate);
    }

    /**
     * 指定週の開始日（日曜日）を取得
     *
     * @param date 基準日
     * @return 指定週の日曜日
     */
    public LocalDate getWeekStartDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int daysToSubtract = dayOfWeek.getValue() % 7;
        return date.minusDays(daysToSubtract);
    }

    /**
     * 指定週の終了日（土曜日）を取得
     *
     * @param date 基準日
     * @return 指定週の土曜日
     */
    public LocalDate getWeekEndDate(LocalDate date) {
        LocalDate weekStart = getWeekStartDate(date);
        return weekStart.plusDays(6);
    }

    /**
     * 今年の1月1日を取得
     *
     * @return 今年の1月1日
     */
    public LocalDate getStartOfYear() {
        return convertToLocalDate(tokyoTime()).withMonth(1).withDayOfMonth(1);
    }

    /**
     * 今年の12月31日を取得
     *
     * @return 今年の12月31日
     */
    public LocalDate getEndOfYear() {
        return convertToLocalDate(tokyoTime()).withMonth(12).withDayOfMonth(31);
    }

    /**
     * 形式変換（LocalDate）
     * 
     * @param inputDate 変換対象の日時
     * @return LocalDate
     */
    public LocalDate convertToLocalDate(LocalDateTime inputDate) {
        return inputDate.toLocalDate();
    }

    /**
     * 形式変換（String）
     *
     * @param inputDate 変換対象の日付
     * @return yyyy-MM-dd 形式の文字列
     */
    public String convertToYYYYMMDD(LocalDate inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return inputDate.format(formatter);
    }

    /**
     * 形式変換（Calendar）
     * 
     * @param inputDate 日付
     * @return Calendar
     */
    private Calendar convartToCalendar(String inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDate localDate = LocalDate.parse(inputDate, formatter);

        Date date = Date.from(
                localDate.atStartOfDay(ZoneId.of("Asia/Tokyo")).toInstant());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    /**
     * 形式変換（String）
     * 
     * @param calendar Calendar
     * @return yyyy-MM-dd 形式の文字列
     */
    private String convartToString(Calendar calendar) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        return dateFormat.format(calendar.getTime());
    }

    /**
     * 形式変換（String）
     * 
     * @param inputDate 変換対象の日時
     * @return yyyy-MM-dd 形式の文字列
     */
    public String convertToYYYYMMDD(LocalDateTime inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return inputDate.format(formatter);
    }

    /**
     * 形式変換（String）
     * 
     * @param inputDate 変換対象の日時
     * @param days      営業日数
     * @return yy/MM/dd(E)
     */
    public String convertToYYMMDD(LocalDateTime inputDate, int days) {
        LocalDateTime targetDate = inputDate != null ? inputDate : tokyoTime();

        if (days != 0) {
            String formattedDate = calculateBusinessDay(convertToYYYYMMDD(targetDate), days);
            targetDate = LocalDate.parse(formattedDate).atStartOfDay();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_YYMM_STRING, Locale.JAPAN);
        return targetDate.format(formatter);
    }

    /**
     * 入力日付が現在日付以降かを判定
     * 
     * @param inputDate 比較対象の日付（yyyy-MM-dd）
     * @return 当日以降なら true、過去なら false
     */
    public boolean isFutureDate(String inputDate) {
        LocalDate input = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
        LocalDate today = convertToLocalDate(tokyoTime());

        return input.isAfter(today) || input.isEqual(today);
    }

    /**
     * 入力月が当月以降かを判定
     * 
     * @param inputMonth 比較対象の月（yyyy-MM）
     * @return 当月以降なら true、過去なら false
     */
    public boolean isFutureMonth(String inputMonth) {
        YearMonth input = YearMonth.parse(inputMonth, DateTimeFormatter.ofPattern(DATE_FORMAT_YYMM));
        YearMonth currentMonth = YearMonth.from(convertToLocalDate(tokyoTime()));

        return input.isAfter(currentMonth) || input.equals(currentMonth);
    }
}
