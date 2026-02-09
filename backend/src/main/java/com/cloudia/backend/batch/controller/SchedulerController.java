package com.cloudia.backend.batch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.cloudia.backend.batch.service.SchedulerService;
import com.cloudia.backend.common.util.DateCalculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class SchedulerController {

    @Autowired
    private final SchedulerService schedulerService;
    private final DateCalculator dateCalculator;

    /**
     * 毎日0時に実行されるバッチ
     */
    @Scheduled(cron = "${scheduling.tracking-update.cron}", zone = "Asia/Tokyo")
    public void executeDailyJob() {
        log.info("バッチ処理開始");
        log.info("開始時刻: {}", dateCalculator.tokyoTime());
        schedulerService.updateResv();
        schedulerService.sendPaymentDeadline();
        schedulerService.confirmDelivery();
        log.info("バッチ処理終了");
        log.info("終了時刻: {}", dateCalculator.tokyoTime());
    }

    /**
     * 毎年最終日の0時に実行されるバッチ
     */
    @Scheduled(cron = "${scheduling.tracking-insert.cron}", zone = "Asia/Tokyo")
    public void executeDailyJobs() {
        log.info("バッチ処理開始");
        log.info("開始時刻: {}", dateCalculator.tokyoTime());
        schedulerService.syncNextYearHolidays();
        log.info("バッチ処理終了");
        log.info("終了時刻: {}", dateCalculator.tokyoTime());
    }
}