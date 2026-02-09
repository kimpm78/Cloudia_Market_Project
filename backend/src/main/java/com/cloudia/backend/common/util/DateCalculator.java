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
     * 일본 현재 시간 취득
     *
     * @return yyyy/MM/dd hh:mm:ss 도쿄 시간
     */
    public LocalDateTime tokyoTime() {
        LocalDateTime tokyoTime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));

        return tokyoTime;
    }

    /**
     * 일본 현재 시간 취득
     *
     * @return yyyy-MM-dd 도쿄 시간
     */
    public String DateString() {
        String dateStr = tokyoTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT));

        return dateStr;
    }

    /**
     * 영업일 N일 후 (시작일 포함)
     * 
     * @param inputDate 시작 날짜 (yyyy-MM-dd)
     * @param days      영업일 수
     * @return N 영업일째 날짜 (yyyy-MM-dd)
     */
    public String calculateBusinessDay(String inputDate, int days) {
        int count = 0;
        String currentDate = inputDate;

        while (count < days) {
            // 토/일 또는 공휴일 체크
            if (!checkWeekend(currentDate) && !checkHoliday(currentDate)) {
                count++;
                if (count == days) {
                    break;
                }
            }
            // 다음날
            currentDate = nextDay(currentDate, 1);
        }

        return currentDate;
    }

    /**
     * N일 전후
     * 
     * @param inputDate
     * @param days
     * @return
     */
    public String nextDay(String inputDate, int days) {
        Calendar calendar = convartToCalendar(inputDate);
        if (days != 0) {
            calendar.add(Calendar.DATE, days);
        }
        return convartToString(calendar);
    }

    /**
     * 공휴일 체크
     * 
     * @param inputDate yyyy-MM-dd 형식
     * @return true: 공휴일, false: 평일
     */
    private boolean checkHoliday(String inputDate) {
        try {
            LocalDate date = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
            return utilMapper.isHoliday(date, COUNTRY_CODE);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토일 체크
     *
     * @param string 날짜
     * @return 체크결과
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
     * 현재 주의 시작일(일요일) 취득
     *
     * @return 이번 주 일요일
     */
    public LocalDate getCurrentWeekStartDate() {
        LocalDate starDate = convertToLocalDate(tokyoTime());
        return getWeekStartDate(starDate);
    }

    /**
     * 현재 주의 종료일(토요일) 취득
     *
     * @return 이번 주 토요일
     */
    public LocalDate getCurrentWeekEndDate() {
        LocalDate endDate = convertToLocalDate(tokyoTime());
        return getWeekEndDate(endDate);
    }

    /**
     * 해당 주의 시작일(일요일) 취득
     *
     * @param date 기준 날짜
     * @return 해당 주의 일요일
     */
    public LocalDate getWeekStartDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int daysToSubtract = dayOfWeek.getValue() % 7;
        return date.minusDays(daysToSubtract);
    }

    /**
     * 해당 주의 종료일(토요일) 취득
     *
     * @param date 기준 날짜
     * @return 해당 주의 토요일
     */
    public LocalDate getWeekEndDate(LocalDate date) {
        LocalDate weekStart = getWeekStartDate(date);
        return weekStart.plusDays(6);
    }

    /**
     * 올해 1월 1일 취득
     *
     * @return 올해 1월 1일
     */
    public LocalDate getStartOfYear() {
        return convertToLocalDate(tokyoTime()).withMonth(1).withDayOfMonth(1);
    }

    /**
     * 올해 12월 31일 취득
     *
     * @return 올해 12월 31일
     */
    public LocalDate getEndOfYear() {
        return convertToLocalDate(tokyoTime()).withMonth(12).withDayOfMonth(31);
    }

    /**
     * 포맷 변환(LocalDate)
     * 
     * @param inputDate 변환할 날짜시간
     * @return LocalDate
     */
    public LocalDate convertToLocalDate(LocalDateTime inputDate) {
        return inputDate.toLocalDate();
    }

    /**
     * 포맷 변환(String)
     *
     * @param inputDate 변환할 날짜
     * @return yyyy-MM-dd 형식의 문자열
     */
    public String convertToYYYYMMDD(LocalDate inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return inputDate.format(formatter);
    }

    /**
     * 포맷 변환(Calendar)
     * 
     * @param string 날짜
     * @return 캘린더
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
     * 포맷 변환(String)
     * 
     * @param calendar 캘린더
     * @return yyyy-MM-dd 형식의 문자열
     */
    private String convartToString(Calendar calendar) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        return dateFormat.format(calendar.getTime());
    }

    /**
     * 포맷 변환(String)
     * 
     * @param inputDate 변환할 날짜시간
     * @return yyyy-MM-dd 형식의 문자열
     */
    public String convertToYYYYMMDD(LocalDateTime inputDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        return inputDate.format(formatter);
    }

    /**
     * 포맷 변환(String)
     * 
     * @param inputDate 변환할 날짜시간
     * @param days      영업일 수
     * @return yy/MM/dd(E)
     */
    public String convertToYYMMDD(LocalDateTime inputDate, int days) {
        LocalDateTime targetDate = inputDate;

        if (days != 0) {
            String formattedDate = calculateBusinessDay(convertToYYYYMMDD(inputDate), days);
            targetDate = LocalDate.parse(formattedDate).atStartOfDay();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_YYMM_STRING, Locale.JAPAN);
        return targetDate.format(formatter);
    }

    /**
     * 입력 날짜가 현재 날짜 이후인지 확인
     * 
     * @param inputDate 비교할 날짜 (yyyy-MM-dd 형식)
     * @return 현재 날짜보다 미래면 true, 과거면 false
     */
    public boolean isFutureDate(String inputDate) {
        LocalDate input = LocalDate.parse(inputDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
        LocalDate today = convertToLocalDate(tokyoTime());

        return input.isAfter(today) || input.isEqual(today);
    }

    /**
     * 입력 월이 현재 월 이후인지 확인
     * 
     * @param inputMonth 비교할 월 (yyyy-MM 형식)
     * @return 현재 월보다 미래면 true, 과거면 false
     */
    public boolean isFutureMonth(String inputMonth) {
        YearMonth input = YearMonth.parse(inputMonth, DateTimeFormatter.ofPattern(DATE_FORMAT_YYMM));
        YearMonth currentMonth = YearMonth.from(convertToLocalDate(tokyoTime()));

        return input.isAfter(currentMonth) || input.equals(currentMonth);
    }
}
